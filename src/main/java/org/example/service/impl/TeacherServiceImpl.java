package org.example.service.impl;

import org.example.dto.CourseSimpleDTO;
import org.example.dto.TeacherInfoDTO;
import org.example.entity.Teacher;
import org.example.mapper.TeacherMapper;
import org.example.service.TeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired; // 新增：导入Autowired
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    // 添加@Autowired注解，让Spring自动注入TeacherMapper实例
    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public TeacherInfoDTO getTeacherInfoByUserId(Integer userId) {
        // 1. 查询教师基本信息（关联sys_user表）
        TeacherInfoDTO teacherInfo = teacherMapper.selectTeacherInfo(userId);
        if (teacherInfo == null) {
            return null;
        }
        // 2. 查询教师ID（用于关联课程表）
        Teacher teacher = lambdaQuery().eq(Teacher::getUserId, userId).one();
        if (teacher == null) {
            return teacherInfo;
        }
        // 3. 查询教师负责的课程列表
        List<CourseSimpleDTO> courses = teacherMapper.selectCoursesByTeacherId(teacher.getId());
        teacherInfo.setCourses(courses); // 设置课程列表到DTO中
        return teacherInfo;
    }
}
