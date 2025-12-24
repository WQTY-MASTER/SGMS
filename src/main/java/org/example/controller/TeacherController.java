package org.example.controller;

import org.example.dto.ScoreDTO;
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

/**
 * 教师管理接口层（最终版：全链路适配PostgreSQL integer类型，无类型不匹配错误）
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    // 构造器注入（消除字段注入警告，符合Spring规范）
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
     * 获取当前登录教师的信息（含负责的课程）
     * 适配：SysUser.id(Long) → Integer（安全转换，匹配TeacherService）
     */
    @GetMapping("/info")
    public Result<TeacherInfoDTO> getTeacherInfo() {
        try {
            String username = UserUtils.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                return Result.error("未获取到当前登录用户名");
            }

            SysUser sysUser = sysUserMapper.selectByUsername(username);
            if (sysUser == null) {
                return Result.error("当前用户不存在");
            }

            // 核心适配：SysUser.id是Long → 转换为Integer（数据库user_id是integer，无溢出风险）
            Integer userIdInt = sysUser.getId() != null ? sysUser.getId().intValue() : null;
            if (userIdInt == null) {
                return Result.error("用户ID为空，无法查询教师信息");
            }

            TeacherInfoDTO teacherInfo = teacherService.getTeacherInfoByUserId(userIdInt);
            if (teacherInfo == null) {
                return Result.error("未查询到该用户关联的教师信息");
            }
            return Result.success(teacherInfo);
        } catch (Exception e) {
            return Result.error("查询教师信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据课程ID查询成绩列表（含学生信息）
     * 适配：courseId为Integer（匹配ScoreService + 数据库integer类型）
     */
    @GetMapping("/score/list")
    public Result<List<ScoreDTO>> getScoreList(@RequestParam Integer courseId) {
        try {
            if (courseId == null || courseId <= 0) {
                return Result.error("课程ID无效");
            }
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
     * 根据课程ID查询学生列表
     * 适配：courseId为Integer（匹配StudentService + 数据库integer类型）
     */
    @GetMapping("/students-by-course")
    public Result<List<Student>> getStudentsByCourse(@RequestParam Integer courseId) {
        try {
            if (courseId == null || courseId <= 0) {
                return Result.error("课程ID无效");
            }
            List<Student> students = studentService.getStudentsByCourseId(courseId);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("查询课程对应学生失败：" + e.getMessage());
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