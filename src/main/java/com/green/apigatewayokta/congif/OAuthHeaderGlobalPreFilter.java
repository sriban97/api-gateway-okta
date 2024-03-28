package com.green.apigatewayokta.congif;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;

@Component
@Slf4j
public class OAuthHeaderGlobalPreFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .filter(c -> c.getAuthentication() != null)
                .flatMap(c -> {
                    Authentication authentication = c.getAuthentication();
                    String sso = authentication.getName();
                    if (ObjectUtils.isEmpty(sso)) {
                        return Mono.error(
                                new AccessDeniedException("Invalid token. User is not present in token.")
                        );
                    }
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("sso-id", sso).build();
                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
