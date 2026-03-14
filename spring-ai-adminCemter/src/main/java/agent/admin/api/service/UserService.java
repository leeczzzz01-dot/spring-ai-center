package agent.admin.api.service;

import agent.admin.api.pojo.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @description: 用户管理 Service
 */
public interface UserService extends IService<User> {

    /**
     * 校验登录获取 Token
     */
    String login(User loginUser);

    /**
     * 管理员添加新用户
     * @param operatorUsername 操作人的 username，需为 admin
     * @param newUser 待添加用户的信息
     */
    void addUser(String operatorUsername, User newUser);

    /**
     * 获取全部用户列表 (仅限 Admin 查看)
     * @param operatorUsername 操作人的 username，需为 admin
     * @return User 集合
     */
    List<User> getUserList(String operatorUsername);
}
