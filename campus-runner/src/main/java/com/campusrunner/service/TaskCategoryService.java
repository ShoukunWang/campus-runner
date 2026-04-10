package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.TaskCategory;

import java.util.List;

public interface TaskCategoryService extends IService<TaskCategory> {

    List<TaskCategory> getEnabledCategories();
}
