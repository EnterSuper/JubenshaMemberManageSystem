/**
 * 功能：提供接口返回数据
 * 作者：Pjkang
 * 日期：2024/5/12 15:56
 */

package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.ValidateCodeUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class WebController {
    @Resource
    private UserService userService;

    @AuthAccess
    @GetMapping("/")
    public Result hello() {
        return Result.success("success");
    }

    // 登录
    @PostMapping(value = "/login")
    public Result login(@RequestBody User user, HttpSession session) {
        // 验证码校验
        String validCode = user.getCode();
        String sessionCode = (String) session.getAttribute("captcha");
        
        if (StrUtil.isBlank(validCode)) {
            return Result.error("请输入验证码");
        }
        
        if (sessionCode == null) {
            return Result.error("验证码已过期，请刷新验证码");
        }
        
        // 验证码比较（忽略大小写）
        if (!validCode.trim().equalsIgnoreCase(sessionCode.trim())) {
            return Result.error("验证码错误");
        }
        
        // 验证码正确，立即从session中移除
        session.removeAttribute("captcha");
        
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("用户名或密码不能为空");
        }
        user = userService.login(user);
        return Result.success(user);
    }

    @AuthAccess
    @PostMapping(value = "/register")
    public Result register(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("用户名或密码不能为空");
        }
        if (StrUtil.isBlank(user.getPhone())) {
            return Result.error("手机号不能为空");
        }
        // 验证用户名长度
        if (user.getUsername().length() < 2 || user.getUsername().length() > 10) {
            return Result.error("用户名长度必须在2-10之间");
        }
        // 验证密码长度
        if (user.getPassword().length() < 6) {
            return Result.error("密码长度不能小于6位");
        }
        // 验证手机号格式
        if (!user.getPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式不正确");
        }
        
        user = userService.register(user);
        return Result.success(user);
    }

    @AuthAccess
    @PostMapping("/password/reset")
    public Result resetPassword(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPhone())) {
            return Result.error("用户名和手机号不能为空");
        }
        userService.resetPassword(user);
        return Result.success();
    }
    
    @AuthAccess
    @GetMapping("/captcha")
    public Result getCaptcha(HttpSession session) {
        try {
            // 生成验证码
            String code = ValidateCodeUtil.generateCode();
            // 将验证码存入session
            session.setAttribute("captcha", code);
            System.out.println("生成验证码: " + code);
            return Result.success(code);
        } catch (Exception e) {
            System.err.println("生成验证码出错: " + e.getMessage());
            return Result.error("生成验证码失败");
        }
    }

    @AuthAccess
    @GetMapping("/test/db")
    public Result testDb() {
        try {
            // 测试查询所有用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            List<User> users = userService.list(queryWrapper);
            System.out.println("Total users in database: " + users.size());
            for (User user : users) {
                System.out.println("User: " + user.getUsername() + ", Role: " + user.getRole());
            }
            return Result.success(users);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Database error: " + e.getMessage());
        }
    }

    @AuthAccess
    @GetMapping("/test/simple")
    public Result testSimple() {
        return Result.success("Test successful");
    }
}
