package com.zosh.config;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtTokenValidator extends OncePerRequestFilter {

    // ✅ CRITICAL: skip JWT validation for auth / OTP APIs
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/auth/")
                || path.startsWith("/sellers/")
                || path.startsWith("/login")
                || path.startsWith("/register");
    }

    @Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
) throws ServletException, IOException {

    String path = request.getRequestURI();

    // ✅ VERY IMPORTANT: skip JWT validation for auth endpoints
    if (request.getRequestURI().startsWith("/auth")) {
    filterChain.doFilter(request, response);
    return;
}

    String authHeader = request.getHeader(JwtConstant.JWT_HEADER);

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    try {
        String jwt = authHeader.substring(7);

        SecretKey key =
                Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        String email = claims.get("email", String.class);
        String authorities = claims.get("authorities", String.class);

        List<GrantedAuthority> auths =
                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(email, null, auths);

        SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (Exception e) {
        // ✅ DO NOT throw exception here
        SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
}
}
