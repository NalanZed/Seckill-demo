package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderService {

    // 1.直接通过前端url传过来活动id，然后下单接口内校验对应id是否属于对应商品且活动已经开始
    // 2.直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单

    //推荐使用 方案一
    // 如果前端没有传递promo 相关参数，认定其为平销商品
    OrderModel createOrder(Integer userId,Integer itemId,Integer amount,Integer promoId,String stockLogId) throws BusinessException;
}
