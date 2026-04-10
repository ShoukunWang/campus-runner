package com.campusrunner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Favorite;
import com.campusrunner.entity.Task;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.service.FavoriteService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private TaskMapper taskMapper;

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleFavorite(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String taskId = (String) params.get("taskId");

        if (taskId == null) {
            return Result.error(400, "任务ID不能为空");
        }

        Map<String, Object> result = favoriteService.toggleFavorite(userId, taskId);
        boolean isFavorite = (boolean) result.get("isFavorite");
        return Result.success(isFavorite ? "收藏成功" : "取消收藏成功", result);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getMyFavorites(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        Page<Favorite> page = favoriteService.getMyFavorites(userId, pageNum, pageSize);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Favorite favorite : page.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteId", favorite.getId());
            item.put("taskId", favorite.getTaskId());
            item.put("createTime", favorite.getCreateTime());

            Task task = taskMapper.selectById(favorite.getTaskId());
            if (task != null) {
                item.put("taskNo", task.getTaskNo());
                item.put("title", task.getTitle());
                item.put("reward", task.getReward());
                item.put("status", task.getStatus());
                item.put("pickupAddress", task.getPickupAddress());
                item.put("deliveryAddress", task.getDeliveryAddress());
            }

            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        result.put("list", list);

        return Result.success(result);
    }

    @GetMapping("/check/{taskId}")
    public Result<Map<String, Object>> checkFavorite(@PathVariable String taskId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        boolean isFavorite = favoriteService.isFavorite(userId, taskId);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("isFavorite", isFavorite);

        return Result.success(result);
    }
}
