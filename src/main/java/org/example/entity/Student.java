package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.AUTO)
    // 核心修改：id 从 Long → Integer（与数据库 integer 类型匹配）
    private Integer id;
    // 核心修改：userId 从 Long → Integer（与数据库 integer 类型匹配）
    private Integer userId;  // 关联sys_user.id
    private String studentNo;
    private String className;
    private String gender;
    private String phone;
}