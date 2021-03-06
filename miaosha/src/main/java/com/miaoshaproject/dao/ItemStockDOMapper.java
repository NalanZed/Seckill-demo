package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.service.model.UserModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemStockDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Mon Jul 04 09:45:29 CST 2022
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Mon Jul 04 09:45:29 CST 2022
     */
    int insert(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Mon Jul 04 09:45:29 CST 2022
     */
    int insertSelective(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Mon Jul 04 09:45:29 CST 2022
     */
    ItemStockDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Mon Jul 04 09:45:29 CST 2022
     */
    int updateByPrimaryKeySelective(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Mon Jul 04 09:45:29 CST 2022
     */
    int updateByPrimaryKey(ItemStockDO record);

    ItemStockDO selectByItemId(Integer itemId);

    int decreaseStock(@Param("itemId")Integer itemId,@Param("amount") Integer amount);

}