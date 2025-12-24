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
                    config.addAllowedOrigin("http://localhost:8081");
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 放行登录接口（匹配context-path+路径）
                        .requestMatchers("/api/auth/login").permitAll()
                        // 学生接口：匹配/api/score/student/**，要求STUDENT角色
                        .requestMatchers("/api/score/student/**").hasRole("STUDENT")
                        // 教师接口：匹配/api/score/teacher/**，要求TEACHER角色
                        .requestMatchers("/api/score/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        .requestMatchers("/api/students/**").hasRole("TEACHER")
                        .requestMatchers(request -> "OPTIONS".equals(request.getMethod())).permitAll()
                        .anyRequest().authenticated()
                );

        // 过滤器顺序：先执行登录认证过滤器，再执行权限解析过滤器
        // 注意：JwtAuthenticationFilter负责登录生成Token，JwtAuthorizationFilter负责请求解析Token
        http.addFilter(jwtAuthenticationFilter(authenticationManager(http.getSharedObject(AuthenticationConfiguration.class))));
        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}