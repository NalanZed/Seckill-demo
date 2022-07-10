package com.miaoshaproject.controller.viewobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemVO {
    private Integer id;
    // 商品名称
    private String title;
    //价格
    private BigDecimal price;
    //库存
    private Integer stock;
    //描述
    private String description;
    //销量
    private Integer sales;
    //图片描述
    private String imgUrl;
    // 记录商品的活动情况
    // 0 无活动信息
    // 1 活动待开始
    // 2 活动进行中
    private Integer promoStatus;

    // 秒杀活动价格
    private BigDecimal promoPrice;
    // 秒杀活动Id
    private Integer promoId;
    //秒杀活动开始时间
    private String startDate;


}
