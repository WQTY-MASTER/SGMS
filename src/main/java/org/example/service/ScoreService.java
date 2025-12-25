package org.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dto.ScoreDTO;
import org.example.dto.ScoreSegmentCountDTO; // 新增：成绩分段计数DTO
import org.example.entity.Score;
import org.example.vo.ScoreSegmentStats; // 新增：成绩分段统计VO

import java.util.List;
import java.util.Map;

/**
 * 成绩服务接口（适配PostgreSQL integer类型）
 */
public interface ScoreService extends IService<Score> {

    /**
     * 学生成绩分页查询（按student_id+课程名模糊搜索）
     * @param page 分页对象
     * @param studentId 学生ID（Integer类型，匹配数据库）
     * @param courseName 课程名（模糊匹配）
     * @return 分页结果
     */
    IPage<ScoreDTO> getScoreListByStudentId(Page<ScoreDTO> page, Integer studentId, String courseName);

    /**
     * 教师成绩分页查询（支持学生姓名、课程ID筛选）
     * @param page 分页对象
     * @param teacherId 教师ID（Long类型，用户表主键）
     * @param studentName 学生姓名（模糊匹配）
     * @param courseId 课程ID（Integer类型，匹配数据库）
     * @return 分页结果
     */
    IPage<ScoreDTO> getScoreListByTeacher(Page<ScoreDTO> page, Long teacherId, String studentName, Integer courseId);

    /**
     * 根据用户ID获取教师ID
     * @param userId 用户ID
     * @return 教师ID
     */
    Long getTeacherIdByUserId(Long userId);

    /**
     * 获取教师负责的课程列表
     * @param teacherId 教师ID
     * @return 课程列表（Map: id/courseName）
     */
    List<Map<String, Object>> getTeacherCourses(Long teacherId);

    /**
     * 保存/更新成绩
     * @param score 成绩实体
     * @return 是否成功
     */
    boolean saveOrUpdateScore(Score score);

    /**
     * 删除成绩（单条）
     * @param id 成绩ID（Integer类型，匹配数据库）
     * @return 是否成功
     */
    boolean removeScoreById(Integer id);

    /**
     * 批量删除成绩
     * @param ids 成绩ID列表（Integer类型，匹配数据库）
     * @return 是否成功
     */
    boolean batchRemoveScores(List<Integer> ids);

    /**
     * 校验成绩唯一性（学生+课程维度）
     * @param studentId 学生ID（Integer类型）
     * @param courseId 课程ID（Integer类型）
     * @param id 成绩ID（Integer类型，编辑时排除自身）
     * @return 是否存在重复
     */
    boolean existsScore(Integer studentId, Integer courseId, Integer id);

    /**
     * 重载：校验成绩唯一性（新增场景）
     * @param studentId 学生ID（Integer类型）
     * @param courseId 课程ID（Integer类型）
     * @return 是否存在重复
     */
    default boolean existsScore(Integer studentId, Integer courseId) {
        return existsScore(studentId, courseId, null);
    }

    /**
     * 根据课程ID查询成绩列表（适配TeacherController）
     * @param courseId 课程ID（Integer类型）
     * @return 成绩DTO列表
     */
    List<ScoreDTO> getScoreListByCourseId(Integer courseId);

    /**
     * 根据学生ID查询成绩列表（备用方法）
     * @param studentId 学生ID（Integer类型）
     * @return 成绩DTO列表
     */
    List<ScoreDTO> getScoreListByStudentId(Integer studentId);

    // ========== 权限校验方法（适配Integer类型） ==========
    /**
     * 校验教师是否有该课程的操作权限
     * @param teacherId 教师ID
     * @param courseId 课程ID（Integer类型）
     * @return 是否有权限
     */
    boolean checkTeacherCoursePermission(Long teacherId, Integer courseId);

    /**
     * 校验教师是否有该成绩的删除权限
     * @param teacherId 教师ID
     * @param scoreId 成绩ID（Integer类型）
     * @return 是否有权限
     */
    boolean checkScorePermission(Long teacherId, Integer scoreId);

    /**
     * 批量校验成绩权限
     * @param teacherId 教师ID
     * @param scoreIds 成绩ID列表（Integer类型）
     * @return 是否有权限
     */
    boolean checkBatchScorePermission(Long teacherId, List<Integer> scoreIds);

    // ========== 新增成绩分段统计方法 ==========
    /**
     * 查询课程成绩各分数段人数计数
     * @param courseId 课程ID（Integer类型）
     * @return 各分数段计数DTO
     */
    ScoreSegmentCountDTO getScoreSegmentCounts(Integer courseId);

    /**
     * 查询课程成绩分段统计完整信息（含平均分、最高分、最低分、分数段等）
     * @param courseId 课程ID（Integer类型）
     * @return 成绩分段统计VO
     */
    ScoreSegmentStats getScoreSegmentStats(Integer courseId);
}