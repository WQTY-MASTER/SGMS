package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String role; // 角色字段，如"TEACHER"、"STUDENT"、"ADMIN"
    @TableField(value = "real_name")
    private String realName; // 真实姓名字段，与数据库表中的real_name对应
    private Integer status; // 状态：0-禁用，1-启用
    
    // 添加数据库中存在但实体类中缺少的字段
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}