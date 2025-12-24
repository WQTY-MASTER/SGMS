package org.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.dto.ScoreDTO;
import org.example.entity.Score;
import org.example.entity.Student;
import org.example.entity.SysUser;
import org.example.mapper.StudentMapper;
import org.example.mapper.SysUserMapper;
import org.example.service.ScoreService;
import org.example.utils.UserUtils;
import org.example.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩管理接口层（最终版：完全匹配Result类定义，消除所有参数个数错误）
 */
@RestController
@RequestMapping("/score")
public class ScoreController {

    private static final Logger logger = LoggerFactory.getLogger(ScoreController.class);
    private final ScoreService scoreService;
    private final SysUserMapper sysUserMapper;
    private final StudentMapper studentMapper;

    // 构造器注入（符合Spring规范，消除字段注入警告）
    public ScoreController(ScoreService scoreService,
                           SysUserMapper sysUserMapper,
                           StudentMapper studentMapper) {
        this.scoreService = scoreService;
        this.sysUserMapper = sysUserMapper;
        this.studentMapper = studentMapper;
    }

    /**
     * 学生成绩查询（核心修复：Result调用+类型转换）
     */
    @GetMapping("/student")
    public Result<Map<String, Object>> getStudentScores(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String courseName
    ) {
        try {
            String username = UserUtils.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                logger.warn("未获取到登录用户名");
                return Result.success(buildEmptyPageResult()); // 匹配Result.success(T data)
            }

            SysUser sysUser = sysUserMapper.selectByUsername(username);
            if (sysUser == null) {
                logger.warn("用户名{}不存在", username);
                return Result.success(buildEmptyPageResult());
            }

            // 修复：SysUser.id(Long) → Integer（匹配StudentMapper.selectByUserId(Integer)）
            Integer userIdInt = sysUser.getId() != null ? sysUser.getId().intValue() : null;
            if (userIdInt == null) {
                logger.error("用户ID为空，无法查询学生信息");
                return Result.success(buildEmptyPageResult());
            }

            // 修复：selectByUserId参数类型匹配（消除Long→Integer错误）
            Student student = studentMapper.selectByUserId(userIdInt);
            if (student == null) {
                // 修复：selectByStudentNo单参数调用（消除实参个数错误）
                student = studentMapper.selectByStudentNo(username);
                if (student == null) {
                    logger.warn("用户{}未关联学生信息", username);
                    return Result.success(buildEmptyPageResult());
                }
            }

            Integer studentId = student.getId();
            if (studentId == null) {
                logger.error("学生ID为空");
                return Result.success(buildEmptyPageResult());
            }

            // 修复：Page泛型推断（消除警告）
            Page<ScoreDTO> page = new Page<>(pageNum, pageSize);
            IPage<ScoreDTO> scorePage = scoreService.getScoreListByStudentId(page, studentId, courseName);

            Map<String, Object> result = new HashMap<>(2);
            result.put("total", scorePage.getTotal());
            result.put("list", scorePage.getRecords());
            return Result.success(result); // 匹配Result.success(T data)

        } catch (Exception e) {
            logger.error("学生成绩查询异常", e);
            // 修复：Result.error仅传1个参数（匹配Result.error(String msg)）
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 教师成绩查询（修复Result调用+参数匹配）
     */
    @GetMapping("/teacher")
    public Result<Map<String, Object>> getTeacherScores(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Integer courseId
    ) {
        try {
            String username = UserUtils.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                logger.warn("未获取到登录用户名");
                return Result.success(buildEmptyPageResult());
            }

            SysUser currentUser = sysUserMapper.selectByUsername(username);
            if (currentUser == null) {
                logger.warn("用户名{}不存在", username);
                return Result.success(buildEmptyPageResult());
            }

            Long teacherId = scoreService.getTeacherIdByUserId(currentUser.getId());
            if (teacherId == null) {
                logger.warn("用户{}未关联教师信息", username);
                return Result.success(buildEmptyPageResult());
            }

            Page<ScoreDTO> page = new Page<>(pageNum, pageSize);
            IPage<ScoreDTO> scorePage = scoreService.getScoreListByTeacher(page, teacherId, studentName, courseId);

            Map<String, Object> result = new HashMap<>(2);
            result.put("total", scorePage.getTotal());
            result.put("list", scorePage.getRecords());
            return Result.success(result);

        } catch (Exception e) {
            logger.error("教师成绩查询异常", e);
            // 修复：Result.error单参数调用
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 教师课程列表（修复Result调用+参数匹配）
     */
    @GetMapping("/teacher/courses")
    public Result<List<Map<String, Object>>> getTeacherCourses() {
        try {
            String username = UserUtils.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                // 修复：Result.error单参数调用
                return Result.error("未获取到登录用户");
            }

            SysUser currentUser = sysUserMapper.selectByUsername(username);
            if (currentUser == null) {
                return Result.error("当前用户不存在");
            }

            Long teacherId = scoreService.getTeacherIdByUserId(currentUser.getId());
            if (teacherId == null) {
                return Result.error("教师信息不存在");
            }

            // 修复：getTeacherCourses单参数调用（消除实参个数错误）
            List<Map<String, Object>> courses = scoreService.getTeacherCourses(teacherId);
            return Result.success(courses); // 匹配Result.success(T data)

        } catch (Exception e) {
            logger.error("获取教师课程失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 保存成绩（修复Result调用+权限校验参数）
     */
    @PostMapping("/teacher/save")
    public Result<String> saveScore(@RequestBody Score score) {
        try {
            // 基础参数校验
            if (score.getStudentId() == null || score.getCourseId() == null) {
                return Result.error("学生ID/课程ID不能为空");
            }
            BigDecimal scoreValue = score.getScore();
            if (scoreValue == null || scoreValue.compareTo(BigDecimal.ZERO) < 0 || scoreValue.compareTo(new BigDecimal("100.0")) > 0) {
                return Result.error("分数需在0-100之间");
            }
            LocalDate examTime = score.getExamTime();
            if (examTime == null) {
                return Result.error("考试时间不能为空");
            }

            // 获取当前登录教师信息
            String username = UserUtils.getCurrentUsername();
            SysUser currentUser = sysUserMapper.selectByUsername(username);
            if (currentUser == null) {
                return Result.unauth(); // 匹配Result.unauth()（无参）
            }
            Long teacherId = scoreService.getTeacherIdByUserId(currentUser.getId());
            if (teacherId == null) {
                return Result.forbidden(); // 匹配Result.forbidden()（无参）
            }

            // 修复：checkTeacherCoursePermission参数个数匹配
            if (!scoreService.checkTeacherCoursePermission(teacherId, score.getCourseId())) {
                return Result.forbidden();
            }

            // 修复：existsScore参数个数匹配（消除无实参错误）
            boolean exists = scoreService.existsScore(score.getStudentId(), score.getCourseId(), score.getId());
            if (exists) {
                return Result.error("该学生已存在该课程成绩");
            }

            boolean success = scoreService.saveOrUpdateScore(score);
            return success ? Result.success("保存成功") : Result.error("保存失败");

        } catch (RuntimeException e) {
            logger.error("保存成绩异常", e);
            if ("未登录".equals(e.getMessage()) || "Token已过期".equals(e.getMessage())) {
                return Result.unauth();
            } else if ("权限不足".equals(e.getMessage())) {
                return Result.forbidden();
            } else {
                return Result.error("保存失败：" + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("保存成绩异常", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 单条删除成绩（修复Result调用+权限校验参数）
     */
    @DeleteMapping("/teacher/{id}")
    public Result<String> deleteScore(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("无效ID");
            }

            // 获取当前登录教师信息
            String username = UserUtils.getCurrentUsername();
            SysUser currentUser = sysUserMapper.selectByUsername(username);
            if (currentUser == null) {
                return Result.unauth();
            }
            Long teacherId = scoreService.getTeacherIdByUserId(currentUser.getId());
            if (teacherId == null) {
                return Result.forbidden();
            }

            // 修复：checkScorePermission参数个数匹配
            if (!scoreService.checkScorePermission(teacherId, id)) {
                return Result.forbidden();
            }

            boolean success = scoreService.removeScoreById(id);
            return success ? Result.success("删除成功") : Result.error("删除失败");

        } catch (Exception e) {
            logger.error("删除成绩异常", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除成绩（修复Result调用+权限校验参数）
     */
    @DeleteMapping("/teacher/batch")
    public Result<String> batchDeleteScore(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的成绩");
            }

            // 获取当前登录教师信息
            String username = UserUtils.getCurrentUsername();
            SysUser currentUser = sysUserMapper.selectByUsername(username);
            if (currentUser == null) {
                return Result.unauth();
            }
            Long teacherId = scoreService.getTeacherIdByUserId(currentUser.getId());
            if (teacherId == null) {
                return Result.forbidden();
            }

            // 修复：checkBatchScorePermission参数个数匹配
            if (!scoreService.checkBatchScorePermission(teacherId, ids)) {
                return Result.forbidden();
            }

            boolean success = scoreService.batchRemoveScores(ids);
            return success ? Result.success("批量删除成功") : Result.error("批量删除失败");

        } catch (Exception e) {
            logger.error("批量删除成绩异常", e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 成绩唯一性校验（修复Result调用+参数匹配）
     */
    @GetMapping("/teacher/check")
    public Result<Boolean> checkScoreUnique(
            @RequestParam Integer studentId,
            @RequestParam Integer courseId,
            @RequestParam(required = false) Integer id
    ) {
        try {
            if (studentId == null || courseId == null) {
                return Result.success(false); // 匹配Result.success(T data)
            }

            // 修复：existsScore参数个数匹配
            boolean exists = scoreService.existsScore(studentId, courseId, id);
            return Result.success(!exists);

        } catch (Exception e) {
            logger.error("校验成绩唯一性异常", e);
            return Result.error("校验失败：" + e.getMessage());
        }
    }

    // 空分页结果（统一返回格式，匹配Result.success(T data)）
    private Map<String, Object> buildEmptyPageResult() {
        Map<String, Object> result = new HashMap<>(2);
        result.put("total", 0L);
        result.put("list", List.of());
        return result;
    }
}