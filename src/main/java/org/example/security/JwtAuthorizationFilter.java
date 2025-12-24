package org.example.security;

import org.example.utils.JwtUtil;
import org.example.vo.Result;
import com.alibaba.fastjson.JSON;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${jwt.token-header:Authorization}")
    private String tokenHeader;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.info("处理请求：URI={}, Method={}", requestURI, request.getMethod());

        // 放行登录接口和OPTIONS预检请求
        if (requestURI.endsWith("/auth/login") || "OPTIONS".equals(request.getMethod())) {
            logger.debug("放行登录/OPTIONS接口");
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(tokenHeader);
        logger.debug("请求头{}: {}", tokenHeader, authHeader);

        // 非登录接口，校验Token
        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            logger.warn("请求头中无有效Token，URI={}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            // 替换1：Result.fail → Result.unauth()（更贴合401未授权场景）
            out.write(JSON.toJSONString(Result.unauth()));
            out.flush();
            out.close();
            return;
        }

        try {
            String token = authHeader.substring(tokenPrefix.length()).trim();
            logger.debug("提取的Token: {}", token);

            String username = jwtUtil.extractUsername(token);
            logger.debug("从Token中提取的用户名: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("开始验证用户: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 核心：使用JwtUtil的validateToken方法验证
                if (jwtUtil.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.info("用户{}认证成功，权限已设置: {}", username, userDetails.getAuthorities());
                } else {
                    logger.warn("Token验证失败，用户名: {}", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    // 替换2：Result.fail → Result.error（自定义错误消息）
                    out.write(JSON.toJSONString(Result.error("Token已过期或无效")));
                    out.flush();
                    out.close();
                    return;
                }
            }
        } catch (RuntimeException e) {
            logger.error("Token处理失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            // 替换3：Result.fail → Result.error
            out.write(JSON.toJSONString(Result.error("登录凭证解析失败，请重新登录")));
            out.flush();
            out.close();
            return;
        }

        chain.doFilter(request, response);
    }
}