package com.miaoshaproject.service.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ItemModel {
    private Integer id;
    // 商品名称
    @NotBlank(message = "商品名称不能为空")
    private String title;
    //价格
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0,message = "商品价格必须大于0")
    private BigDecimal price;
    //库存
    @NotNull(message = "商品库存不能为空")
    private Integer stock;
    //描述
    @NotBlank(message = "商品描述不能为空")
    private String description;
    //销量
    private Integer sales;
    //图片描述
    @NotBlank(message = "商品图片不能为空")
    private String imgUrl;

    // 聚合模型，聚合一个promoModel
    // 如果promoModel 不为空，则说明有尚未结束的秒杀活动
    private PromoModel promoModel;
}
