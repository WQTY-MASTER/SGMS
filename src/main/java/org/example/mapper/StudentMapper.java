package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dto.StudentOptionDTO;
import org.example.entity.Student;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学生Mapper接口（适配PostgreSQL integer类型，新增全量学生选项查询）
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
     * @param userId 用户ID（Integer类型：匹配PostgreSQL integer字段）
     * @return 学生实体
     */
    @Select("SELECT * FROM student WHERE user_id = #{userId}")
    Student selectByUserId(Integer userId);

    /**
     * 根据课程ID查询学生列表
     * @param courseId 课程ID（Integer类型：匹配PostgreSQL integer字段）
     * @return 学生列表
     */
    @Select("SELECT s.* FROM student s JOIN student_course sc ON s.id = sc.student_id WHERE sc.course_id = #{courseId}")
    List<Student> selectByCourseId(Integer courseId);

    /**
     * 根据课程ID查询学生下拉选项（包含姓名）
     * @param courseId 课程ID
     * @return 学生选项列表（ID/学号/姓名）
     */
    @Select("SELECT s.id, s.student_no AS studentNo, u.real_name AS studentName " +
            "FROM student s " +
            "JOIN student_course sc ON s.id = sc.student_id " +
            "JOIN sys_user u ON s.user_id = u.id " +
            "WHERE sc.course_id = #{courseId}")
    List<StudentOptionDTO> selectOptionsByCourseId(Integer courseId);

    /**
     * 查询全部学生下拉选项（包含姓名）
     * @return 全量学生选项列表（无课程筛选）
     */
    @Select("SELECT s.id, s.student_no AS studentNo, u.real_name AS studentName " +
            "FROM student s " +
            "JOIN sys_user u ON s.user_id = u.id")
    List<StudentOptionDTO> selectAllOptions();
}