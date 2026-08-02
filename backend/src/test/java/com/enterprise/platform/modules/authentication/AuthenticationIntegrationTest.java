package com.enterprise.platform.modules.authentication;

import com.enterprise.platform.modules.authentication.api.*;
import com.enterprise.platform.modules.authentication.adapter.*;
import com.enterprise.platform.modules.authorization.domain.Role;
import com.enterprise.platform.modules.authorization.domain.UserRole;
import com.enterprise.platform.modules.authorization.repository.RoleRepository;
import com.enterprise.platform.modules.authorization.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthIdentityRepository identityRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AuthCredentialsRepository credentialsRepository;

    @Autowired
    private ActiveSessionRepository activeSessionRepository;

    @Autowired
    private AuthDeviceRepository authDeviceRepository;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private AccountLockoutRepository lockoutRepository;

    @Autowired
    private AuthAuditEventRepository auditEventRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public static record ApiAuthResponse(
        boolean success,
        String timestamp,
        String requestId,
        AuthResponse data
    ) {}

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/auth";
    }

    private String getFoldersTreeUrl() {
        return "http://localhost:" + port + "/api/v1/folders/tree";
    }

    @BeforeEach
    public void cleanUp() {
        // Clean up test users
        Optional<AuthIdentityEntity> testUserOpt = identityRepository.findByTenantIdAndEmail("acme", "integration.user@acme.com");
        testUserOpt.ifPresent(identity -> {
            userRoleRepository.findAll().stream()
                    .filter(ur -> ur.getIdentityId().equals(identity.getId()))
                    .forEach(userRoleRepository::delete);
            activeSessionRepository.findAll().stream()
                    .filter(s -> s.getIdentity().getId().equals(identity.getId()))
                    .forEach(activeSessionRepository::delete);
            authDeviceRepository.findAll().stream()
                    .filter(d -> d.getIdentity().getId().equals(identity.getId()))
                    .forEach(authDeviceRepository::delete);
            loginAttemptRepository.findAll().stream()
                    .filter(l -> l.getIdentity().getId().equals(identity.getId()))
                    .forEach(loginAttemptRepository::delete);
            lockoutRepository.findAll().stream()
                    .filter(l -> l.getIdentity().getId().equals(identity.getId()))
                    .forEach(lockoutRepository::delete);
            auditEventRepository.findAll().stream()
                    .filter(a -> a.getIdentity().getId().equals(identity.getId()))
                    .forEach(auditEventRepository::delete);
            passwordResetTokenRepository.findAll().stream()
                    .filter(t -> t.getIdentity().getId().equals(identity.getId()))
                    .forEach(passwordResetTokenRepository::delete);
            emailVerificationTokenRepository.findAll().stream()
                    .filter(t -> t.getIdentity().getId().equals(identity.getId()))
                    .forEach(emailVerificationTokenRepository::delete);
            List<RefreshTokenEntity> userTokens = refreshTokenRepository.findAll().stream()
                    .filter(t -> t.getIdentity().getId().equals(identity.getId()))
                    .toList();
            userTokens.forEach(t -> {
                t.setParentToken(null);
                refreshTokenRepository.saveAndFlush(t);
            });
            userTokens.forEach(refreshTokenRepository::delete);

            credentialsRepository.findByIdentityId(identity.getId()).ifPresent(credentialsRepository::delete);
            identityRepository.delete(identity);
        });
    }

    @Test
    public void testFullAuthenticationFlow() {
        // 1. Register User
        RegisterRequest registerReq = new RegisterRequest("integration.user@acme.com", "Password123!!", "acme");
        ResponseEntity<ApiAuthResponse> registerRes = restTemplate.postForEntity(
                getBaseUrl() + "/register",
                registerReq,
                ApiAuthResponse.class
        );

        assertEquals(HttpStatus.OK, registerRes.getStatusCode());
        assertNotNull(registerRes.getBody());
        assertTrue(registerRes.getBody().success());
        assertNotNull(registerRes.getBody().data());
        assertNotNull(registerRes.getBody().data().accessToken());
        assertNotNull(registerRes.getBody().data().refreshToken());

        // 2. Login (Should return 401 Unauthorized because email is pending verification)
        LoginRequest loginReq = new LoginRequest("integration.user@acme.com", "Password123!!", "acme");
        ResponseEntity<Map> failedLoginRes = restTemplate.postForEntity(
                getBaseUrl() + "/login",
                loginReq,
                Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, failedLoginRes.getStatusCode());
        Map failedLoginBody = failedLoginRes.getBody();
        assertNotNull(failedLoginBody);
        
        // Response wrapper can have nested map for data/error
        if (failedLoginBody.containsKey("data")) {
            Map dataMap = (Map) failedLoginBody.get("data");
            assertEquals(401, dataMap.get("status"));
            assertEquals("Email verification is pending", dataMap.get("message"));
        } else {
            assertEquals(401, failedLoginBody.get("status"));
            assertEquals("Email verification is pending", failedLoginBody.get("message"));
        }

        // 3. Retrieve Verification Token from DB and Verify Email
        AuthIdentityEntity identity = identityRepository.findByTenantIdAndEmail("acme", "integration.user@acme.com")
                .orElseThrow(() -> new AssertionError("Identity was not saved in DB"));

        EmailVerificationTokenEntity verificationToken = emailVerificationTokenRepository.findByIdentityId(identity.getId())
                .orElseThrow(() -> new AssertionError("Verification token not found in DB"));

        ResponseEntity<Void> verifyRes = restTemplate.getForEntity(
                getBaseUrl() + "/verify-email?token=" + verificationToken.getTokenValue(),
                Void.class
        );
        assertEquals(HttpStatus.OK, verifyRes.getStatusCode());

        // Associate user with ROLE_VIEWER so they have appropriate permission for protected endpoints
        Role viewerRole = roleRepository.findByName("ROLE_VIEWER")
                .orElseThrow(() -> new AssertionError("ROLE_VIEWER not seeded"));
        userRoleRepository.save(new UserRole(identity.getId(), viewerRole, "acme"));

        // 4. Login Again (Should succeed now and return 200 OK)
        ResponseEntity<ApiAuthResponse> loginRes = restTemplate.postForEntity(
                getBaseUrl() + "/login",
                loginReq,
                ApiAuthResponse.class
        );

        assertEquals(HttpStatus.OK, loginRes.getStatusCode());
        assertNotNull(loginRes.getBody());
        assertTrue(loginRes.getBody().success());
        String jwtToken = loginRes.getBody().data().accessToken();
        String refreshTokenValue = loginRes.getBody().data().refreshToken();
        assertNotNull(jwtToken);
        assertNotNull(refreshTokenValue);

        // 5. Access Protected Endpoint without JWT (Should return 403 Forbidden)
        ResponseEntity<Void> protectedNoJwtRes = restTemplate.getForEntity(
                getFoldersTreeUrl(),
                Void.class
        );
        assertEquals(HttpStatus.FORBIDDEN, protectedNoJwtRes.getStatusCode());

        // 6. Access Protected Endpoint with JWT (Should succeed, returning 200/404/etc, NOT 403)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> requestWithJwt = new HttpEntity<>(headers);

        ResponseEntity<Object> protectedWithJwtRes = restTemplate.exchange(
                getFoldersTreeUrl(),
                HttpMethod.GET,
                requestWithJwt,
                Object.class
        );
        assertEquals(HttpStatus.OK, protectedWithJwtRes.getStatusCode());

        // 7. Change Password
        ChangePasswordRequest changePasswordReq = new ChangePasswordRequest("Password123!!", "NewPassword123!!");
        HttpEntity<ChangePasswordRequest> changePasswordEntity = new HttpEntity<>(changePasswordReq, headers);
        ResponseEntity<Void> changePasswordRes = restTemplate.exchange(
                getBaseUrl() + "/change-password",
                HttpMethod.POST,
                changePasswordEntity,
                Void.class
        );
        assertEquals(HttpStatus.OK, changePasswordRes.getStatusCode());

        // 8. Forgot Password recovery flow
        ResponseEntity<Void> forgotPasswordRes = restTemplate.postForEntity(
                getBaseUrl() + "/forgot-password?email=integration.user@acme.com",
                null,
                Void.class
        );
        assertEquals(HttpStatus.OK, forgotPasswordRes.getStatusCode());

        // Retrieve Password Reset Token from DB
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findAll().stream()
                .filter(t -> t.getIdentity().getId().equals(identity.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Password reset token not found in DB"));

        // Reset Password using the recovery token
        ResetPasswordRequest resetPasswordReq = new ResetPasswordRequest(
                resetToken.getTokenValue().toString(),
                "FinalPassword123!!"
        );
        ResponseEntity<Void> resetPasswordRes = restTemplate.postForEntity(
                getBaseUrl() + "/reset-password",
                resetPasswordReq,
                Void.class
        );
        assertEquals(HttpStatus.OK, resetPasswordRes.getStatusCode());

        // 9. Login with final password
        LoginRequest finalLoginReq = new LoginRequest("integration.user@acme.com", "FinalPassword123!!", "acme");
        ResponseEntity<ApiAuthResponse> finalLoginRes = restTemplate.postForEntity(
                getBaseUrl() + "/login",
                finalLoginReq,
                ApiAuthResponse.class
        );
        assertEquals(HttpStatus.OK, finalLoginRes.getStatusCode());
        assertNotNull(finalLoginRes.getBody());
        String finalJwtToken = finalLoginRes.getBody().data().accessToken();
        String finalRefreshTokenValue = finalLoginRes.getBody().data().refreshToken();

        // 10. Refresh Token Rotation (Rotate the refresh token)
        HttpHeaders refreshHeaders = new HttpHeaders();
        refreshHeaders.set("Authorization", "Bearer " + finalRefreshTokenValue);
        HttpEntity<Void> refreshRequest = new HttpEntity<>(refreshHeaders);

        ResponseEntity<ApiAuthResponse> refreshRes = restTemplate.postForEntity(
                getBaseUrl() + "/refresh",
                refreshRequest,
                ApiAuthResponse.class
        );
        assertEquals(HttpStatus.OK, refreshRes.getStatusCode());
        assertNotNull(refreshRes.getBody());
        String newRefreshTokenValue = refreshRes.getBody().data().refreshToken();
        assertNotEquals(finalRefreshTokenValue, newRefreshTokenValue);

        // 11. Re-use Rotated Refresh Token (Should return 401 Unauthorized)
        ResponseEntity<Map> reuseRefreshRes = restTemplate.postForEntity(
                getBaseUrl() + "/refresh",
                refreshRequest,
                Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, reuseRefreshRes.getStatusCode());

        // 12. Logout using new refresh token
        HttpHeaders logoutHeaders = new HttpHeaders();
        logoutHeaders.set("Authorization", "Bearer " + newRefreshTokenValue);
        HttpEntity<Void> logoutRequest = new HttpEntity<>(logoutHeaders);

        ResponseEntity<Void> logoutRes = restTemplate.postForEntity(
                getBaseUrl() + "/logout",
                logoutRequest,
                Void.class
        );
        assertEquals(HttpStatus.OK, logoutRes.getStatusCode());
    }
}
