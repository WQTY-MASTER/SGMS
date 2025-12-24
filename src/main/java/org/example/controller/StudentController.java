package org.example.controller;

import org.example.entity.Student;
import org.example.service.StudentService;
import org.example.vo.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生控制器（修复类型不匹配 + 优化注入方式）
 */
@RestController
@RequestMapping("/students")
public class StudentController {

    // 核心优化1：替换@Autowired字段注入 → 构造器注入（消除"不建议使用字段注入"警告）
    private final StudentService studentService;

    // 构造器注入（Spring推荐方式，符合规范）
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 根据课程ID查询学生列表
     * 访问路径：http://localhost:8080/api/students/course/{courseId}
     * 核心修复：Long courseId → Integer courseId（匹配StudentService + 数据库integer类型）
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<List<Student>> getStudentsByCourseId(@PathVariable Integer courseId) {
        try {
            // 空值/合法性校验（增强鲁棒性）
            if (courseId == null || courseId <= 0) {
                return Result.error("课程ID无效，请传入正整数");
            }
            // 修复类型不匹配：传入Integer类型参数，匹配StudentService的getStudentsByCourseId(Integer)
            List<Student> students = studentService.getStudentsByCourseId(courseId);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("获取学生列表失败：" + e.getMessage());
        }
    }
}