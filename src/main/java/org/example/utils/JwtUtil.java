package org.example.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类（确保生成/验证密钥一致，解决"无效签名"问题）
 */
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // 从配置文件读取密钥（必须确保application.yml中配置的密钥≥32字符）
    @Value("${jwt.secret:default-secret-key-123456789012345678901234567890}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private long expiration; // 单位：毫秒

    // 生成签名密钥（核心：统一编码格式，避免密钥乱码导致签名不一致）
    private SecretKey getSigningKey() {
        // 强制使用UTF-8编码，避免不同环境下getBytes()编码不一致
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        // 密钥长度校验（HS256要求≥256位=32字节）
        if (keyBytes.length < 32) {
            String errorMsg = String.format("JWT密钥长度不足！当前%s字节，需至少32字节（256位），请修改application.yml的jwt.secret配置", keyBytes.length);
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 生成Token（适配Spring Security的ROLE_前缀，确保权限匹配）
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        // 关键：角色必须拼接ROLE_前缀，否则hasRole()校验失败
        claims.put("role", "ROLE_" + role.toUpperCase()); // 统一大写，避免大小写问题
        try {
            return Jwts.builder()
                    .claims(claims)          // 设置自定义Claims
                    .subject(username)       // 用户名作为subject
                    .issuedAt(new Date())    // 签发时间
                    .expiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 签名算法+密钥
                    .compact();
        } catch (Exception e) {
            logger.error("Token生成失败：{}", e.getMessage(), e);
            throw new RuntimeException("Token生成失败：" + e.getMessage(), e);
        }
    }

    // 提取用户名（兼容Bearer前缀）
    public String extractUsername(String token) {
        // 移除Bearer前缀（如果有）
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return extractClaim(cleanToken, Claims::getSubject);
    }

    // 提取角色（快速获取权限，适配过滤器）
    public String extractRole(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        Claims claims = extractAllClaims(cleanToken);
        return claims.get("role", String.class);
    }

    // 提取过期时间
    public Date extractExpiration(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return extractClaim(cleanToken, Claims::getExpiration);
    }

    // 通用提取Claim方法
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 提取所有Claims（核心：容错处理，解决"无效签名"）
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSigningKey()) // 用统一的密钥解析
                    .build()
                    .parseSignedClaims(token) // 解析带签名的Token
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.error("Token已过期：{}", e.getMessage());
            throw new RuntimeException("Token已过期", e);
        } catch (SignatureException e) {
            logger.error("Token签名无效（密钥不一致/Token被篡改）：{}", e.getMessage());
            throw new RuntimeException("Token签名无效，请重新登录", e);
        } catch (MalformedJwtException e) {
            logger.error("Token格式错误：{}", e.getMessage());
            throw new RuntimeException("Token格式错误", e);
        } catch (IllegalArgumentException e) {
            logger.error("Token为空或解析失败：{}", e.getMessage());
            throw new RuntimeException("Token解析失败", e);
        } catch (Exception e) {
            logger.error("Token解析异常：{}", e.getMessage(), e);
            throw new RuntimeException("Token解析异常", e);
        }
    }

    // 验证Token有效性（用户名+过期时间+签名）
    public boolean validateToken(String token, String username) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        try {
            final String extractedUsername = extractUsername(cleanToken);
            final boolean isUsernameMatch = extractedUsername.equals(username);
            final boolean isNotExpired = !isTokenExpired(cleanToken);
            return isUsernameMatch && isNotExpired;
        } catch (Exception e) {
            logger.error("Token验证失败：{}", e.getMessage());
            return false;
        }
    }

    // 检查Token是否过期
    public boolean isTokenExpired(String token) {
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return extractExpiration(cleanToken).before(new Date());
    }
}