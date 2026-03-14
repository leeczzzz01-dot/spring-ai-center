package agent.admin.api.exception;

import agent.admin.api.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @description: 全局异常处理，用来向前端返回格式化的 Result
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统发生未知异常: ", e);
        // 此处为了安全可以脱敏抛出，内部可自定义业务Exception配合抓取不同提示
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "服务器内部错误");
    }
}
