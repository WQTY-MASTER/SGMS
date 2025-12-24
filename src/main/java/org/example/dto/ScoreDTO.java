package org.example.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成绩数据传输对象（DTO）
 * 用于封装前端展示的成绩信息，关联学生、课程等表的关键字段
 */
@Data
public class ScoreDTO implements Serializable {

    /**
     * 成绩记录ID（对应score表的id字段）
     */
    private Integer id;

    /**
     * 学生ID（对应score表的student_id字段）
     */
    private Integer studentId;

    /**
     * 学生学号（对应student表的student_no字段）
     */
    private String studentNo;

    /**
     * 学生姓名（对应sys_user表的real_name字段）
     */
    private String studentName;

    /**
     * 课程名称（对应course表的course_name字段）
     */
    private String courseName;

    /**
     * 课程ID（对应score表的course_id字段）
     */
    private Integer courseId;

    /**
     * 分数（对应score表的score字段，保留小数位数由业务决定）
     */
    private BigDecimal score;

    /**
     * 考试时间（对应score表的exam_time字段）
     * 推荐使用LocalDate（仅日期）或LocalDateTime（日期+时间），替代java.util.Date
     * 原因：LocalDate是不可变类，线程安全，API更友好
     */
    private LocalDate examTime;
}