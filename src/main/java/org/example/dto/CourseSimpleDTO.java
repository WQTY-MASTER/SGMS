package org.example.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data // 自动生成getter、setter
public class CourseSimpleDTO {
    private Integer id; // 课程ID
    private String courseCode; // 课程编码（对应course表的course_code）
    private String courseName; // 课程名称（对应course表的course_name）
    private BigDecimal credit; // 学分（对应course表的credit）
}