package com.miaoshaproject.service.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

// 交易模型
@Data
public class OrderModel {
    // 订单编号，设置为String，最好赋予一定的业务意义
    private String id;
    // 购买者ID
    @NotNull(message = "用户id不能为空")
    private Integer userId;
    // 商品ID
    @NotNull(message = "商品id不能为空")
    private Integer itemId;
    //购买数量
    @Min(value = 1,message = "商品数量必须大于0")
    @Max(value = 100,message = "最多同时下单100件")
    @NotNull(message = "商品数量不能为空")
    private Integer amount;
    //总金额(若promo不为0，就是秒杀总价格)
    private BigDecimal orderPrice;
    // 本次购买时，商品的价格(若promo不为null，就是秒杀价格)
    private BigDecimal itemPrice;

    // 若非0，则表示以秒杀方式下单
    private Integer promoId;

}
