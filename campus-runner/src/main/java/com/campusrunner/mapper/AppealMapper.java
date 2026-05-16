package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Appeal;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppealMapper extends BaseMapper<Appeal> {

    default Page<Appeal> selectByApplicantId(Page<Appeal> page, String applicantId) {
        return selectPage(page, new LambdaQueryWrapper<Appeal>()
                .eq(Appeal::getApplicantId, applicantId)
                .orderByDesc(Appeal::getCreateTime));
    }
}