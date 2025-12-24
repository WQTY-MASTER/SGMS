package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Student;
import java.util.List;

/**
 * 学生服务接口（统一为Integer类型）
 */
public interface StudentService extends IService<Student> {
    List<Student> getAllStudents();
    Student getStudentByStudentNo(String studentNo);
    // 核心修改：courseId 从 Long → Integer
    List<Student> getStudentsByCourseId(Integer courseId);
}