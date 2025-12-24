package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.Course;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 课程Mapper接口
 */
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 根据学生ID查询该学生已选课程
     * @param studentId 学生ID
     * @return 课程列表
     */
    @Select("SELECT c.* FROM course c " +
            "JOIN student_course sc ON c.id = sc.course_id " +
            "WHERE sc.student_id = #{studentId}") // 假设存在student_course关联表
    List<Course> selectByStudentId(Long studentId);

    /**
     * 根据教师ID查询该教师负责的课程
     * @param teacherId 教师ID
     * @return 课程列表
     */
    @Select("SELECT * FROM course WHERE teacher_id = #{teacherId}") // 假设course表有teacher_id字段
    List<Course> selectByTeacherId(Long teacherId);
}