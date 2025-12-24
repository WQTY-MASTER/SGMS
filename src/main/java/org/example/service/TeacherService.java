package org.example.service;

import org.example.dto.TeacherInfoDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Teacher;

public interface TeacherService extends IService<Teacher> {
    // 根据用户ID查询教师信息（包含负责的课程）
    TeacherInfoDTO getTeacherInfoByUserId(Integer userId);
}