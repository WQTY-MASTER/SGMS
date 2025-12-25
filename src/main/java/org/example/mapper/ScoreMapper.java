package org.example.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.dto.ScoreDTO;
import org.example.dto.ScoreSegmentCountDTO; // 新增：导入成绩分段统计DTO
import org.example.entity.Score;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 成绩Mapper接口（适配PostgreSQL integer类型）
 */
public interface ScoreMapper extends BaseMapper<Score> {

    // 原有方法（修改参数类型为Integer）
    List<ScoreDTO> selectScoreListByStudentIdAndCourseName(
            @Param("studentId") Integer studentId,
            @Param("courseName") String courseName
    );

    List<ScoreDTO> selectScoreListByCourseId(@Param("courseId") Integer courseId);

    List<ScoreDTO> selectScoreListByStudentId(@Param("studentId") Integer studentId);

    List<ScoreDTO> selectScoreListByTeacher(
            @Param("teacherId") Long teacherId,
            @Param("studentName") String studentName,
            @Param("courseId") Integer courseId
    );

    // ========== 新增分页查询方法（修改参数类型为Integer） ==========
    /**
     * 学生成绩分页查询（按student_id+课程名模糊搜索）
     */
    IPage<ScoreDTO> selectScoreListByStudentIdAndCourseNamePage(
            Page<ScoreDTO> page,
            @Param("studentId") Integer studentId,
            @Param("courseName") String courseName
    );

    /**
     * 教师成绩分页查询（支持学生姓名、课程ID筛选）
     */
    IPage<ScoreDTO> selectScoreListByTeacherPage(
            Page<ScoreDTO> page,
            @Param("teacherId") Long teacherId,
            @Param("studentName") String studentName,
            @Param("courseId") Integer courseId
    );

    // ========== 新增成绩分段统计方法 ==========
    /**
     * 查询课程成绩各分数段人数
     * @param courseId 课程ID
     * @return 各分数段计数DTO
     */
    ScoreSegmentCountDTO selectScoreSegmentCounts(@Param("courseId") Integer courseId);
}