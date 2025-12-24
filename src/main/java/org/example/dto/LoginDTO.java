package org.example.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username; // 与前端请求的"username"字段匹配
    private String password; // 与前端请求的"password"字段匹配
}