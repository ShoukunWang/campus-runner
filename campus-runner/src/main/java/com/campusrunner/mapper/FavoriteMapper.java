package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusrunner.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND task_id = #{taskId} AND is_deleted = 0")
    Favorite selectByUserAndTask(@Param("userId") String userId, @Param("taskId") String taskId);

    @Select("SELECT COUNT(*) FROM favorite WHERE task_id = #{taskId} AND is_deleted = 0")
    int countByTaskId(@Param("taskId") String taskId);
}
