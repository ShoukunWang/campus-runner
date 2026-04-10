package com.campusrunner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Review;
import com.campusrunner.entity.User;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.ReviewService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/submit")
    public Result<Review> submitReview(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String reviewerId = (String) request.getAttribute("userId");
            String taskId = (String) params.get("taskId");
            Integer rating = (Integer) params.get("rating");
            String content = (String) params.get("content");
            List<String> tags = (List<String>) params.get("tags");
            Boolean isAnonymous = (Boolean) params.get("isAnonymous");

            if (taskId == null || rating == null) {
                return Result.error(400, "任务ID和评分不能为空");
            }

            Review review = reviewService.submitReview(taskId, reviewerId, rating, content, tags, isAnonymous);
            return Result.success("评价成功", review);
        } catch (RuntimeException e) {
            return Result.error(3001, e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getReviewList(
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer reviewType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Review> page = reviewService.getReviewList(taskId, userId, reviewType, pageNum, pageSize);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Review review : page.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("reviewId", review.getId());
            item.put("taskId", review.getTaskId());
            item.put("rating", review.getRating());
            item.put("content", review.getContent());
            item.put("tags", review.getTags() != null ? review.getTags().split(",") : null);
            item.put("isAnonymous", review.getIsAnonymous());
            item.put("createTime", review.getCreateTime());

            User reviewer = userMapper.selectById(review.getReviewerId());
            if (reviewer != null) {
                Map<String, Object> reviewerInfo = new HashMap<>();
                reviewerInfo.put("userId", reviewer.getId());
                if (review.getIsAnonymous() == 1) {
                    reviewerInfo.put("nickname", "匿名用户");
                } else {
                    reviewerInfo.put("nickname", reviewer.getNickname());
                    reviewerInfo.put("avatar", reviewer.getAvatar());
                }
                item.put("reviewer", reviewerInfo);
            }

            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        result.put("list", list);

        if (userId != null) {
            Map<String, Object> stats = reviewService.getUserRatingStats(userId);
            result.putAll(stats);
        }

        return Result.success(result);
    }

    @GetMapping("/stats/{userId}")
    public Result<Map<String, Object>> getUserRatingStats(@PathVariable String userId) {
        Map<String, Object> stats = reviewService.getUserRatingStats(userId);
        return Result.success(stats);
    }
}
