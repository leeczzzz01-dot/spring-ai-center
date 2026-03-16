package agent.ai.api.interceptor;

import agent.ai.api.pojo.po.User;
import agent.ai.api.utils.ThreadContext;
import agent.ai.api.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头中获取 token (假设 header 名字叫 Authorization 或 token)
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = request.getHeader("token");
        }
        if (StringUtils.hasText(token)) {
            // 2. 解析 Token 直接还原 User 对象，无需查库，提高性能
            User user = TokenUtil.getUserFromToken(token);
            if (user != null) {
                ThreadContext.set(user);
                return true;
            }else{
                ThreadContext.set(null);
                return true;
            }
        }else{
            ThreadContext.set(null);
            return true;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadContext.remove();
    }
}