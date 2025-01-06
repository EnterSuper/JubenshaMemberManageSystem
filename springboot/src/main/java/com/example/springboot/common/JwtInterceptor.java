/**
 * 功能：
 * 作者：Pjkang
 * 日期：2024/5/26 10:21
 */

package com.example.springboot.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.out.println("JwtInterceptor - Request URI: " + request.getRequestURI());
        
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        System.out.println("JwtInterceptor - Token: " + token);
        
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            System.out.println("JwtInterceptor - Not a handler method, passing through");
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        System.out.println("JwtInterceptor - Handler method: " + handlerMethod.getMethod().getName());
        
        AuthAccess annotation = handlerMethod.getMethodAnnotation(AuthAccess.class);
        if (annotation != null) {
            System.out.println("JwtInterceptor - @AuthAccess annotation found, passing through");
            return true;  // 有@AuthAccess注解，直接放行
        }
        
        System.out.println("JwtInterceptor - No @AuthAccess annotation, checking token");
        
        // 执行认证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException("401", "请登录");
        }
        
        // 获取 token 中的 user id
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);  // 获取token中的userid
            System.out.println("JwtInterceptor - Decoded user ID: " + userId);
        } catch (JWTDecodeException j) {
            System.out.println("JwtInterceptor - Failed to decode token");
            throw new ServiceException("401", "请登录");
        }
        
        // 根据token中的userid查询数据库
        User user = userMapper.selectById(Integer.valueOf(userId));
        if (user == null) {
            System.out.println("JwtInterceptor - User not found in database");
            throw new ServiceException("401", "请登录");
        }
        
        System.out.println("JwtInterceptor - Found user: " + user.getUsername());
        
        // 用户密码加签验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            jwtVerifier.verify(token); // 验证token
            System.out.println("JwtInterceptor - Token verified successfully");
        } catch (JWTVerificationException e) {
            System.out.println("JwtInterceptor - Token verification failed");
            throw new ServiceException("401", "请登录");
        }
        return true;
    }
}