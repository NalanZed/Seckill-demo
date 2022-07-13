package com.miaoshaproject;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@RestController
@MapperScan({"com.miaoshaproject.dao"})
public class App
{
    //required = false 是为了解决idea无法识别mybatis创建的mapper的误会问题
    //语义上还是必须要userDOmapper的
    @Autowired(required = false)
    private UserDOMapper userDOMapper;

    @RequestMapping("/")
    public String selectUser(){
        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        if(userDO == null){
            return "用户不存在";
        }else {
            return userDO.getName();
        }
    }

    public static void main( String[] args )
    {
        SpringApplication.run(App.class,args);
    }
}
