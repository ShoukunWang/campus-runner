package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    Map<String, Object> register(String username, String password, String phone, String nickname, Integer role);

    Map<String, Object> login(String account, String password);

    User getCurrentUser(String userId);

    User updateProfile(String userId, String nickname, String avatar, String email);

    void logout(String userId);
}
