package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dto.ScoreDTO;
import org.example.entity.Course;
import org.example.entity.Score;
import org.example.entity.Teacher;
import org.example.mapper.CourseMapper;
import org.example.mapper.ScoreMapper;
import org.example.mapper.TeacherMapper;
import org.example.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩服务实现类（适配PostgreSQL integer类型）
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
     * 获取教师负责的课程列表
     */
    @Override
    public List<Map<String, Object>> getTeacherCourses(Long teacherId) {
        List<Course> courseList = courseMapper.selectList(new QueryWrapper<Course>().eq("teacher_id", teacherId));
        List<Map<String, Object>> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(courseList)) {
            for (Course course : courseList) {
                Map<String, Object> courseMap = new HashMap<>(2);
                courseMap.put("id", course.getId());
                courseMap.put("courseName", course.getCourseName());
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
}