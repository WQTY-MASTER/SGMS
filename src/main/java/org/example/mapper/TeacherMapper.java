package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dto.CourseSimpleDTO;
import org.example.dto.TeacherInfoDTO;
import org.example.entity.Teacher;

import java.util.List;

public interface TeacherMapper extends BaseMapper<Teacher> {
    TeacherInfoDTO selectTeacherInfo(Integer userId);

    List<CourseSimpleDTO> selectCoursesByTeacherId(Long id);
}