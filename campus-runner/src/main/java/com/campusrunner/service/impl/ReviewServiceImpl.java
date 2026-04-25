package com.campusrunner.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.Review;
import com.campusrunner.entity.Task;
import com.campusrunner.entity.User;
import com.campusrunner.mapper.ReviewMapper;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Review submitReview(String taskId, String reviewerId, Integer rating, String content,
                               List<String> tags, Boolean isAnonymous) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (task.getStatus() != 4) {
            throw new RuntimeException("任务未完成，暂不能评价");
        }

        boolean isPublisher = task.getPublisherId().equals(reviewerId);
        boolean isRunner = task.getRunnerId() != null && task.getRunnerId().equals(reviewerId);

        if (!isPublisher && !isRunner) {
            throw new RuntimeException("您无权评价此任务");
        }

        Review existingReview = baseMapper.selectByTaskAndReviewer(taskId, reviewerId);
        if (existingReview != null) {
            throw new RuntimeException("您已评价过此任务");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5星之间");
        }

        String revieweeId = isPublisher ? task.getRunnerId() : task.getPublisherId();

        Review review = new Review();
        review.setTaskId(taskId);
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setReviewType(isPublisher ? 1 : 2);
        review.setRating(rating);
        review.setContent(content);
        review.setTags(tags != null ? String.join(",", tags) : null);
        review.setIsAnonymous(isAnonymous != null && isAnonymous ? 1 : 0);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(review);

        updateUserCreditScore(revieweeId, rating);

        return review;
    }

    private void updateUserCreditScore(String userId, Integer rating) {
        synchronized (ReviewServiceImpl.class) {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return;
            }

            int scoreChange = 0;
            switch (rating) {
                case 5:
                    scoreChange = 5;
                    break;
                case 4:
                    scoreChange = 3;
                    break;
                case 3:
                    scoreChange = 1;
                    break;
                case 2:
                    scoreChange = -2;
                    break;
                case 1:
                    scoreChange = -5;
                    break;
            }

            int newScore = user.getCreditScore() + scoreChange;
            user.setCreditScore(Math.max(0, Math.min(100, newScore)));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    @Override
    public Page<Review> getReviewList(String taskId, String userId, Integer reviewType,
                                     Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(taskId)) {
            wrapper.eq(Review::getTaskId, taskId);
        }

        if (StrUtil.isNotBlank(userId)) {
            wrapper.eq(Review::getRevieweeId, userId);
        }

        if (reviewType != null) {
            wrapper.eq(Review::getReviewType, reviewType);
        }

        wrapper.orderByDesc(Review::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getUserRatingStats(String userId) {
        Map<String, Object> stats = new HashMap<>();

        Double avgRating = baseMapper.selectAverageRatingByUserId(userId);
        stats.put("avgRating", avgRating != null ? avgRating : 0.0);

        int totalReviews = baseMapper.countByRevieweeId(userId);
        stats.put("totalReviews", totalReviews);

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Review::getRevieweeId, userId);
            wrapper.eq(Review::getRating, i);
            ratingDistribution.put(i, baseMapper.selectCount(wrapper));
        }
        stats.put("ratingDistribution", ratingDistribution);

        return stats;
    }
}
