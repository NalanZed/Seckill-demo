package com.miaoshaproject.mq;

import com.alibaba.fastjson.JSON;
import com.miaoshaproject.dao.StockLogDOMapper;
import com.miaoshaproject.dataobject.StockLogDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.OrderService;
import lombok.AllArgsConstructor;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {
    private DefaultMQProducer producer;
    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;
    @Value("${mq.topicname}")
    private String topicName;
    
    @Autowired
    private OrderService orderService;
    @Autowired(required = false)
    private StockLogDOMapper stockLogDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        // 做mq producer的初始化
//        producer = new DefaultMQProducer("producer_group");
//        producer.setNamesrvAddr(nameAddr);
//        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            // 监听的事件
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                Map args = (Map)o;
                Integer userId = (Integer) args.get("userId");
                Integer itemId = (Integer) args.get("itemId");
                Integer amount = (Integer) args.get("amount");
                Integer promoId = (Integer) args.get("promoId");
                String stockLogId = (String) args.get("stockLogId");
                try {
                    orderService.createOrder(userId,itemId,amount,promoId,stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    //设置对用的stockLog为回滚状态
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            //定时回调，确认事务消息状态
            // 当executeLocalTransaction方法长时间没有返回时，会回调这个方法
            // 来主动判断，下单以及库存扣减是否成功
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                // 根据是否扣减库存成功，来判断返回COMMIT,ROLLBACK,还是继续UNKOWN
                String jsonSTring = new String(messageExt.getBody());
                Map<String,Object> map = JSON.parseObject(jsonSTring,Map.class);
                Integer itemId= (Integer) map.get("itemId");
                Integer amount= (Integer) map.get("amount");
                String stockLogId = (String) map.get("stockLogId");

                /*
                    要想通过回查确认某一笔订单是否已经得到处理
                    光靠 itemId 和 amount 显然是不够的
                    我们还需要记录一些 操作型数据，log data

                    log data 也叫操作流水，指的是，系统运行过程中进行了哪些操作，
                    这些操作是否成功，时间，结果，等等的日志信息
                    方便我们采用的中间件可以通过回查这些log data得知
                    某些操作的执行状态
                    进而决定其接下来的执行策略，是继续等待，还是回滚。

                    这里，我们可以在下单之前，先生成一条库存预减的操作流水
                    当下单逻辑完成之后，根据下单业务的完成状态，改变
                    这条预减流水的状态，是成功或者是失败
                    这样，中间件就可以通过这个流水来回查业务的进行状况。

                 */
                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if(stockLogDO==null){
                    return LocalTransactionState.UNKNOW;
                }
                Integer status = stockLogDO.getStatus();
                if(status==2){
                    return LocalTransactionState.COMMIT_MESSAGE;
                }else if(status == 1){
                    return LocalTransactionState.UNKNOW;
                }else {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }
        });
    }
    // 事务型同步库存扣减消息
    //userModel.getId(), itemId, amount, promoId
    public boolean transactionAsyncReduceStock(Integer userId,Integer itemId,Integer amount,Integer promoId,String stockLogId){
        Map<String,Object> argsMap = new HashMap<>();
        Map<String,Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);
        bodyMap.put("stockLogId",stockLogId);
        argsMap.put("itemId",itemId);
        argsMap.put("amount",amount);
        argsMap.put("userId",userId);
        argsMap.put("promoId",promoId);
        argsMap.put("stockLogId",stockLogId);
        Message message = new Message(topicName,"increase", JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));
//        Message increaseSaleMessage = new Message("")
        TransactionSendResult transactionSendResult = null;
        try {
            //事务型消息
            //二阶段提交
            //刚发送出去时，处于prepare，这个状态的消息是不能被消费的
            //需要等待信号，将其状态改变
            transactionSendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if(transactionSendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
            return true;
        }else if(transactionSendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE){
            return false;
        }
        return true;
    }



    //同步库存扣减消息

    public boolean asyncReduceStock(Integer itemId,Integer amount) {
        Map<String,Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);
        Message message = new Message(topicName,"increase", JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));
        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
