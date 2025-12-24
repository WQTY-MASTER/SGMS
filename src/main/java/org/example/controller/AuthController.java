package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.dto.LoginDTO;
import org.example.entity.SysUser;
import org.example.mapper.SysUserMapper;
import org.example.utils.JwtUtil;
import org.example.vo.Result;
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
@RequestMapping("/auth") // 核心修正：添加/api前缀，匹配前端请求路径
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
}