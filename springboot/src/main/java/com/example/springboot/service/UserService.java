/**
 * 功能：
 * 作者：Pjkang
 * 日期：2024/5/13 11:49
 */

package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.utils.PasswordEncoder;
import com.example.springboot.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Resource
    UserMapper userMapper;

    public User selectByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);  //  eq => ==   where username = #{username}
        // 根据用户名查询数据库的用户信息
        return getOne(queryWrapper); //  select * from user where username = #{username}
    }

    // 验证用户账户是否合法
    public User login(User user) {
        System.out.println("============ Login Debug Info ============");
        System.out.println("Login attempt with data:");
        System.out.println("Username/Phone: " + user.getUsername());
        System.out.println("Raw Password: " + user.getPassword());
        System.out.println("Login Type: " + user.getLoginType());
        
        // 先尝试用用户名查询
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser != null) {
            System.out.println("Found user by username");
        }
        
        // 如果用户名查询不到，尝试用手机号查询
        if (dbUser == null) {
            System.out.println("User not found by username, trying phone number...");
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", user.getUsername());  // 使用输入的用户名作为手机号查询
            dbUser = getOne(queryWrapper);
            if (dbUser != null) {
                System.out.println("Found user by phone number");
            }
        }
        
        if (dbUser == null) {
            System.out.println("User not found in database");
            throw new ServiceException("用户名或密码错误");
        }
        
        System.out.println("Database user info:");
        System.out.println("ID: " + dbUser.getId());
        System.out.println("Username: " + dbUser.getUsername());
        System.out.println("Phone: " + dbUser.getPhone());
        System.out.println("Role: " + dbUser.getRole());
        
        // 验证密码
        String rawPassword = user.getPassword();
        String encodedInputPassword = PasswordEncoder.encode(rawPassword);
        String dbPassword = dbUser.getPassword();
        
        System.out.println("Password verification:");
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Encoded input password: " + encodedInputPassword);
        System.out.println("Database password: " + dbPassword);
        
        if (!PasswordEncoder.matches(rawPassword, dbPassword)) {
            System.out.println("Password mismatch!");
            throw new ServiceException("用户名或密码错误");
        }
        
        System.out.println("Password verified successfully");
        
        // 生成token
        String token = TokenUtils.createToken(dbUser.getId().toString(), dbUser.getPassword());
        dbUser.setToken(token);
        System.out.println("Generated token: " + token);
        
        // 更新最后登录时间
        dbUser.setUpdateTime(new Date());
        updateById(dbUser);
        
        System.out.println("Login successful for user: " + dbUser.getUsername());
        System.out.println("=========================================");
        return dbUser;
    }

    public User register(User user) {
        // 验证用户名是否已存在
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 验证手机号是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", user.getPhone());
        dbUser = getOne(queryWrapper);
        if (dbUser != null) {
            throw new ServiceException("手机号已被注册");
        }

        // 设置默认值
        user.setRole("member");
        user.setPoints(0);
        user.setBalance(new BigDecimal("0"));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        // 加密密码
        user.setPassword(PasswordEncoder.encode(user.getPassword()));
        
        save(user);
        return user;
    }

    public void resetPassword(User user) {
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser == null) {
            throw new ServiceException("用户不存在");
        }
        if (!user.getPhone().equals(dbUser.getPhone())) {
            throw new ServiceException("手机号验证失败");
        }
        
        // 重置密码为123456并加密
        dbUser.setPassword(PasswordEncoder.encode("123456"));
        dbUser.setUpdateTime(new Date());
        updateById(dbUser);
    }

    public void updateProfile(User user) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || !currentUser.getId().equals(user.getId())) {
            throw new ServiceException("无权限");
        }
        
        // 只允许修改某些字段
        User dbUser = getById(user.getId());
        dbUser.setGender(user.getGender());
        dbUser.setBirthday(user.getBirthday());
        dbUser.setAvatar(user.getAvatar());
        dbUser.setUpdateTime(new Date());
        
        updateById(dbUser);
    }

    public void recharge(Integer userId, BigDecimal amount) {
        User user = getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 更新余额
        user.setBalance(user.getBalance().add(amount));
        user.setUpdateTime(new Date());
        updateById(user);
    }

    public void updatePoints(Integer userId, Integer points) {
        User user = getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 更新积分
        user.setPoints(user.getPoints() + points);
        user.setUpdateTime(new Date());
        updateById(user);
    }
}