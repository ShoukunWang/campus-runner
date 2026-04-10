package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusrunner.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {

    @Select("SELECT * FROM sys_config WHERE config_key = #{key} AND is_deleted = 0")
    SysConfig selectByKey(@Param("key") String key);
}
