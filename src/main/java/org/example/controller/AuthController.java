package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.dto.LoginDTO;
import org.example.entity.SysUser;
import org.example.mapper.SysUserMapper;
import org.example.utils.JwtUtil;
import org.example.vo.Result; // 只保留这一个导入，删除其他路径的Result导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // 登录接口（返回Token+角色，供前端跳转）
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

        // 错误：JwtUtil的generateToken需要username和role，不是直接传user对象
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        // 4. 返回结果（含角色：STUDENT/TEACHER，前端根据role跳转对应页面）
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole());
        // 修复：删除不存在的realName字段，或替换为实际存在的字段
        result.put("username", user.getUsername());

        return Result.success(result);
    }
}