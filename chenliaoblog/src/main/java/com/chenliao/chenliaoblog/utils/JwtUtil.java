package com.chenliao.chenliaoblog.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtil {
    /**
     * 核心秘钥（重要：请替换为你项目的专属秘钥，建议至少32位，不要硬编码在代码中）
     */
    private static final String SECRET_KEY = "chenliao-myblog-jwt-secret-key-20260212-32bit++";

    /**
     * Token过期时间（示例：2小时，单位：毫秒）
     */
    private static final long EXPIRATION_TIME = 1 * 60 * 60 * 1000L;

    /**
     * 生成JWT令牌
     * @param userId 用户ID（自定义载荷信息）
     * @param username 用户名（自定义载荷信息）
     * @param extraClaims 额外的自定义载荷信息（可选）
     * @return 生成的JWT令牌字符串
     */
    public static String generateToken(Long userId, String username, Map<String, Object> extraClaims) {
        // 生成加密密钥
        Key key = getSigningKey();

        // 构建JWT令牌
        return Jwts.builder()
                // 载荷：自定义信息（非敏感信息）
                .setClaims(extraClaims)          // 额外自定义载荷
                .setSubject(username)            // 主题（通常存用户名）
                .claim("userId", userId)         // 自定义claim：用户ID
                // 令牌元信息
                .setIssuedAt(new Date())         // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                // 签名：保证令牌不被篡改
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 重载方法：简化生成token（无额外载荷）
     */
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, Map.of());
    }

    /**
     * 验证JWT令牌的有效性
     * @param token 待验证的令牌
     * @return true-有效，false-无效（过期/签名错误/格式错误）
     */
    public static boolean validateToken(String token) {
        try {
            // 解析token，若解析失败会抛出对应异常
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT令牌格式不支持: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT参数异常: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析JWT令牌，获取载荷中的所有信息
     * @param token JWT令牌
     * @return 载荷Claims对象（包含所有自定义信息）
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 获取令牌剩余过期时间（毫秒）
     * @param token JWT令牌
     * @return 剩余时间（负数表示已过期）
     */
    public static long getRemainingTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * 生成签名密钥（内部使用，保证密钥符合HS256算法要求）
     */
    private static Key getSigningKey() {
        // 将字符串秘钥转换为符合HS256要求的Key对象
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
