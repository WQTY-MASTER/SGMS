package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dto.StudentOptionDTO;
import org.example.entity.Student;
import org.example.mapper.StudentMapper;
import org.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 学生服务实现类（统一为Integer类型）
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<Student> getAllStudents() {
        return studentMapper.selectList(null);
    }

    @Override
    public Student getStudentByStudentNo(String studentNo) {
        return studentMapper.selectByStudentNo(studentNo);
    }

    // 核心修改：courseId 从 Long → Integer（与数据库 integer 类型匹配）
    @Override
    public List<Student> getStudentsByCourseId(Integer courseId) {
        return studentMapper.selectByCourseId(courseId);
    }

    // 新增：根据课程ID查询学生下拉选项列表（实现接口中的新增方法）
    @Override
    public List<StudentOptionDTO> getStudentOptionsByCourseId(Integer courseId) {
        return studentMapper.selectOptionsByCourseId(courseId);
    }
}