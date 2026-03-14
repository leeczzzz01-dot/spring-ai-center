package agent.admin.api.controller;

import agent.admin.api.common.Result;
import agent.admin.api.pojo.po.User;
import agent.admin.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 登录和鉴权入口
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody User loginUser) {
        String token = userService.login(loginUser);
        return Result.success("登录成功", token);
    }
}
