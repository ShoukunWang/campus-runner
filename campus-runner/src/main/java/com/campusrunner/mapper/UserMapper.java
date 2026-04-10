package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusrunner.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username} AND is_deleted = 0")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE phone = #{phone} AND is_deleted = 0")
    User selectByPhone(@Param("phone") String phone);
}
