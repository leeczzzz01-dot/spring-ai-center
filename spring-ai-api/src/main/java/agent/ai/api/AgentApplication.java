package agent.ai.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        // 1. 启动并获取上下文
        ConfigurableApplicationContext context = SpringApplication.run(AgentApplication.class, args);
        ConfigurableEnvironment env = context.getEnvironment();

        System.out.println("\n========================================================");
        System.out.println("               🚀 多模型 Agent 启动成功！                 ");
        System.out.println("========================================================");

        boolean hasModel = false;

        // 2. 动态遍历所有的配置源 (包括 application.yml, 系统环境变量等)
        for (PropertySource<?> propertySource : env.getPropertySources()) {
            // 只看能遍历的配置源
            if (propertySource instanceof EnumerablePropertySource<?> eps) {
                for (String key : eps.getPropertyNames()) {
                    // 3. 寻找 Spring AI 标准的模型配置 Key
                    if (key.startsWith("spring.ai.") && key.endsWith(".chat.options.model")) {

                        // 提取出厂商的名字 (截取 spring.ai. 和 .chat.options.model 中间的部分)
                        // 比如把 "spring.ai.google.genai.chat.options.model" 变成 "GOOGLE.GENAI"
                        String provider = key.substring(10, key.indexOf(".chat.options")).toUpperCase();
                        String modelName = env.getProperty(key);

                        System.out.println("✅ [" + provider + "] 装配模型 : " + modelName);
                        hasModel = true;
                    }
                }
            }
        }

        if (!hasModel) {
            System.out.println("⚠️ 未在配置中检测到具体的 AI Model 装配");
        }
        System.out.println("========================================================\n");
    }
}