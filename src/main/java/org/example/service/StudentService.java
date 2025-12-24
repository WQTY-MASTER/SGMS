package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dto.StudentOptionDTO;
import org.example.entity.Student;
import java.util.List;

/**
 * 学生服务接口（统一为Integer类型）
 */
public interface StudentService extends IService<Student> {
    /**
     * 获取所有学生列表
     */
    List<Student> getAllStudents();

    /**
     * 根据学号查询学生
     * @param studentNo 学生学号
     * @return 学生实体
     */
    Student getStudentByStudentNo(String studentNo);

    /**
     * 根据课程ID查询学生列表
     * 核心修改：courseId 从 Long → Integer（适配数据库integer类型）
     * @param courseId 课程ID
     * @return 学生列表
     */
    List<Student> getStudentsByCourseId(Integer courseId);

    /**
     * 根据课程ID查询学生下拉选项列表
     * @param courseId 课程ID
     * @return 学生选项DTO列表
     */
    List<StudentOptionDTO> getStudentOptionsByCourseId(Integer courseId);
}