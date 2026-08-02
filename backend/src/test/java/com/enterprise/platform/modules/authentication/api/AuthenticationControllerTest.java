package com.enterprise.platform.modules.authentication.api;

import com.enterprise.platform.core.security.SecurityConfig;
import com.enterprise.platform.modules.authentication.adapter.JwtProvider;
import com.enterprise.platform.modules.authentication.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthenticationController.class)
@Import({SecurityConfig.class, com.enterprise.platform.core.config.ClockConfig.class})
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private com.enterprise.platform.modules.authentication.adapter.AuthIdentityRepository authIdentityRepository;

    @MockBean
    private com.enterprise.platform.modules.authorization.service.PermissionCacheService permissionCacheService;

    @MockBean
    private com.enterprise.platform.modules.authorization.service.PermissionResolver permissionResolver;

    @MockBean
    private com.enterprise.platform.modules.authorization.service.RoleResolver roleResolver;

    @MockBean
    private com.enterprise.platform.core.config.properties.CorsProperties corsProperties;

    @MockBean
    private com.enterprise.platform.infrastructure.ratelimit.RateLimiter rateLimiter;

    @MockBean
    private com.enterprise.platform.infrastructure.ratelimit.RateLimitKeyResolver rateLimitKeyResolver;

    @Autowired
    private ObjectMapper objectMapper;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        Mockito.when(corsProperties.allowedOrigins()).thenReturn(java.util.List.of("*"));
        Mockito.when(corsProperties.allowedMethods()).thenReturn(java.util.List.of("GET", "POST", "PUT", "DELETE"));
        Mockito.when(corsProperties.allowedHeaders()).thenReturn(java.util.List.of("Authorization", "Content-Type"));
        Mockito.when(corsProperties.allowCredentials()).thenReturn(false);
        Mockito.when(corsProperties.maxAgeSeconds()).thenReturn(3600L);
        Mockito.when(rateLimiter.isAllowed(Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    public void register_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("david.chen@acme.com", "Password123!!", "acme");
        AuthResponse response = new AuthResponse("mock-access", "mock-refresh", 900);
        
        Mockito.when(authenticationService.register(Mockito.any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void register_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "Password123!!", "acme");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
