package com.example.gateway.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String requestCorrelationId = correlationId;
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, requestCorrelationId);
        ServerWebExchange forwardedExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(CORRELATION_ID_HEADER, requestCorrelationId)))
                .build();

        return chain.filter(forwardedExchange)
                .doFinally(signalType -> logRequest(forwardedExchange, requestCorrelationId));
    }

    private void logRequest(ServerWebExchange exchange, String correlationId) {
        HttpStatusCode status = exchange.getResponse().getStatusCode();
        logger.info("method={} path={} status={} correlationId={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                status == null ? "UNKNOWN" : status.value(),
                correlationId);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
