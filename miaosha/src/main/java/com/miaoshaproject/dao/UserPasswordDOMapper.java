package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.UserPasswordDO;
import org.apache.ibatis.annotations.Mapper;

public interface UserPasswordDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Thu Jun 30 10:08:39 CST 2022
     */
    int deleteByPrimaryKey(Integer id);

    UserPasswordDO selectByUserId(Integer userId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Thu Jun 30 10:08:39 CST 2022
     */
    int insert(UserPasswordDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Thu Jun 30 10:08:39 CST 2022
     */
    int insertSelective(UserPasswordDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Thu Jun 30 10:08:39 CST 2022
     */
    UserPasswordDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Thu Jun 30 10:08:39 CST 2022
     */
    int updateByPrimaryKeySelective(UserPasswordDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_password
     *
     * @mbg.generated Thu Jun 30 10:08:39 CST 2022
     */
    int updateByPrimaryKey(UserPasswordDO record);

    UserPasswordDO selectByTelphone(String telphone);

}