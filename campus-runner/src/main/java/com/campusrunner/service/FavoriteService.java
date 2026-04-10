package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.Favorite;

import java.util.Map;

public interface FavoriteService extends IService<Favorite> {

    Map<String, Object> toggleFavorite(String userId, String taskId);

    Page<Favorite> getMyFavorites(String userId, Integer pageNum, Integer pageSize);

    boolean isFavorite(String userId, String taskId);
}
