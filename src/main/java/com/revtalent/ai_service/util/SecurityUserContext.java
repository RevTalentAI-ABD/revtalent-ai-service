package com.revtalent.ai_service.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class SecurityUserContext {

    private final JwtUtil jwtUtil;

    public Long getCurrentUserId() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                Long userId = jwtUtil.extractUserId(auth.substring(7));
                if (userId != null) {
                    return userId;
                }
            }
        }
        throw new IllegalStateException("Authenticated user id is missing from JWT");
    }
}
