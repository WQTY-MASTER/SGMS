package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    // 数据库id是integer(PK)，对应Java Integer
    @TableId(type = IdType.AUTO)
    private Integer id;
    // 数据库student_id是integer，对应Java Integer
    private Integer studentId;
    // 数据库course_id是integer，对应Java Integer
    private Integer courseId;
    // 数据库score是numeric(5,1)，对应Java BigDecimal（已匹配）
    private BigDecimal score;
    // 数据库exam_time是date，对应Java LocalDate（已匹配）
    private LocalDate examTime;
    // 数据库create_time是timestamp without time zone，对应Java LocalDateTime（已匹配）
    private LocalDateTime createTime;
}