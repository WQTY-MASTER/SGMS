package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dto.StudentOptionDTO;
import org.example.entity.Student;
import java.util.List;

/**
 * 学生服务接口（统一为Integer类型，适配PostgreSQL integer字段，新增全量学生选项查询）
 */
public interface StudentService extends IService<Student> {
    /**
     * 获取所有学生列表
     * @return 全量学生实体列表
     */
    List<Student> getAllStudents();

    /**
     * 根据学号查询学生
     * @param studentNo 学生学号
     * @return 学生实体（无则返回null）
     */
    Student getStudentByStudentNo(String studentNo);

    /**
     * 根据课程ID查询学生列表
     * 核心修改：courseId 从 Long → Integer（适配数据库integer类型）
     * @param courseId 课程ID（Integer类型，非负有效）
     * @return 指定课程下的学生列表
     */
    List<Student> getStudentsByCourseId(Integer courseId);

    /**
     * 根据课程ID查询学生下拉选项列表（含ID/学号/姓名）
     * @param courseId 课程ID
     * @return 学生选项DTO列表（适配前端下拉框）
     */
    List<StudentOptionDTO> getStudentOptionsByCourseId(Integer courseId);

    /**
     * 查询全部学生下拉选项列表（无课程筛选）
     * @return 全量学生选项DTO列表（适配前端“全部学生”下拉场景）
     */
    List<StudentOptionDTO> getAllStudentOptions();
}