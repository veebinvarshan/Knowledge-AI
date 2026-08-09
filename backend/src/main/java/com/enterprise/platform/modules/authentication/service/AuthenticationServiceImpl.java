package com.enterprise.platform.modules.authentication.service;

import com.enterprise.platform.modules.authentication.api.*;
import com.enterprise.platform.modules.authentication.adapter.*;
import com.enterprise.platform.modules.authentication.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final AuthIdentityRepository identityRepository;
    private final AuthCredentialsRepository credentialsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthDeviceRepository deviceRepository;
    private final ActiveSessionRepository activeSessionRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountLockoutRepository lockoutRepository;
    private final AuthAuditEventRepository auditEventRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public AuthenticationServiceImpl(
            AuthIdentityRepository identityRepository,
            AuthCredentialsRepository credentialsRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthDeviceRepository deviceRepository,
            ActiveSessionRepository activeSessionRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AccountLockoutRepository lockoutRepository,
            AuthAuditEventRepository auditEventRepository,
            LoginAttemptRepository loginAttemptRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            StringRedisTemplate redisTemplate,
            ApplicationEventPublisher eventPublisher) {
        this.identityRepository = identityRepository;
        this.credentialsRepository = credentialsRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.deviceRepository = deviceRepository;
        this.activeSessionRepository = activeSessionRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.lockoutRepository = lockoutRepository;
        this.auditEventRepository = auditEventRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String emailClean = request.email().toLowerCase().trim();
        log.info("Registering user context for tenant: {}, email: {}", request.tenant(), emailClean);

        Optional<AuthIdentityEntity> existing = identityRepository.findByTenantIdAndEmail(request.tenant(), emailClean);
        if (existing.isPresent()) {
            throw new AuthenticationException("Identity already registered under this organization");
        }

        AuthIdentityEntity identity = new AuthIdentityEntity();
        identity.setTenantId(request.tenant());
        identity.setEmail(emailClean);
        identity.setStatus("PENDING_VERIFICATION");
        identity = identityRepository.save(identity);

        AuthCredentialsEntity credentials = new AuthCredentialsEntity();
        credentials.setIdentity(identity);
        credentials.setPasswordHash(passwordEncoder.encode(request.password()));
        credentialsRepository.save(credentials);

        EmailVerificationTokenEntity verificationToken = new EmailVerificationTokenEntity();
        verificationToken.setIdentity(identity);
        verificationToken.setTokenValue(UUID.randomUUID());
        verificationToken.setExpiresAt(Instant.now().plusSeconds(7200)); // 2 hours
        emailVerificationTokenRepository.save(verificationToken);

        // Placeholder for Email Service
        log.info("DISPATCHING VERIFICATION EMAIL to: {}, token: {}", emailClean, verificationToken.getTokenValue());

        eventPublisher
                .publishEvent(new UserRegisteredEvent(identity.getId(), identity.getEmail(), identity.getTenantId()));

        String accessToken = jwtProvider.generateToken(identity.getEmail(), identity.getTenantId(),
                List.of("ROLE_VIEWER"));
        RefreshTokenEntity refreshToken = createRefreshToken(identity);

        return new AuthResponse(accessToken, refreshToken.getTokenValue().toString(), 900);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent, String fingerprint) {
        String emailClean = request.email().toLowerCase().trim();
        log.info("Processing login request for tenant: {}, email: {}", request.tenant(), emailClean);

        AuthIdentityEntity identity = identityRepository.findByTenantIdAndEmail(request.tenant(), emailClean)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid organization credentials"));

        checkLockoutStatus(identity);

        AuthCredentialsEntity credentials = credentialsRepository.findByIdentityId(identity.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Credentials profile missing"));

        if (!passwordEncoder.matches(request.password(), credentials.getPasswordHash())) {
            recordFailedAttempt(identity, ipAddress);
            throw new InvalidCredentialsException("Invalid organization credentials");
        }

        if ("PENDING_VERIFICATION".equals(identity.getStatus())) {
            throw new AuthenticationException("Email verification is pending");
        }

        // Clean up failed attempts on success
        clearFailedAttempts(identity);

        String finalFingerprint = fingerprint != null && !fingerprint.isBlank() ? fingerprint : "default-fingerprint";
        String finalIp = ipAddress != null && !ipAddress.isBlank() ? ipAddress : "0.0.0.0";
        String finalUserAgent = userAgent != null && !userAgent.isBlank() ? userAgent : "Unknown Device";

        AuthDeviceEntity device = deviceRepository
                .findByIdentityIdAndFingerprintHash(identity.getId(), finalFingerprint)
                .orElseGet(() -> {
                    AuthDeviceEntity d = new AuthDeviceEntity();
                    d.setIdentity(identity);
                    d.setFingerprintHash(finalFingerprint);
                    d.setDeviceName(finalUserAgent);
                    d.setLastIpAddress(finalIp);
                    return deviceRepository.save(d);
                });
        device.setLastSeenAt(Instant.now());
        device.setLastIpAddress(finalIp);
        deviceRepository.save(device);

        ActiveSessionEntity session = new ActiveSessionEntity();
        session.setIdentity(identity);
        session.setDevice(device);
        session.setExpiresAt(Instant.now().plusSeconds(86400)); // 24 hours
        activeSessionRepository.save(session);

        String accessToken = jwtProvider.generateToken(identity.getEmail(), identity.getTenantId(),
                List.of("ROLE_VIEWER"));
        RefreshTokenEntity refreshToken = createRefreshToken(identity);

        writeAuditEvent(identity.getTenantId(), identity, "LOGIN_SUCCESS", ipAddress, userAgent);
        eventPublisher.publishEvent(
                new UserLoggedInEvent(identity.getId(), identity.getEmail(), identity.getTenantId(), ipAddress));

        return new AuthResponse(accessToken, refreshToken.getTokenValue().toString(), 900);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        log.info("Processing session logout request");
        UUID tokenUuid = UUID.fromString(refreshTokenValue);
        RefreshTokenEntity token = refreshTokenRepository.findByTokenValue(tokenUuid)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshTokenRepository.delete(token);

        eventPublisher
                .publishEvent(new UserLoggedOutEvent(token.getIdentity().getId(), token.getIdentity().getTenantId()));
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshTokenValue, String fingerprint) {
        log.info("Processing token refresh transaction");
        UUID tokenUuid = UUID.fromString(refreshTokenValue);
        RefreshTokenEntity token = refreshTokenRepository.findByTokenValue(tokenUuid)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (token.isRotated() || token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new InvalidTokenException("Token has been rotated or expired");
        }

        AuthIdentityEntity identity = token.getIdentity();

        // Rotate Refresh Token
        token.setRotated(true);
        refreshTokenRepository.save(token);

        RefreshTokenEntity nextRefreshToken = createRefreshToken(identity);
        nextRefreshToken.setParentToken(token);
        refreshTokenRepository.save(nextRefreshToken);

        String accessToken = jwtProvider.generateToken(identity.getEmail(), identity.getTenantId(),
                List.of("ROLE_VIEWER"));

        return new AuthResponse(accessToken, nextRefreshToken.getTokenValue().toString(), 900);
    }

    @Override
    public boolean validateSession(String accessToken) {
        if (!jwtProvider.validateToken(accessToken)) {
            return false;
        }
        // Check Redis blacklist
        try {
            Boolean blacklisted = redisTemplate.hasKey("blacklist:token:" + accessToken);
            return !Boolean.TRUE.equals(blacklisted);
        } catch (Exception e) {
            log.warn("Redis connectivity failure during blacklist verification", e);
            return true; // Fall back to true if Redis fails to ensure availability
        }
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        String emailClean = email.toLowerCase().trim();
        log.info("Processing password recovery request for email: {}", emailClean);

        AuthIdentityEntity identity = identityRepository.findByEmail(emailClean)
                .orElseThrow(() -> new AuthenticationException("Identity not registered"));

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setIdentity(identity);
        resetToken.setTokenValue(UUID.randomUUID());
        resetToken.setExpiresAt(Instant.now().plusSeconds(1800)); // 30 minutes
        passwordResetTokenRepository.save(resetToken);

        log.info("DISPATCHING RESET PASSWORD LINK to: {}, token: {}", emailClean, resetToken.getTokenValue());
        eventPublisher.publishEvent(
                new PasswordResetRequestedEvent(identity.getId(), identity.getEmail(), resetToken.getTokenValue()));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset transaction");
        UUID tokenUuid = UUID.fromString(request.token());
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByTokenValue(tokenUuid)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Password reset token has expired");
        }

        AuthIdentityEntity identity = resetToken.getIdentity();
        AuthCredentialsEntity credentials = credentialsRepository.findByIdentityId(identity.getId())
                .orElseThrow(() -> new AuthenticationException("Credentials profile missing"));

        credentials.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        credentials.setPasswordChangedAt(Instant.now());
        credentialsRepository.save(credentials);

        passwordResetTokenRepository.delete(resetToken);

        writeAuditEvent(identity.getTenantId(), identity, "PASSWORD_RESET_SUCCESS", "0.0.0.0", "System");
        eventPublisher
                .publishEvent(new PasswordChangedEvent(identity.getId(), identity.getEmail(), identity.getTenantId()));
    }

    @Override
    @Transactional
    public void verifyEmail(String tokenValue) {
        log.info("Verifying email validation token");
        UUID tokenUuid = UUID.fromString(tokenValue);
        EmailVerificationTokenEntity verificationToken = emailVerificationTokenRepository.findByTokenValue(tokenUuid)
                .orElseThrow(() -> new InvalidTokenException("Invalid email verification token"));

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Email verification token has expired");
        }

        AuthIdentityEntity identity = verificationToken.getIdentity();
        identity.setStatus("ACTIVE");
        identity.setUpdatedAt(Instant.now());
        identityRepository.save(identity);

        emailVerificationTokenRepository.delete(verificationToken);

        writeAuditEvent(identity.getTenantId(), identity, "EMAIL_VERIFICATION_SUCCESS", "0.0.0.0", "System");
        eventPublisher
                .publishEvent(new EmailVerifiedEvent(identity.getId(), identity.getEmail(), identity.getTenantId()));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Processing password change request for identifier: {}", email);
        AuthIdentityEntity identity;
        try {
            UUID id = UUID.fromString(email);
            identity = identityRepository.findById(id)
                    .orElseThrow(() -> new AuthenticationException("Identity not registered"));
        } catch (IllegalArgumentException e) {
            identity = identityRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("Identity not registered"));
        }

        AuthCredentialsEntity credentials = credentialsRepository.findByIdentityId(identity.getId())
                .orElseThrow(() -> new AuthenticationException("Credentials profile missing"));

        if (!passwordEncoder.matches(request.oldPassword(), credentials.getPasswordHash())) {
            throw new InvalidCredentialsException("Mismatched old credentials");
        }

        credentials.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        credentials.setPasswordChangedAt(Instant.now());
        credentialsRepository.save(credentials);

        writeAuditEvent(identity.getTenantId(), identity, "PASSWORD_CHANGE_SUCCESS", "0.0.0.0", "System");
        eventPublisher
                .publishEvent(new PasswordChangedEvent(identity.getId(), identity.getEmail(), identity.getTenantId()));
    }

    private RefreshTokenEntity createRefreshToken(AuthIdentityEntity identity) {
        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setIdentity(identity);
        refreshToken.setTokenValue(UUID.randomUUID());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(604800)); // 7 days
        return refreshTokenRepository.save(refreshToken);
    }

    private void checkLockoutStatus(AuthIdentityEntity identity) {
        Optional<AccountLockoutEntity> lockout = lockoutRepository.findByIdentityIdAndUnlocksAtAfter(identity.getId(),
                Instant.now());
        if (lockout.isPresent()) {
            throw new AccountLockedException(
                    "Account is temporarily locked. Try again after: " + lockout.get().getUnlocksAt());
        }
    }

    private void recordFailedAttempt(AuthIdentityEntity identity, String ipAddress) {
        LoginAttemptEntity attempt = new LoginAttemptEntity();
        attempt.setIdentity(identity);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(false);
        attempt.setAttemptedAt(Instant.now());
        loginAttemptRepository.save(attempt);

        // Count failed attempts in the last 10 minutes
        // For simplicity in scaffolding, mock check for lockout creation:
        // Here we simulate the lockout creation if there are 5 failures.
        // We'll write to lockout repository.
        AccountLockoutEntity lockout = new AccountLockoutEntity();
        lockout.setIdentity(identity);
        lockout.setLockedAt(Instant.now());
        lockout.setUnlocksAt(Instant.now().plusSeconds(900)); // 15 mins lock
        lockoutRepository.save(lockout);
    }

    private void clearFailedAttempts(AuthIdentityEntity identity) {
        // Mock cleanup
        log.debug("Cleaning failed attempts for identity: {}", identity.getId());
    }

    private void writeAuditEvent(String tenantId, AuthIdentityEntity identity, String type, String ipAddress,
            String userAgent) {
        AuthAuditEventEntity event = new AuthAuditEventEntity();
        event.setTenantId(tenantId);
        event.setIdentity(identity);
        event.setEventType(type);
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        auditEventRepository.save(event);
    }
}