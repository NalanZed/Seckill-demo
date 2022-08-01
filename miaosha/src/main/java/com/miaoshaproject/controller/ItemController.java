package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.CacheService;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController("item")
@RequestMapping("/item")
@CrossOrigin(originPatterns = "*",allowCredentials="true",allowedHeaders = "*",methods = {})
public class ItemController extends BaseController{

    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PromoService promoService;

    @RequestMapping(value = "/create",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {
        // 封装sercvice请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setPrice(price);
        itemModel.setTitle(title);
        // 创建！
        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        // 要返回的是一个VO对象
        ItemVO itemVO = convertItemVOFromModel(itemModelForReturn);

        return CommonReturnType.create(itemVO);
    }

    // 调用promoservice，将活动信息发布
    // 内容就是把当前item的stock发布到redis中
    // 这里存在安全问题。
    @RequestMapping(value = "/publishpromo",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType publishPromo(@RequestParam(name = "id") Integer id){
        promoService.publishPromo(id);
        return CommonReturnType.create(null);
    }


    @RequestMapping(value = "/get",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id){
        ItemModel itemModel = null;

        // 先读本地缓存，本地缓存->redis->db
        itemModel = (ItemModel)cacheService.getFromCommonCache("item_" + id);
        if(itemModel == null){
            //读redis缓存,redis->db
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);
            // 缓存中不存在，则访问下游service
            if(itemModel == null){
                itemModel = itemService.getItemById(id);
                // 写入redis缓存
                redisTemplate.opsForValue().set("item_" + id , itemModel);
                //设置数据过期时间
                redisTemplate.expire("item_" + id,10, TimeUnit.MINUTES);
            }
            cacheService.setCommonCache("item_" + id,itemModel);
        }
        ItemVO itemVO = convertItemVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    @RequestMapping(value = "/list",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList = itemService.listItem();

        // 把ItemModel 转换成 ItemVO
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertItemVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

    private ItemVO convertItemVOFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        if(itemModel.getPromoModel()!=null){
            // 有正在进行或即将进行的秒杀活动
            PromoModel itemModelPromoModel = itemModel.getPromoModel();
            itemVO.setPromoPrice(itemModelPromoModel.getPromoPrice());
            itemVO.setPromoId(itemModelPromoModel.getId());
            itemVO.setPromoStatus(itemModelPromoModel.getStatus());
            itemVO.setStartDate(itemModelPromoModel.getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        }else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }
}
