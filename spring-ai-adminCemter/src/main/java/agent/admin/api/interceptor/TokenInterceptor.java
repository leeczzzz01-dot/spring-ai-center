package agent.admin.api.interceptor;


import agent.admin.api.mapper.UserMapper;
import agent.admin.api.pojo.po.User;
import agent.admin.api.utils.ThreadContext;
import agent.admin.api.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 1. 从请求头中获取 token (假设 header 名字叫 Authorization 或 token)
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = request.getHeader("token");
        }

        if (StringUtils.hasText(token)) {
            // 2. 解析 Token
            Long userId = TokenUtil.getUserIdFromToken(token);
            if (userId != null) {
                // 3. 根据解析出的 userId 从 DB 查出真正的 User，存入 threadContext 中
                User user = userMapper.selectById(userId);
                if (user != null) {
                    ThreadContext.set(user);
                    return true;
                }
            }
        }

        // 返回 401
        response.setStatus(401);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 【关键防御】请求结束后必须清理 ThreadLocal，防止 Tomcat 线程池复用导致内存泄漏和数据串号！
        ThreadContext.remove();
    }
}