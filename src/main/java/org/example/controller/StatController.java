package org.example.controller;

import org.example.entity.SysUser;
import org.example.service.ScoreService;
import org.example.mapper.SysUserMapper;
import org.example.utils.UserUtils;
import org.example.vo.Result;
import org.example.vo.ScoreSegmentStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计相关接口
 */
@RestController
@RequestMapping("/stat")
public class StatController {

    private static final Logger logger = LoggerFactory.getLogger(StatController.class);
    private final ScoreService scoreService;
    private final SysUserMapper sysUserMapper;

    public StatController(ScoreService scoreService, SysUserMapper sysUserMapper) {
        this.scoreService = scoreService;
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/score/segment")
    public Result<ScoreSegmentStats> getScoreSegmentStats(@RequestParam Integer courseId) {
        try {
            if (courseId == null) {
                return Result.error("课程ID不能为空");
            }

            String username = UserUtils.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
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

            if (!scoreService.checkTeacherCoursePermission(teacherId, courseId)) {
                return Result.forbidden();
            }

            ScoreSegmentStats stats = scoreService.getScoreSegmentStats(courseId);
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取成绩分段统计失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}