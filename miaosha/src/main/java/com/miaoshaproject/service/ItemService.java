package com.miaoshaproject.service;

import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    // 获取商品全部信息
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    // 浏览商品
    List<ItemModel> listItem();

    //商品详情
    ItemModel getItemById(Integer id);

    //库存扣减
    boolean decreaseStock(Integer id,Integer amount)throws BusinessException;
    boolean rollbackStock(Integer id,Integer amount);
    // 商品销量增加
    void increaseSales(Integer item_id,Integer amount)throws BusinessException;

    // 验证item 及 promo是否有效
    // item 及 promo model 缓存模型
    ItemModel getItemByIdInCache(Integer id);

    //发送异步更新库存消息
    boolean asyncDecreaseStock(Integer id,Integer amount);

    // 初始化库存流水
    String initStockLog(Integer itemId, Integer amount);

}
