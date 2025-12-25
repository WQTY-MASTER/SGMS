package org.example.controller;

import org.example.dto.ScoreDTO;
import org.example.dto.StudentOptionDTO;
import org.example.dto.TeacherInfoDTO;
import org.example.entity.Student;
import org.example.entity.SysUser;
import org.example.mapper.SysUserMapper;
import org.example.service.ScoreService;
import org.example.service.StudentService;
import org.example.service.TeacherService;
import org.example.utils.UserUtils;
import org.example.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 教师管理接口层（最终版：全链路适配PostgreSQL integer类型，无类型不匹配错误）
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    // ========== 构造器注入（符合Spring规范，消除字段注入警告） ==========
    private final TeacherService teacherService;
    private final ScoreService scoreService;
    private final StudentService studentService;
    private final SysUserMapper sysUserMapper;

    public TeacherController(TeacherService teacherService,
                             ScoreService scoreService,
                             StudentService studentService,
                             SysUserMapper sysUserMapper) {
        this.teacherService = teacherService;
        this.scoreService = scoreService;
        this.studentService = studentService;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 查询课程成绩列表
     */
    @GetMapping("/score/list/{courseId}")
    public Result<List<ScoreDTO>> getScoreList(@PathVariable Integer courseId) {
        try {
            List<ScoreDTO> scoreList = scoreService.getScoreListByCourseId(courseId);
            return Result.success(scoreList);
        } catch (Exception e) {
            return Result.error("查询成绩列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有学生列表
     */
    @GetMapping("/students")
    public Result<List<Student>> getStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("查询学生列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据课程ID查询学生列表（优化：兼容courseId为空，返回全部学生选项）
     * 适配：courseId为Integer（匹配StudentService + 数据库integer类型）
     */
    @GetMapping("/students-by-course")
    public Result<List<Map<String, Object>>> getStudentsByCourse(@RequestParam(required = false) Integer courseId) {
        try {
            List<StudentOptionDTO> students;
            // courseId为空/无效时返回全部学生，非空时返回对应课程的学生
            if (courseId == null || courseId <= 0) {
                students = studentService.getAllStudentOptions();
            } else {
                students = studentService.getStudentOptionsByCourseId(courseId);
            }

            List<Map<String, Object>> result = new java.util.ArrayList<>();
            if (students != null && !students.isEmpty()) {
                for (StudentOptionDTO student : students) {
                    Map<String, Object> item = new HashMap<>(5);
                    item.put("id", student.getId());
                    item.put("studentId", student.getId());
                    item.put("studentNo", student.getStudentNo());
                    item.put("studentName", student.getStudentName());
                    item.put("label", student.getStudentName());
                    item.put("value", student.getId());
                    result.add(item);
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询课程对应学生失败：" + e.getMessage());
        }
    }

    /**
     * 查询全部学生下拉选项（独立接口，复用性更高）
     */
    @GetMapping("/students/options")
    public Result<List<Map<String, Object>>> getAllStudentOptions() {
        try {
            List<StudentOptionDTO> students = studentService.getAllStudentOptions();
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            if (students != null && !students.isEmpty()) {
                for (StudentOptionDTO student : students) {
                    Map<String, Object> item = new HashMap<>(5);
                    item.put("id", student.getId());
                    item.put("studentId", student.getId());
                    item.put("studentNo", student.getStudentNo());
                    item.put("studentName", student.getStudentName());
                    item.put("label", student.getStudentName());
                    item.put("value", student.getId());
                    result.add(item);
                }
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询学生列表失败：" + e.getMessage());
        }
    }

    /**
     * 成绩唯一性校验接口
     * 适配：studentId/courseId均为Integer（匹配ScoreService + 数据库integer类型）
     */
    @GetMapping("/score/check-unique")
    public Result<Boolean> checkScoreUnique(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId
    ) {
        try {
            if (studentId == null || studentId <= 0 || courseId == null || courseId <= 0) {
                return Result.error("学生ID/课程ID无效");
            }
            boolean exists = scoreService.existsScore(studentId, courseId);
            return Result.success(!exists); // true=无重复，false=已存在
        } catch (Exception e) {
            return Result.error("校验成绩唯一性失败：" + e.getMessage());
        }
    }
}