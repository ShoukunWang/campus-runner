package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.Review;

import java.util.List;
import java.util.Map;

public interface ReviewService extends IService<Review> {

    Review submitReview(String taskId, String reviewerId, Integer rating, String content, 
                       List<String> tags, Boolean isAnonymous);

    Page<Review> getReviewList(String taskId, String userId, Integer reviewType, 
                              Integer pageNum, Integer pageSize);

    Map<String, Object> getUserRatingStats(String userId);
}
