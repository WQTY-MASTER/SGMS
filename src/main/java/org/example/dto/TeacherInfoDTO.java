package org.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class TeacherInfoDTO {
    private String teacherNo; // 教师工号（teacher表的teacher_no）
    private String realName; // 教师姓名（sys_user表的real_name）
    private String department; // 所属部门（teacher表的department）
    private List<CourseSimpleDTO> courses; // 负责的课程列表（关联course表）
}