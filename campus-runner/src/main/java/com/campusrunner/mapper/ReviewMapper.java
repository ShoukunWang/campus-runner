package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusrunner.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT * FROM review WHERE task_id = #{taskId} AND reviewer_id = #{reviewerId} AND is_deleted = 0")
    Review selectByTaskAndReviewer(@Param("taskId") String taskId, @Param("reviewerId") String reviewerId);

    @Select("SELECT AVG(rating) FROM review WHERE reviewee_id = #{userId} AND is_deleted = 0")
    Double selectAverageRatingByUserId(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM review WHERE reviewee_id = #{userId} AND is_deleted = 0")
    int countByRevieweeId(@Param("userId") String userId);
}
