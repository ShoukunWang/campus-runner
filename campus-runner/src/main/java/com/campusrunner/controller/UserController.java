package com.campusrunner.controller;

import com.campusrunner.entity.User;
import com.campusrunner.service.UserService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, Object> params) {
        try {
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            String phone = (String) params.get("phone");
            String nickname = (String) params.get("nickname");
            Integer role = params.get("role") != null ? (Integer) params.get("role") : 0;

            if (username == null || password == null || phone == null) {
                return Result.error(400, "缺少必要参数");
            }

            Map<String, Object> result = userService.register(username, password, phone, nickname, role);
            return Result.success("注册成功", result);
        } catch (RuntimeException e) {
            return Result.error(1001, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        try {
            String account = (String) params.get("account");
            String password = (String) params.get("password");

            if (account == null || password == null) {
                return Result.error(400, "缺少必要参数");
            }

            Map<String, Object> result = userService.login(account, password);
            return Result.success("登录成功", result);
        } catch (RuntimeException e) {
            return Result.error(1004, e.getMessage());
        }
    }

    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String nickname = (String) params.get("nickname");
        String avatar = (String) params.get("avatar");
        String email = (String) params.get("email");

        User user = userService.updateProfile(userId, nickname, avatar, email);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        userService.logout(userId);
        return Result.success("退出登录成功", null);
    }
}
