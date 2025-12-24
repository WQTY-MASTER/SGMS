package org.example.config;

import org.example.security.JwtAuthenticationFilter;
import org.example.security.JwtAuthorizationFilter;
import org.example.utils.JwtUtil;
import org.example.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(authenticationManager, jwtUtil, sysUserMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.addAllowedOrigin("http://localhost:8082"); // 前端端口
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    config.addExposedHeader("Authorization");
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L); // 预检请求缓存1小时
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 放行登录接口（已修正/api前缀）
                        .requestMatchers("/api/auth/login").permitAll()
                        // 放行预检请求（核心：解决OPTIONS请求401）
                        .requestMatchers(request -> "OPTIONS".equals(request.getMethod())).permitAll()
                        // 学生接口权限
                        .requestMatchers("/api/score/student/**").hasRole("STUDENT")
                        // 教师接口权限
                        .requestMatchers("/api/score/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        .requestMatchers("/api/students/**").hasRole("TEACHER")
                        // 其他请求需认证
                        .anyRequest().authenticated()
                );

        // 核心修正：移除JwtAuthenticationFilter（登录逻辑已在AuthController实现，无需重复过滤器）
        // 仅保留JwtAuthorizationFilter解析Token
        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}