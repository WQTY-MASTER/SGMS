package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.dto.LoginDTO;
import org.example.dto.RegisterDTO;
import org.example.entity.SysUser;
import org.example.entity.Student;
import org.example.entity.Teacher;
import org.example.mapper.StudentMapper;
import org.example.mapper.SysUserMapper;
import org.example.mapper.TeacherMapper;
import org.example.utils.JwtUtil;
import org.example.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // ========== 统一依赖注入（整合两段代码的注入逻辑，无冗余） ==========
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== 登录接口（保留完整逻辑，无修改） ==========
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        // 1. 用户名密码认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. 获取用户信息
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));

        // 3. 角色标准化处理（去除ROLE_前缀，统一大写）
        String rawRole = user.getRole();
        String normalizedRole = rawRole == null ? "" : rawRole.toUpperCase();
        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring("ROLE_".length());
        }

        // 4. 生成Token（使用标准化后的角色，避免重复拼接ROLE_）
        String token = jwtUtil.generateToken(user.getUsername(), normalizedRole);

        // 5. 返回结果（含标准化角色：STUDENT/TEACHER，前端根据role跳转对应页面）
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("accessToken", token); // 兼容前端多字段读取
        result.put("role", normalizedRole); // 统一返回大写、无前缀的角色
        result.put("username", user.getUsername());

        return Result.success(result);
    }

    // ========== 学生注册接口（整合校验逻辑+修复语法问题） ==========
    @PostMapping("/register/student")
    @Transactional // 事务注解：确保用户和学生表要么都插入成功，要么都回滚
    public Result<?> registerStudent(@RequestBody RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        String confirmPassword = registerDTO.getConfirmPassword();
        String studentNo = registerDTO.getStudentNo();

        // 参数非空+一致性校验（补充：confirmPassword为null时也提示不一致）
        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        // 优化：confirmPassword为null时直接提示不一致（前端未传该字段的场景）
        if (!password.equals(confirmPassword)) {
            return Result.error("两次输入的密码不一致");
        }
        if (studentNo == null || studentNo.trim().isEmpty()) {
            return Result.error("学号不能为空");
        }

        // 校验用户名是否已存在
        SysUser existingUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 校验学号是否已存在
        Student existingStudent = studentMapper.selectByStudentNo(studentNo);
        if (existingStudent != null) {
            return Result.error("学号已存在");
        }

        // 1. 插入系统用户表（密码加密）
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(passwordEncoder.encode(password)); // 密码加密存储
        sysUser.setRole("STUDENT"); // 角色标记为学生
        sysUser.setRealName(registerDTO.getRealName() != null ? registerDTO.getRealName() : username);
        sysUser.setStatus(1); // 启用状态
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(sysUser);

        // 校验用户ID是否生成成功（统一为Long类型，避免类型转换问题）
        Long userId = sysUser.getId();
        if (userId == null) {
            return Result.error("注册失败，请稍后重试");
        }

        // 2. 插入学生表（关联用户ID）
        Student student = new Student();
        student.setUserId(userId.intValue()); // 若Student的userId是Integer，转int；否则直接用userId
        student.setStudentNo(studentNo);
        studentMapper.insert(student);

        return Result.success("注册成功");
    }

    // ========== 教师注册接口（整合逻辑+统一字段处理） ==========
    @PostMapping("/register/teacher")
    @Transactional // 事务注解：确保用户和教师表要么都插入成功，要么都回滚
    public Result<?> registerTeacher(@RequestBody RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        String confirmPassword = registerDTO.getConfirmPassword();
        String teacherNo = registerDTO.getTeacherNo();

        // 参数非空+一致性校验
        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (!password.equals(confirmPassword)) {
            return Result.error("两次输入的密码不一致");
        }
        if (teacherNo == null || teacherNo.trim().isEmpty()) {
            return Result.error("工号不能为空");
        }

        // 校验用户名是否已存在
        SysUser existingUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 1. 插入系统用户表（密码加密）
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(passwordEncoder.encode(password)); // 密码加密存储
        sysUser.setRole("TEACHER"); // 角色标记为教师
        sysUser.setRealName(registerDTO.getRealName() != null ? registerDTO.getRealName() : username);
        sysUser.setStatus(1); // 启用状态
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(sysUser);

        // 校验用户ID是否生成成功
        Long userId = sysUser.getId();
        if (userId == null) {
            return Result.error("注册失败，请稍后重试");
        }

        // 2. 插入教师表（关联用户ID）
        Teacher teacher = new Teacher();
        teacher.setUserId(userId);
        teacher.setTeacherNo(teacherNo);
        teacher.setPhone(registerDTO.getPhone()); // 可选字段：教师手机号
        teacherMapper.insert(teacher);

        return Result.success("注册成功");
    }
}