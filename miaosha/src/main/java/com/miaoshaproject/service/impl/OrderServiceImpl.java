package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ValidatorImpl validator;
    @Autowired(required = false)
    private OrderDOMapper orderDOMapper;
    @Autowired(required = false)
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount,Integer promoId) throws BusinessException {
        // 1. 校验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel item = itemService.getItemById(itemId);
        if(item == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品不存在");
        }
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        ValidationResult validationResult = validator.validate(orderModel);
        if(validationResult.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrorMsg());
        }
        // 校验活动信息
        if(promoId != null){
            // (1) 校验对应活动是否存在这个适用商品
            if(promoId.intValue() != item.getPromoModel().getId()){
                // 校验活动id是否与数据库中的id相同
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }else{
                // (2) 校验活动是否正在进行
                if(item.getPromoModel().getStatus()!=2){
                    throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动还未开始");
                }
            }
        }

        // 2. 落单减库存\支付减库存
        boolean successed = itemService.decreaseStock(itemId, amount);
        if(!successed){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3.订单入库
        // 先得得到单价，算出amount * price 得到总价
        if(promoId != null){
            orderModel.setItemPrice(item.getPromoModel().getPromoPrice());
            orderModel.setOrderPrice(item.getPromoModel().getPromoPrice().multiply(BigDecimal.valueOf(amount)));
        }else{
            orderModel.setItemPrice(item.getPrice());
            orderModel.setOrderPrice(item.getPrice().multiply(BigDecimal.valueOf(amount)));
        }
        orderModel.setPromoId(promoId);
        OrderDO orderDO = convertFromOrderModel(orderModel);
        //生成唯一ID
        orderDO.setId(generateOrderNo());
        orderDOMapper.insertSelective(orderDO);
        //4. 更新销量
        itemService.increaseSales(itemId,amount);
        // 5. 返回前端
        return orderModel;
    }
    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        return orderDO;
    }

    // 当外层事务（createOrder发生问题回滚时，会将对sequence_info表的操作也一并回滚
    // 而我们希望即使是失败的createOder事务也应该占用一个sequence，不希望后来的添加操作重复使用这个sequence
    // 因此： 无论外层事务 createOrder是否成功，seuqence表的更新操作都正常提交
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo(){
        StringBuilder builder = new StringBuilder();
        // 订单号16位
        //前8位时间信息、年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        builder.append(nowDate);

        //中间6位自增序列
        int sequence = 0;
        // 获取当前sequence
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        // 设置其id为当前值 + 步长
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        // 填充0
        for(int i =0;i<6-sequenceStr.length();++i){
            builder.append(0);
        }
        builder.append(sequenceStr);
        //最后两位分库分表位00-99
        builder.append("00");
        return builder.toString();
    }
}
