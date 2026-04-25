package com.campusrunner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.Favorite;
import com.campusrunner.mapper.FavoriteMapper;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional
    public Map<String, Object> toggleFavorite(String userId, String taskId) {
        synchronized (this) {
            Favorite existingFavorite = baseMapper.selectByUserAndTask(userId, taskId);
            Map<String, Object> result = new HashMap<>();

            if (existingFavorite != null) {
                baseMapper.deleteById(existingFavorite.getId());
                taskMapper.updateFavoriteCount(taskId, -1);
                result.put("isFavorite", false);
            } else {
                Favorite favorite = new Favorite();
                favorite.setUserId(userId);
                favorite.setTaskId(taskId);
                favorite.setCreateTime(LocalDateTime.now());
                favorite.setUpdateTime(LocalDateTime.now());
                baseMapper.insert(favorite);
                taskMapper.updateFavoriteCount(taskId, 1);
                result.put("isFavorite", true);
            }

            int favoriteCount = baseMapper.countByTaskId(taskId);
            result.put("taskId", taskId);
            result.put("favoriteCount", favoriteCount);

            return result;
        }
    }

    @Override
    public Page<Favorite> getMyFavorites(String userId, Integer pageNum, Integer pageSize) {
        Page<Favorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean isFavorite(String userId, String taskId) {
        return baseMapper.selectByUserAndTask(userId, taskId) != null;
    }
}
