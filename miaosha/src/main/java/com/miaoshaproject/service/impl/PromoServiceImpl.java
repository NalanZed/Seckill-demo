package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromoServiceImpl implements PromoService {
    @Autowired(required = false)
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = convertFormPromoDO(promoDO);
        if(promoModel == null){
            return null;
        }
        // 判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isAfterNow()){
            promoModel.setStatus(2);
        }else {
            promoModel.setStatus(3);
        }
        return promoModel;
    }

    private PromoModel convertFormPromoDO(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        // mysql 中是 java util的date实现
        // 我们用的是 joda dateTime
        // 由于类型不同，需要另外手动转
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
