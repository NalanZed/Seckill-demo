package com.miaoshaproject.service.model;

import lombok.Data;
import org.joda.time.DateTime;

import java.math.BigDecimal;

@Data
public class PromoModel {
    private Integer id;
    // 秒杀活动名称
    private String promoName;
    // 秒杀开始时间
    private DateTime startDate;
    // 秒杀开始时间
    private DateTime endDate;
    // 商品id
    private Integer itemId;
    // 秒杀活动价格
    private BigDecimal promoPrice;
    //秒杀活动状态(1表示未开始，2表示进行中，3表示已结束)
    private Integer status;


}
