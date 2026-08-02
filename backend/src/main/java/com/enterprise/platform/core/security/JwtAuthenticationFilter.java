package com.enterprise.platform.core.security;

import com.enterprise.platform.modules.authentication.adapter.AuthIdentityEntity;
import com.enterprise.platform.modules.authentication.adapter.AuthIdentityRepository;
import com.enterprise.platform.modules.authentication.adapter.JwtProvider;
import com.enterprise.platform.modules.authorization.domain.AuthorizationContext;
import com.enterprise.platform.modules.authorization.domain.Role;
import com.enterprise.platform.modules.authorization.service.PermissionCacheService;
import com.enterprise.platform.modules.authorization.service.PermissionResolver;
import com.enterprise.platform.modules.authorization.service.RoleResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AuthIdentityRepository authIdentityRepository;
    private final PermissionCacheService permissionCacheService;
    private final PermissionResolver permissionResolver;
    private final RoleResolver roleResolver;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            AuthIdentityRepository authIdentityRepository,
            PermissionCacheService permissionCacheService,
            PermissionResolver permissionResolver,
            RoleResolver roleResolver) {
        this.jwtProvider = jwtProvider;
        this.authIdentityRepository = authIdentityRepository;
        this.permissionCacheService = permissionCacheService;
        this.permissionResolver = permissionResolver;
        this.roleResolver = roleResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmailFromToken(token);
                String tenantId = jwtProvider.getTenantIdFromToken(token);
                
                Optional<AuthIdentityEntity> identityOpt = authIdentityRepository.findByTenantIdAndEmail(tenantId, email);
                if (identityOpt.isPresent()) {
                    AuthIdentityEntity identity = identityOpt.get();
                    UUID userId = identity.getId();

                    // 1. Resolve and cache user permissions
                    Set<String> permissions = permissionCacheService.getUserPermissions(userId, tenantId);
                    if (permissions == null) {
                        permissions = permissionResolver.resolvePermissions(userId, tenantId);
                        permissionCacheService.cacheUserPermissions(userId, tenantId, permissions);
                    }

                    // 2. Resolve active roles names
                    Set<Role> roles = roleResolver.resolveRoles(userId, tenantId);
                    Set<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());

                    // 3. Construct dynamic AuthorizationContext principal
                    AuthorizationContext context = new AuthorizationContext(
                            userId,
                            tenantId,
                            null, // workspaceId can be injected from request headers/routes if needed
                            roleNames,
                            permissions,
                            request.getSession(false) != null ? request.getSession(false).getId() : null,
                            request.getHeader("X-Device-Fingerprint")
                    );

                    // Map permissions & roles to SimpleGrantedAuthority for default spring security filters compatibility
                    List<SimpleGrantedAuthority> authorities = roleNames.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    
                    permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority("PERMISSION_" + p)));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            context, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativePath = path;
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            relativePath = path.substring(contextPath.length());
        }
        return relativePath.equals("/api/v1/auth/register")
                || relativePath.equals("/api/v1/auth/login")
                || relativePath.equals("/api/v1/auth/verify-email")
                || relativePath.equals("/api/v1/auth/forgot-password")
                || relativePath.equals("/api/v1/auth/reset-password")
                || relativePath.equals("/api/v1/auth/refresh")
                || relativePath.equals("/api/v1/auth/logout")
                || relativePath.startsWith("/actuator")
                || relativePath.startsWith("/swagger-ui")
                || relativePath.startsWith("/v3/api-docs");
    }
}
