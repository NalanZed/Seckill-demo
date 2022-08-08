package com.miaoshaproject.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.util.CodeUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin(originPatterns = "*",allowCredentials="true",allowedHeaders = "*",methods = {})
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemService itemService;
    @Autowired
    private PromoService promoService;

    private ExecutorService executorService;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init(){
        //工作线程为20
        executorService = Executors.newFixedThreadPool(20);
        //初始化limit
        orderCreateRateLimiter = RateLimiter.create(200);
    }


    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    private CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                         @RequestParam(name = "amount")Integer amount,
                                         @RequestParam(name= "promoId",required = false)Integer promoId,
                                         @RequestParam(name= "promoToken",required = false)String promoToken) throws BusinessException {


        // 获取用户的登录信息
//        Boolean is_login = (Boolean)session.getAttribute("IS_LOGIN");
//        if(is_login == null){
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
//        }
        //令牌桶中获取令牌
        if(!orderCreateRateLimiter.tryAcquire()){
            throw new BusinessException(EmBusinessError.RATELIMIT);
        }



        // 获取token,只有一个
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        //从redis中获取ｔｏｋｅｎ对应的用户信息
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);

        //检验秒杀令牌
        if(promoId != null){
            String key = "promo_token_"+promoId + "user_id_" + userModel.getId() + "item_id_" + itemId;
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get(key);
            if(inRedisPromoToken == null || !inRedisPromoToken.equals(promoToken)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌错误...");
            }
        }

        //同步调用线程池的submit方法
        // 拥塞窗口为20的等待队列，用来队列泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                String stockLogId = itemService.initStockLog(itemId, amount);

                boolean successed = mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, amount, promoId, stockLogId);
                if (!successed) {
                    throw new BusinessException(EmBusinessError.UNKNOW_ERROR, "下单失败...");
                }
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOW_ERROR);
        }

        // 从登录session中直接获得userModel信息
//        userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
//        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,amount,promoId);

        // 检查售罄标识---前置到令牌生成中
//        Boolean hasSoldOut = redisTemplate.hasKey("promo_item_stock_invalid_" + itemId);
//        if(hasSoldOut){
//            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH,"下单失败...,已售罄");
//        }
        // 加入库存流水init 状态,初始化库存流水 --- 放入队列中异步执行，队列泄洪
//        String stockLogId = itemService.initStockLog(itemId,amount);
//
//        boolean successed = mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, amount, promoId,stockLogId);
//        if(!successed){
//            throw new BusinessException(EmBusinessError.UNKNOW_ERROR,"下单失败...");
//        }
        return CommonReturnType.create(null);
    }


    //生成验证码
    @RequestMapping(value = "/generateverifycode",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    private void generateverifycode(HttpServletResponse response) throws BusinessException, IOException {
        // 根据token获取用户登录信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        // 将img对象写入到response中
        redisTemplate.opsForValue().set("verify_code_" + userModel.getId(),map.get("code"));
        redisTemplate.expire("verify_code_" + userModel.getId(),5,TimeUnit.MINUTES);
        ImageIO.write((RenderedImage)map.get("codePic"),"jpeg",response.getOutputStream());
    }


    //请求秒杀令牌
    @RequestMapping(value = "/generatetoken",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    private CommonReturnType generateToken(@RequestParam(name = "itemId")Integer itemId,
                                           @RequestParam(name= "promoId")Integer promoId,
                                           @RequestParam(name = "verifyCode")String verifyCode) throws BusinessException{

        // 根据token获取用户登录信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //通过验证码验证
        String inRedisVerfifyCode = (String) redisTemplate.opsForValue().get("verify_code_" + userModel.getId());
        if(StringUtils.isEmpty(inRedisVerfifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"非法请求");
        }
        if(!inRedisVerfifyCode.equalsIgnoreCase(verifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"验证码错误");
        }

        //获取秒杀令牌
        String secondKillToken  = promoService.generateSecondKillToken(promoId, userModel.getId(),itemId);
        if(secondKillToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        return CommonReturnType.create(secondKillToken);
    }


}
