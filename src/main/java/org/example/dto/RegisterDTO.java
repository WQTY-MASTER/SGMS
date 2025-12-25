package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;
    private String role;
    private String realName;

    // 新增JSON别名：兼容前端多种字段命名方式（下划线/驼峰/缩写）
    @JsonAlias({"student_no", "studentId", "student_id", "studentNumber", "studentNum"})
    private String studentNo;

    // 新增JSON别名：兼容前端多种字段命名方式
    @JsonAlias({"teacher_no", "teacherId", "teacher_id", "teacherNumber", "teacherNum"})
    private String teacherNo;

    private String phone;
}