package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 统一接口响应格式：code（状态码）、msg（消息）、data（数据）
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    // 状态码：200=成功，400=请求错误，401=未登录，403=无权限
    private Integer code;
    // 提示消息
    private String msg;
    // 响应数据
    private T data;

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 错误响应（带消息）
    public static <T> Result<T> error(String msg) {
        return new Result<>(400, msg, null);
    }

    // 未登录响应
    public static <T> Result<T> unauth() {
        return new Result<>(401, "未登录或令牌失效", null);
    }

    // 无权限响应
    public static <T> Result<T> forbidden() {
        return new Result<>(403, "权限不足", null);
    }
}