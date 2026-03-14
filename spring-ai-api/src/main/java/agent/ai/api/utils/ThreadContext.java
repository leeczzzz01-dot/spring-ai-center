package agent.ai.api.utils;


import agent.ai.api.pojo.po.User;

/**
 * @author lichen
 * @date 2026/3/14
 * @description:
 */
public class ThreadContext {
    public static ThreadLocal<User> threadLocal = new ThreadLocal<>();
    public static void set(User value){
        threadLocal.set(value);
    }
    public static User get(){
        return threadLocal.get();
    }
    public static void remove(){
        threadLocal.remove();
    }
    public static void clear(){
        threadLocal.remove();
    }
}
