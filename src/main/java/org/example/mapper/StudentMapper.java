package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.Student;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生Mapper接口
 */
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 根据学号查询学生
     * @param studentNo 学生学号
     * @return 学生实体
     */
    @Select("SELECT * FROM student WHERE student_no = #{studentNo}") // 可替换为XML配置
    Student selectByStudentNo(String studentNo);

    /**
     * 根据用户ID查询学生
     * @param userId 用户ID
     * @return 学生实体
     */
    @Select("SELECT * FROM student WHERE user_id = #{userId}")
    // 核心修改：userId 从 Long → Integer（与数据库 integer 类型匹配）
    Student selectByUserId(Integer userId);

    /**
     * 根据课程ID查询学生列表
     * @param courseId 课程ID
     * @return 学生列表
     */
    @Select("SELECT s.* FROM student s JOIN student_course sc ON s.id = sc.student_id WHERE sc.course_id = #{courseId}")
    // 核心修改：courseId 从 Long → Integer（与数据库 integer 类型匹配）
    List<Student> selectByCourseId(Integer courseId);
}