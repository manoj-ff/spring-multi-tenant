package com.example.tenant.config;

import com.example.tenant.entity.master.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof AppUser) {
                AppUser appUser = (AppUser) principal;
                String tenantId = appUser.getTenantId();
                if (tenantId != null) {
                    TenantContext.setCurrentTenant(tenantId);
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear the tenant context after the request has been processed
            TenantContext.clear();
        }
    }
}
