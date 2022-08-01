package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.stereotype.Service;


public interface UserService {

    //通过用户Id查找User
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     *
     * @param telphone 用户手机
     * @param encrptPassword 用户加密后的密码
     * @throws BusinessException
     */
    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;

    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);
}
