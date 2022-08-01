package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);

    // 活动发布
    void publishPromo(Integer promoId);
}
