package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dto.ScoreDTO;
import org.example.dto.ScoreSegmentCountDTO; // 新增：成绩分段计数DTO
import org.example.entity.Course;
import org.example.entity.Score;
import org.example.entity.Teacher;
import org.example.mapper.CourseMapper;
import org.example.mapper.ScoreMapper;
import org.example.mapper.TeacherMapper;
import org.example.service.ScoreService;
import org.example.vo.ScoreSegmentDistribution; // 新增：分数段分布VO
import org.example.vo.ScoreSegmentStats; // 新增：成绩分段统计VO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal; // 新增：高精度计算
import java.math.RoundingMode; // 新增：四舍五入模式
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩服务实现类（适配PostgreSQL integer类型 + 前端下拉组件数据格式）
 */
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ScoreMapper scoreMapper;

    /**
     * 学生成绩分页查询（适配Integer类型studentId）
     */
    @Override
    public IPage<ScoreDTO> getScoreListByStudentId(Page<ScoreDTO> page, Integer studentId, String courseName) {
        return scoreMapper.selectScoreListByStudentIdAndCourseNamePage(page, studentId, courseName);
    }

    /**
     * 教师成绩分页查询（适配Integer类型courseId）
     */
    @Override
    public IPage<ScoreDTO> getScoreListByTeacher(Page<ScoreDTO> page, Long teacherId, String studentName, Integer courseId) {
        return scoreMapper.selectScoreListByTeacherPage(page, teacherId, studentName, courseId);
    }

    /**
     * 根据用户ID获取教师ID
     */
    @Override
    public Long getTeacherIdByUserId(Long userId) {
        Teacher teacher = teacherMapper.selectOne(new QueryWrapper<Teacher>().eq("user_id", userId));
        return teacher != null ? teacher.getId() : null;
    }

    /**
     * 获取教师负责的课程列表（扩展字段，适配前端下拉选择器）
     */
    @Override
    public List<Map<String, Object>> getTeacherCourses(Long teacherId) {
        List<Course> courseList = courseMapper.selectList(new QueryWrapper<Course>().eq("teacher_id", teacherId));
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(courseList)) {
            for (Course course : courseList) {
                // 扩展Map容量，新增多字段适配前端下拉组件（label/value/name/text等）
                Map<String, Object> courseMap = new HashMap<>(8);
                courseMap.put("id", course.getId());
                courseMap.put("courseId", course.getId());
                courseMap.put("courseName", course.getCourseName());
                courseMap.put("name", course.getCourseName());
                courseMap.put("text", course.getCourseName());
                courseMap.put("label", course.getCourseName());
                courseMap.put("value", course.getId());
                result.add(courseMap);
            }
        }
        return result;
    }

    /**
     * 保存/更新成绩
     */
    @Override
    public boolean saveOrUpdateScore(Score score) {
        return this.saveOrUpdate(score);
    }

    /**
     * 删除成绩（单条，适配Integer类型id）
     */
    @Override
    public boolean removeScoreById(Integer id) {
        return this.removeById(id);
    }

    /**
     * 批量删除成绩（适配Integer类型ids）
     */
    @Override
    public boolean batchRemoveScores(List<Integer> ids) {
        return this.removeByIds(ids);
    }

    /**
     * 校验成绩唯一性（适配Integer类型参数）
     */
    @Override
    public boolean existsScore(Integer studentId, Integer courseId, Integer id) {
        QueryWrapper<Score> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId)
                .eq("course_id", courseId);
        if (id != null) {
            wrapper.ne("id", id); // 编辑时排除自身
        }
        return this.count(wrapper) > 0;
    }

    /**
     * 根据课程ID查询成绩列表（适配Integer类型courseId）
     */
    @Override
    public List<ScoreDTO> getScoreListByCourseId(Integer courseId) {
        return scoreMapper.selectScoreListByCourseId(courseId);
    }

    /**
     * 根据学生ID查询成绩列表（适配Integer类型studentId）
     */
    @Override
    public List<ScoreDTO> getScoreListByStudentId(Integer studentId) {
        return scoreMapper.selectScoreListByStudentId(studentId);
    }

    // ========== 权限校验方法（适配Integer类型） ==========
    /**
     * 校验教师是否有该课程的操作权限
     */
    @Override
    public boolean checkTeacherCoursePermission(Long teacherId, Integer courseId) {
        if (teacherId == null || courseId == null) {
            return false;
        }
        Course course = courseMapper.selectById(courseId);
        return course != null && teacherId.equals(course.getTeacherId());
    }

    /**
     * 校验教师是否有该成绩的删除权限
     */
    @Override
    public boolean checkScorePermission(Long teacherId, Integer scoreId) {
        if (teacherId == null || scoreId == null) {
            return false;
        }
        // 1. 查询成绩信息
        Score score = this.getById(scoreId);
        if (score == null) {
            return false;
        }
        // 2. 查询成绩所属课程
        Course course = courseMapper.selectById(score.getCourseId());
        // 3. 校验课程所属教师是否匹配
        return course != null && teacherId.equals(course.getTeacherId());
    }

    /**
     * 批量校验成绩权限
     */
    @Override
    public boolean checkBatchScorePermission(Long teacherId, List<Integer> scoreIds) {
        if (teacherId == null || CollectionUtils.isEmpty(scoreIds)) {
            return false;
        }
        // 1. 批量查询成绩信息
        List<Score> scoreList = this.listByIds(scoreIds);
        if (CollectionUtils.isEmpty(scoreList)) {
            return false;
        }
        // 2. 逐个校验课程权限
        for (Score score : scoreList) {
            Course course = courseMapper.selectById(score.getCourseId());
            if (course == null || !teacherId.equals(course.getTeacherId())) {
                return false; // 只要有一个无权限，整体返回无权限
            }
        }
        return true;
    }

    // ========== 新增成绩分段统计方法 ==========
    /**
     * 查询课程成绩各分数段人数计数
     */
    @Override
    public ScoreSegmentCountDTO getScoreSegmentCounts(Integer courseId) {
        return scoreMapper.selectScoreSegmentCounts(courseId);
    }

    /**
     * 查询课程成绩分段统计完整信息（含平均分、最高分、最低分、分数段分布等）
     */
    @Override
    public ScoreSegmentStats getScoreSegmentStats(Integer courseId) {
        // 1. 获取课程下所有成绩数据
        List<ScoreDTO> scores = getScoreListByCourseId(courseId);
        // 2. 获取各分数段人数计数
        ScoreSegmentCountDTO segmentCounts = getScoreSegmentCounts(courseId);

        // 3. 处理各分数段人数（空值兜底）
        long count0To60 = segmentCounts != null && segmentCounts.getCount0To60() != null ? segmentCounts.getCount0To60() : 0L;
        long count60To80 = segmentCounts != null && segmentCounts.getCount60To80() != null ? segmentCounts.getCount60To80() : 0L;
        long count80To100 = segmentCounts != null && segmentCounts.getCount80To100() != null ? segmentCounts.getCount80To100() : 0L;

        // 4. 计算总人数（优先用成绩列表总数，兜底用分数段求和）
        long totalFromScores = scores == null ? 0L : scores.size();
        long total = totalFromScores > 0 ? totalFromScores : count0To60 + count60To80 + count80To100;

        // 5. 计算平均分、最高分、最低分（高精度计算避免浮点误差）
        BigDecimal avgScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        BigDecimal minScore = null;
        long validCount = 0;

        if (scores != null) {
            BigDecimal sum = BigDecimal.ZERO;
            for (ScoreDTO scoreDTO : scores) {
                if (scoreDTO.getScore() == null) {
                    continue;
                }
                BigDecimal score = scoreDTO.getScore();
                sum = sum.add(score);
                validCount++;
                // 更新最高分
                if (maxScore.compareTo(score) < 0) {
                    maxScore = score;
                }
                // 更新最低分
                if (minScore == null || minScore.compareTo(score) > 0) {
                    minScore = score;
                }
            }
            // 计算平均分（保留1位小数，四舍五入）
            if (validCount > 0) {
                avgScore = sum.divide(BigDecimal.valueOf(validCount), 1, RoundingMode.HALF_UP);
            }
        }

        // 最低分空值兜底
        if (minScore == null) {
            minScore = BigDecimal.ZERO;
        }

        // 6. 构建分数段分布列表
        List<ScoreSegmentDistribution> distribution = new ArrayList<>();
        distribution.add(new ScoreSegmentDistribution("0-60", count0To60, formatRate(count0To60, total)));
        distribution.add(new ScoreSegmentDistribution("60-80", count60To80, formatRate(count60To80, total)));
        distribution.add(new ScoreSegmentDistribution("80-100", count80To100, formatRate(count80To100, total)));

        // 7. 封装并返回完整统计结果
        return new ScoreSegmentStats(avgScore, maxScore, minScore, total, distribution);
    }

    /**
     * 私有工具方法：格式化占比（保留1位小数，百分比形式）
     */
    private String formatRate(long count, long total) {
        if (total <= 0) {
            return "0.0%";
        }
        BigDecimal rate = BigDecimal.valueOf(count)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
        return rate + "%";
    }
}