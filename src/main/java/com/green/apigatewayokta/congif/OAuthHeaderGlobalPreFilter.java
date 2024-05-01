package com.green.apigatewayokta.congif;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
        var LOG_NAME = "filter";


        return ReactiveSecurityContextHolder.getContext()
                .filter(c -> c.getAuthentication() != null)
                .flatMap(c -> {
                    log.info("{} Begin...", LOG_NAME);

                    Authentication authentication = c.getAuthentication();
                    Jwt user =(Jwt) authentication.getPrincipal();
                    String sso = authentication.getName();
                    System.out.println(user);
                    log.info("{} sso {}", LOG_NAME, sso);

                    if (ObjectUtils.isEmpty(sso)) {
                        var error = "Invalid token. User is not present in token.";
                        log.error("{} error {}", LOG_NAME, error);
                        return Mono.error(
                                new AccessDeniedException(error)
                        );
                    }
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("sso-id", sso).build();
                    log.info("{} End.", LOG_NAME);
                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(chain.filter(exchange));

    }
}
