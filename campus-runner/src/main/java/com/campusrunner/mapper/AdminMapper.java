package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusrunner.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE username = #{username} AND is_deleted = 0")
    Admin selectByUsername(@Param("username") String username);
}
