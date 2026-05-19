package com.whatsyouretf.userservice.common.auth;

import com.whatsyouretf.userservice.common.util.JwtTokenUtil;
import com.whatsyouretf.userservice.domain.user.entity.User;
import com.whatsyouretf.userservice.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);
            log.debug("Token resolved: {}", token != null ? "present" : "null");

            if (StringUtils.hasText(token)) {
                boolean isValid = jwtTokenUtil.validateToken(token);
                log.debug("Token validation: {}", isValid);

                if (isValid) {
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    log.debug("User ID from token: {}", userId);

                    User user = userRepository.findById(userId).orElse(null);
                    log.debug("User found: {}, isActive: {}", user != null, user != null ? user.getIsActive() : "N/A");

                    if (user != null && Boolean.TRUE.equals(user.getIsActive())) {
                        CustomUserDetails userDetails = new CustomUserDetails(user);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Set Authentication for user: {}", userId);
                    } else {
                        log.warn("User not found or inactive: userId={}", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtTokenUtil.HEADER_STRING);
        return jwtTokenUtil.resolveToken(bearerToken);
    }
}
