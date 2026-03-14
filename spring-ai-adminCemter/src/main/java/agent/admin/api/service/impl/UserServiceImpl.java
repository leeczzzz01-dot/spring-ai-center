package agent.admin.api.service.impl;

import agent.admin.api.mapper.UserMapper;
import agent.admin.api.pojo.po.User;
import agent.admin.api.service.UserService;
import agent.admin.api.utils.TokenUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public String login(User loginUser) {
        if (loginUser == null || loginUser.getUsername() == null || loginUser.getPassword() == null) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginUser.getUsername())
                .eq("password", loginUser.getPassword());
        User user = baseMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码不正确");
        }

        // 返回分发的 JWT Token
        return TokenUtil.createToken(user);
    }

    @Override
    public void addUser(String operatorUsername, User newUser) {
        // 简易鉴权拦截
        if (!"admin".equals(operatorUsername)) {
            throw new RuntimeException("当前账号无权执行该操作");
        }
        if (newUser == null || newUser.getUsername() == null || newUser.getPassword() == null) {
            throw new RuntimeException("传入的新用户信息不完整");
        }

        // 校验名字是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", newUser.getUsername());
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("该用户名已被注册");
        }

        newUser.setCreatedAt(LocalDateTime.now());
        baseMapper.insert(newUser);
    }

    @Override
    public List<User> getUserList(String operatorUsername) {
        if (!"admin".equals(operatorUsername)) {
            throw new RuntimeException("当前账号无权查看用户列表");
        }
        return baseMapper.selectList(null);
    }
}
