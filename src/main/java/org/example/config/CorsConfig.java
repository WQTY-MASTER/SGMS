package org.example.config; // 包名必须正确，对应路径

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration // 标记为配置类，Spring会自动加载
public class CorsConfig {

    // 定义跨域过滤器Bean，覆盖所有接口
    @Bean
    public CorsFilter corsFilter() {
        // 1. 配置跨域核心参数
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端域名（你的前端运行地址是http://localhost:8081，必须精确匹配）
        config.addAllowedOrigin("http://localhost:8081");
        // 允许携带Cookie（登录后Token通常存在Cookie或请求头中，必须开启）
        config.setAllowCredentials(true);
        // 允许所有HTTP方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");
        // 允许所有请求头（如Authorization、Content-Type等）
        config.addAllowedHeader("*");
        // 预检请求有效期（3600秒=1小时，避免频繁发送OPTIONS请求）
        config.setMaxAge(3600L);

        // 2. 配置跨域生效的路径（/** 表示所有接口都应用该配置）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 所有接口都允许跨域

        // 3. 返回过滤器实例
        return new CorsFilter(source);
    }
}