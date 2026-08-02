package com.enterprise.platform.core.security;

import com.enterprise.platform.modules.authentication.adapter.AuthIdentityRepository;
import com.enterprise.platform.modules.authentication.adapter.JwtProvider;
import com.enterprise.platform.modules.authorization.service.PermissionCacheService;
import com.enterprise.platform.modules.authorization.service.PermissionResolver;
import com.enterprise.platform.modules.authorization.service.RoleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final AuthIdentityRepository authIdentityRepository;
    private final PermissionCacheService permissionCacheService;
    private final PermissionResolver permissionResolver;
    private final RoleResolver roleResolver;
    private final com.enterprise.platform.core.config.properties.CorsProperties corsProperties;

    public SecurityConfig(
            JwtProvider jwtProvider,
            AuthIdentityRepository authIdentityRepository,
            PermissionCacheService permissionCacheService,
            PermissionResolver permissionResolver,
            RoleResolver roleResolver,
            com.enterprise.platform.core.config.properties.CorsProperties corsProperties) {
        this.jwtProvider = jwtProvider;
        this.authIdentityRepository = authIdentityRepository;
        this.permissionCacheService = permissionCacheService;
        this.permissionResolver = permissionResolver;
        this.roleResolver = roleResolver;
        this.corsProperties = corsProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(corsProperties.allowedMethods());
        configuration.setAllowedHeaders(corsProperties.allowedHeaders());
        configuration.setAllowCredentials(corsProperties.allowCredentials());
        configuration.setMaxAge(corsProperties.maxAgeSeconds());

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/verify-email", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                .requestMatchers("/api/v1/auth/change-password").authenticated()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(
                    jwtProvider,
                    authIdentityRepository,
                    permissionCacheService,
                    permissionResolver,
                    roleResolver
                ),
                UsernamePasswordAuthenticationFilter.class
            );
        return http.build();
    }
}
