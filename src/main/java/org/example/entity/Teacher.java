package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("teacher")
public class Teacher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String teacherNo;
    private String title;
    private String department;
    private String phone;
}