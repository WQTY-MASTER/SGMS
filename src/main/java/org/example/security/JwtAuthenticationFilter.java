package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSON;
import org.example.dto.LoginDTO;
import org.example.entity.SysUser;
import org.example.mapper.SysUserMapper;
import org.example.utils.JwtUtil;
import org.example.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

// 移除 @Component 注解，改为通过 SecurityConfig 中的 @Bean 方法创建
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;

    @Autowired
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, SysUserMapper sysUserMapper) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.sysUserMapper = sysUserMapper;
        // 使用去除context-path的路径
        setFilterProcessesUrl("/auth/login");  // 登录路径（不包含context-path，由Spring自动处理）
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 解析登录请求，获取 LoginDTO
            LoginDTO loginDTO = JSON.parseObject(request.getInputStream(), LoginDTO.class);
            logger.debug("登录请求解析成功: 用户名={}", loginDTO.getUsername());
            // 进行认证
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );
        } catch (IOException e) {
            logger.error("登录请求解析失败: {}", e.getMessage());
            throw new AuthenticationException("登录请求解析失败") {
                @Override
                public Throwable getCause() {
                    return e;
                }
            };
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = authResult.getName();
        logger.debug("认证成功，用户名={}", username);

        // 从数据库中查询用户的实际角色
        SysUser user = sysUserMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUser>()
                .eq("username", username));

        String role = user != null ? user.getRole() : "USER"; // 默认角色为USER
        logger.debug("用户角色: {}", role);
        String token = jwtUtil.generateToken(username, role);
        logger.debug("生成Token: {}", token);

        // 跨域相关：暴露Authorization头，让前端能获取到
        response.setHeader("Access-Control-Expose-Headers", "Authorization, token");
        // 设置JWT令牌头
        response.setHeader("Authorization", "Bearer " + token);
        response.setHeader("token", token);
        // 设置响应格式
        response.setContentType("application/json;charset=utf-8");

        PrintWriter out = response.getWriter();
        // 整合响应数据：包含token、accessToken（兼容前端）、role、username
        out.write(JSON.toJSONString(Result.success(Map.of(
                "token", token,
                "accessToken", token,
                "role", role,
                "username", username
        ))));
        out.flush();
        out.close();
        logger.info("登录成功，用户名={}, 角色={}", username, role);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        logger.error("登录失败: {}", failed.getMessage());
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(JSON.toJSONString(Result.error("用户名或密码错误")));
        out.flush();
        out.close();
    }
}