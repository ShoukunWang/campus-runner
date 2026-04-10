package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    @Select("SELECT * FROM task WHERE task_no = #{taskNo} AND is_deleted = 0")
    Task selectByTaskNo(@Param("taskNo") String taskNo);

    @Update("UPDATE task SET view_count = view_count + 1 WHERE id = #{taskId}")
    int incrementViewCount(@Param("taskId") String taskId);

    @Update("UPDATE task SET favorite_count = favorite_count + #{delta} WHERE id = #{taskId}")
    int updateFavoriteCount(@Param("taskId") String taskId, @Param("delta") int delta);

    List<Task> selectTaskListWithPublisher(Page<Task> page, 
                                           @Param("categoryId") String categoryId,
                                           @Param("taskType") Integer taskType,
                                           @Param("minReward") java.math.BigDecimal minReward,
                                           @Param("maxReward") java.math.BigDecimal maxReward,
                                           @Param("area") String area,
                                           @Param("status") Integer status);
}
