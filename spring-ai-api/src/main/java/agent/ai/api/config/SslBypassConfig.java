//package agent.ai.api.config;
//
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.netty.http.client.HttpClient;
//import reactor.netty.transport.ProxyProvider; // 注意这里新增了 import
//
//import javax.net.ssl.SSLException;
//
//@Configuration
//public class SslBypassConfig {
//
//    @Bean
//    public WebClient.Builder webClientBuilder() throws SSLException {
//        // 1. 配置信任所有证书
//        SslContext sslContext = SslContextBuilder.forClient()
//                .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                .build();
//
//        // 2. 将代理配置和证书配置一起塞给 Netty
//        HttpClient httpClient = HttpClient.create()
//                // 强行指定走 ProxyPin 的代理端口
//                .proxy(proxy -> proxy
//                        .type(ProxyProvider.Proxy.HTTP)
//                        .host("127.0.0.1")
//                        .port(7990))
//                // 强行忽略 SSL 校验
//                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
//
//        // 3. 返回构造器
//        return WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient));
//    }
//}