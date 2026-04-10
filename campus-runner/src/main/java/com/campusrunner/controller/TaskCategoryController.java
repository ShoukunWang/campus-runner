package com.campusrunner.controller;

import com.campusrunner.entity.TaskCategory;
import com.campusrunner.service.TaskCategoryService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class TaskCategoryController {

    @Autowired
    private TaskCategoryService taskCategoryService;

    @GetMapping("/list")
    public Result<List<TaskCategory>> list() {
        List<TaskCategory> categories = taskCategoryService.getEnabledCategories();
        return Result.success(categories);
    }
}
