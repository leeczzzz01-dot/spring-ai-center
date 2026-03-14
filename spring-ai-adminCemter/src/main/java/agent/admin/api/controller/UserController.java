package agent.admin.api.controller;

import agent.admin.api.common.Result;
import agent.admin.api.pojo.po.User;
import agent.admin.api.service.UserService;
import agent.admin.api.utils.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 用户管理控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 从请求中提取 username 判断 Admin
     * 可以放在拦截器，如果为了直观，暂时以解析为准
     */
    private String extractUsername(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = request.getHeader("token");
        }
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("访问被拒绝，凭据缺失");
        }
        return TokenUtil.getUsernameFromToken(token);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody User newUser, HttpServletRequest request) {
        String operator = extractUsername(request);
        userService.addUser(operator, newUser);
        return Result.success("添加用户成功", null);
    }

    @GetMapping("/list")
    public Result<List<User>> list(HttpServletRequest request) {
        String operator = extractUsername(request);
        List<User> list = userService.getUserList(operator);
        return Result.success(list);
    }
}
