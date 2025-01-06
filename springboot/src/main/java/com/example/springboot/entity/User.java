/**
 * 功能：
 * 作者：Pjkang
 * 日期：2024/5/13 11:29
 */

package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data  // 通过该注解，可以省略getter和setter方法和构造方法和builder方法
@TableName("sys_user")  // 通过该注解，可以指定表名
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String phone;
    private String avatar;
    private String gender;
    private Date birthday;
    private BigDecimal balance;
    private Integer points;
    private String role;
    private Date createTime;
    private Date updateTime;
    
    @TableField(exist = false)  // 通过该注解，可以指定该字段不是数据库表中的字段
    private String token;
    
    @TableField(exist = false)
    private String code;  // 验证码
    
    @TableField(exist = false)
    private String codeKey;  // 验证码key
    
    @TableField(exist = false)
    private String loginType;  // 登录类型：admin/member
    
    @TableField(exist = false)
    private String oldPassword;  // 修改密码时的原密码
    
    @TableField(exist = false)
    private String newPassword;  // 修改密码时的新密码
}
