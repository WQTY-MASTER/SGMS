package org.example.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;
    private String role;
    private String realName;
    private String studentNo;
    private String teacherNo;
    private String phone;
}