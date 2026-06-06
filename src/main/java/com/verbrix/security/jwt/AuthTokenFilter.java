package com.verbrix.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/public/")
                || path.startsWith("/api/v1/metadata/")
                || path.startsWith("/api/v1/webhooks/")
                || path.startsWith("/ws/")
                || path.equals("/api/health");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extract token
            String jwt = jwtUtils.getJwtFromHeader(request);

            // 2. No token present → continue filter chain
            if (jwt == null || jwt.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtUtils.validateJwtToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Token valid → load user
            String username = jwtUtils.getUsernameFromJwtToken(jwt);
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            // 5. Build authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            // 6. Set security context
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception ex) {
            // Do NOT break request flow
            logger.error("JWT authentication failed: {}", ex.getMessage());
        }

        // 7. Continue request
        filterChain.doFilter(request, response);
    }
}
