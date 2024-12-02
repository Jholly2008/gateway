package com.example.demo.gateway.filter;

import com.example.demo.gateway.utils.JwtUtils;
import io.opentelemetry.api.baggage.Baggage;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.util.StringUtils;

@Component
public class VersionFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TENANT_HEADER = "x-api-version";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authorization = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authorization)) {
            try {
                String tenant = JwtUtils.getTenantFromToken(authorization);

                // 创建新的请求，添加租户信息到请求头
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header(TENANT_HEADER, tenant)
                        .build();

                // 使用新的请求创建新的 exchange
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(mutatedRequest)
                        .build();

                // 创建新的 Baggage
                Baggage.current()
                        .toBuilder()
                        .put("one-key", "one-value")
                        .put(TENANT_HEADER, tenant)
                        .put("two-key", "two-value")
                        .build().makeCurrent();

                // 使用 contextWrite 确保 Baggage 正确传播
                return chain.filter(mutatedExchange);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 如果没有 authorization header，继续传递原始请求
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 设置过滤器优先级，建议设置一个较高的优先级（数字越小优先级越高）
        return -9998;
    }
}
