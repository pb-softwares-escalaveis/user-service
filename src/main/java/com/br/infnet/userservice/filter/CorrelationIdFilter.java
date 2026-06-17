package com.br.infnet.userservice.filter;

import com.br.infnet.userservice.utils.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException {
        try {
            String correlationId = request.getHeader("X-Correlation-Id");
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = CorrelationIdUtil.generateCorrelationId();
            }
            MDC.put("correlationId", correlationId);
            response.setHeader("X-Correlation-Id", correlationId);
            filterChain.doFilter(request, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
