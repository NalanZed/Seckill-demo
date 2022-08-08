package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {
    @Autowired(required = false)
    private PromoDOMapper promoDOMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

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

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动后id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO.getItemId() == null || promoDO.getItemId().intValue()==0){
            return;
        }
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        // 将stock缓存存入redis
        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(),itemModel.getStock());

        //设置秒杀大闸限制
        redisTemplate.opsForValue().set("promo_door_count_"+promoId,itemModel.getStock().intValue() * 5);
    }

    // 生成令牌的过程，把校验流程给执行掉
    @Override
    public String generateSecondKillToken(Integer promoId, Integer userId, Integer itemId) {

        boolean hasSoldOut = redisTemplate.hasKey("promo_item_stock_invalid_" + itemId);
        if(hasSoldOut){
            return null;
        }

        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
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
        // 判断活动是否正在进行
        if(promoModel.getStatus()!=2){
            return null;
        }
        // 验证 item和user的合法性
        ItemModel item = itemService.getItemByIdInCache(itemId);
        UserModel user = userService.getUserByIdInCache(userId);
        if(item == null || user == null){
            return null;
        }

        //获取秒杀大闸的count,控制流量
        Long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId, -1);
        if(result <0){
            return null;
        }

        //生成秒杀令牌 String，存入redis，并给出5分钟有效期
        String token = UUID.randomUUID().toString().replace("-","");
        String key = "promo_token_"+promoId + "user_id_" + userId + "item_id_" + itemId;
        redisTemplate.opsForValue().set(key,token);
        redisTemplate.expire(key,5, TimeUnit.MINUTES);
        return token;
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
