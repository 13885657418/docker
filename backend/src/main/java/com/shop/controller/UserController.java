package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.User;
import com.shop.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        logger.info("用户注册请求");
        String username = params.get("username");
        String password = params.get("password");
        String nickname = params.get("nickname");
        
        User user = userService.register(username, password, nickname);
        user.setPassword(null); // 不返回密码
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        logger.info("用户登录请求");
        String username = params.get("username");
        String password = params.get("password");
        
        User user = userService.login(username, password);
        user.setPassword(null); // 不返回密码
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        // 简化处理，实际项目应使用JWT等token机制
        result.put("token", "token_" + user.getId() + "_" + System.currentTimeMillis());
        
        return Result.success("登录成功", result);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        logger.info("获取用户信息: id={}", id);
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody User user) {
        logger.info("更新用户信息: id={}", user.getId());
        user.setPassword(null); // 不允许通过此接口修改密码
        userService.updateById(user);
        return Result.success();
    }
}
