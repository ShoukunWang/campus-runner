package com.campusrunner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.TaskCategory;
import com.campusrunner.mapper.TaskCategoryMapper;
import com.campusrunner.service.TaskCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskCategoryServiceImpl extends ServiceImpl<TaskCategoryMapper, TaskCategory> implements TaskCategoryService {

    @Override
    public List<TaskCategory> getEnabledCategories() {
        LambdaQueryWrapper<TaskCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCategory::getStatus, 1)
               .orderByAsc(TaskCategory::getSortOrder);
        return baseMapper.selectList(wrapper);
    }
}
