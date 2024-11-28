package com.example.demo.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.UUID;

@Component
@Slf4j
public class TraceFilter implements GlobalFilter, Ordered {

    private static final String X_B3_TRACE_ID = "X-B3-TraceId";
    private static final String X_B3_SPAN_ID = "X-B3-SpanId";
    private static final String X_B3_PARENT_SPAN_ID = "X-B3-ParentSpanId";
    private static final String X_B3_SAMPLED = "X-B3-Sampled";

    @Value("${app.kubernetes.enabled:false}")
    private boolean isKubernetesEnabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!isKubernetesEnabled) {
            log.debug("Not in Kubernetes environment, manually setting trace headers");
            return handleNonK8sRequest(exchange, chain);
        } else {
            log.debug("Running in Kubernetes environment, trace headers handled by Istio");
            return chain.filter(exchange);
        }
    }

    private Mono<Void> handleNonK8sRequest(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutatedRequest = request.mutate();

        // 如果不存在，生成新的追踪信息
        String traceId = generateTraceId();
        String spanId = generateSpanId();

        // 添加B3追踪头
        mutatedRequest.header(X_B3_TRACE_ID, traceId)
                .header(X_B3_SPAN_ID, spanId)
                .header(X_B3_PARENT_SPAN_ID, "0")  // 网关作为起始节点，parentSpanId为0
                .header(X_B3_SAMPLED, "1");        // 启用采样

        // 使用修改后的请求构建新的 ServerWebExchange
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest.build())
                .build();

        // 记录日志（可选）
        logTraceInfo(mutatedExchange.getRequest());

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -9999;  // 确保在其他过滤器之前执行
    }

    /**
     * 生成32位的TraceId
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成16位的SpanId
     */
    private String generateSpanId() {
        return "gateway-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 记录追踪信息（可选）
     */
    private void logTraceInfo(ServerHttpRequest request) {
        String traceId = request.getHeaders().getFirst(X_B3_TRACE_ID);
        String spanId = request.getHeaders().getFirst(X_B3_SPAN_ID);
        String path = request.getPath().value();

        log.info("Gateway Trace - Path: {}, TraceId: {}, SpanId: {}", path, traceId, spanId);
    }

}