package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * 注册请求DTO（兼容前端多种字段命名方式，适配学生/教师注册场景）
 */
@Data
public class RegisterDTO {
    // 通用字段
    private String username;        // 用户名
    private String password;        // 密码
    private String confirmPassword; // 确认密码
    private String role;            // 角色（STUDENT/TEACHER）
    private String realName;        // 真实姓名

    // 学生专属字段：兼容前端下划线/驼峰/缩写等命名方式
    @JsonAlias({"student_no", "studentId", "student_id", "studentNumber", "studentNum"})
    private String studentNo;       // 学号

    // 教师专属字段：兼容前端多种命名方式
    @JsonAlias({"teacher_no", "teacherId", "teacher_id", "teacherNumber", "teacherNum"})
    private String teacherNo;       // 工号

    // 教师可选字段
    private String phone;           // 手机号
}