package agent.admin.api.utils;

import agent.admin.api.pojo.po.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

/**
 * @description: Token 签发与解析工具类 (AdminCenter 模块) - 使用 Jackson 优化对象存取
 */
public class TokenUtil {

    private static final String SECRET_KEY = "admin_secret_key_here";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 根据 User 对象创建 Token (整体序列化存储)
     */
    public static String createToken(User user) {
        // 将对象转为 Map，Jackson 会根据 @JsonIgnore 自动排除密码字段
        Map<String, Object> payload = OBJECT_MAPPER.convertValue(user, Map.class);
        
        return JWT.create()
                .withPayload(payload)
                .withExpiresAt(new java.util.Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    /**
     * 直接从 Token 中还原 User 对象
     */
    public static User getUserFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token);
            
            // 获取所有 Claims 并转为 Map
            Map<String, Object> claims = jwt.getClaims().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().as(Object.class)
                    ));
            
            // 将 Map 转回 User 对象
            return OBJECT_MAPPER.convertValue(claims, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getUserIdFromToken(String token) {
        User user = getUserFromToken(token);
        return user != null ? user.getId() : null;
    }

    public static String getUsernameFromToken(String token) {
        User user = getUserFromToken(token);
        return user != null ? user.getUsername() : null;
    }
}
