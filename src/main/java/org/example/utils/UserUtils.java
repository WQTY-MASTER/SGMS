package org.example.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserUtils {
    // 获取当前登录用户的 username（从 Security 上下文获取，JWT 认证后会自动存入）
    public static String getCurrentUsername() {
        // 从 Security 上下文获取认证信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            // UserDetails 中包含 username
            return ((UserDetails) principal).getUsername();
        }
        // 异常情况：返回 principal 的字符串形式
        return principal.toString();
    }
}