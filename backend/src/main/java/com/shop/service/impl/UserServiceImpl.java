package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.entity.User;
import com.shop.exception.BusinessException;
import com.shop.mapper.UserMapper;
import com.shop.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User register(String username, String password, String nickname) {
        logger.info("用户注册: {}", username);
        
        // 检查用户名是否已存在
        User existUser = getByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 实际项目中应加密
        user.setNickname(nickname);
        user.setRole(0); // 普通用户
        user.setStatus(1); // 启用
        user.setDeleted(0);

        save(user);
        logger.info("用户注册成功: {}", username);
        return user;
    }

    @Override
    public User login(String username, String password) {
        logger.info("用户登录: {}", username);
        
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        logger.info("用户登录成功: {}", username);
        return user;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getDeleted, 0);
        return getOne(wrapper);
    }
}
