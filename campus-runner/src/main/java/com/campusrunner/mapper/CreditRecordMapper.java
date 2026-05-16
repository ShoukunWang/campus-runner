package com.campusrunner.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {

    default Page<CreditRecord> selectByUserId(Page<CreditRecord> page, String userId) {
        return selectPage(page, new LambdaQueryWrapper<CreditRecord>()
                .eq(CreditRecord::getUserId, userId)
                .orderByDesc(CreditRecord::getCreateTime));
    }
}