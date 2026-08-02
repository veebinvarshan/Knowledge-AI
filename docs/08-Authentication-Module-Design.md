# Authentication Module Design Specification
## Document ID: PL-DSN-AUTH-001 | Version: 1.0.0 | Status: DRAFT-FOR-REVIEW
## Target Module: `com.enterprise.platform.modules.authentication`

---

## 1. Module Boundaries & Responsibilities

The Authentication (`authentication`) module is strictly isolated from authorization, identity, and organization directory engines:

```
+-------------------------------------------------------------------------------------------------+
|                                       AUTHENTICATION MODULE                                     |
|                                                                                                 |
|   * Verify Identity (SSO, Credentials)        * Manage JWT Sessions (RSA-256 signatures)       |
|   * Execute Token Rotation & Revocation       * Enforce Lockouts & Hashing Policies             |
+-----------------------------------------------+-------------------------------------------------+
                                                |
                                                v (Emits events: UserLoggedIn, TokenRefreshed)
+-----------------------------------------------+-------------------------------------------------+
|                                       EXTERNAL MODULE BOUNDS                                    |
|                                                                                                 |
|   +-----------------------+   +-----------------------+   +-----------------------+             |
|   |   users-module        |   |   org-module          |   |   authorization-mod   |             |
|   | (Profile fields, ID)  |   | (Tenant limits, seats)|   | (RBAC roles mapping)  |             |
|   +-----------------------+   +-----------------------+   +-----------------------+             |
+-------------------------------------------------------------------------------------------------+
```

### 1.1 In-Scope Responsibilities
* **Credential Verification:** Validate local fallback passwords and external SAML/OIDC SSO assertions.
* **Token Operations:** Issue, verify, rotate, and revoke Access Tokens and Refresh Tokens.
* **Session Lifecycle:** Tracks active logins, concurrent session limits, and token-revocation blacklists.
* **Credential Security:** Enforce password strength rules, BCrypt hashing algorithms, and lockout counters.
* **Account Recovery:** Orchestrate forgot/reset password challenges and email validations.

### 1.2 Out-of-Scope (Delegated) Responsibilities
* **Authorization (`authorization` module):** Mapping roles to resources and verifying user clearances.
* **User Management (`users` module):** Managing profile attributes (emails, names, job titles) and user preferences.
* **Tenant Setup (`organizations` module):** Checking active organization accounts, seat limits, and billing states.
* **User Provisioning:** Handling account signups, invite processes, and team updates.

---

## 2. Authentication Flows

```
[Registration] ---> Send Verification Mail ---> [Verify Email] ---> Active Account
                                                                       |
[Login (Credentials/SSO)] <--- Locked Account <--- [Lockout Policy] <--+
      |
      v
  Issue JWT  ---> [Session Validate] ---> [Token Refresh] ---> Session Invalidation / [Logout]
```

### 2.1 User Registration Flow
1. User submits email, password, and tenant subdomain indicator.
2. System validates subdomain exists, checks for duplicate email within tenant.
3. Password is checked for complexity, hashed with BCrypt, and written to database (with status set to `PENDING-VERIFICATION`).
4. System publishes `EmailVerificationRequested` event containing an ephemeral verification token (expires in 2 hours).
5. Notification module dispatches an email containing the validation hyperlink.

### 2.2 Login Flow (SSO / SAML / Password fallback)
* **SSO / SAML Path:**
  1. Next.js triggers login by navigating to `/api/v1/auth/sso/saml/login?tenant=[subdomain]`.
  2. Spring Boot redirects browser to the mapped SAML Identity Provider (IdP) URL.
  3. IdP returns a signed SAML XML assertion to the Spring Boot endpoint `/api/v1/auth/sso/saml/callback`.
  4. Spring Boot verifies the signature against the organization's public key, extracts claims (email, name, roles), maps it to a local user ID, and issues tokens.
* **Credentials Fallback Path (Development/Emergency):**
  1. User submits email and password parameters.
  2. System verifies user account exists and is not locked.
  3. Hashed password verification is performed. If verification fails, login counters are updated.
  4. If successful, session tokens are returned.

### 2.3 Logout Flow
1. Client calls `/api/v1/auth/logout` supplying active JWT session cookie and Refresh Token headers.
2. System extracts token identifiers, adds active Access Token signatures to the Redis blacklist (for remaining TTL window), and deletes Refresh Token rows in the database.
3. System deletes the HttpOnly session cookie, returning a `200 OK` status response.

### 2.4 Token Refresh Flow
1. Client requests token replacement from `/api/v1/auth/refresh` sending the Refresh Token in the request header.
2. System validates the token signature, checks if the Refresh Token is blacklisted in Redis or deleted from the database.
3. System executes rotation check: issues a new Access Token and rotates the Refresh Token, invalidating the old Refresh Token signature.
4. Active session metrics are updated in Redis.

### 2.5 Forgot & Reset Password Flow
1. User submits their email parameter.
2. System looks up account. If found, generates an ephemeral password reset token (expires in 30 minutes) and logs the request.
3. System fires a `PasswordResetRequested` event, triggering the mailer pipeline.
4. User receives mail, navigates to UI link, and inputs a new password alongside the reset token.
5. System verifies token validity, validates new password complexity, hashes the password, writes updates to the database, and immediately invalidates active user tokens.

### 2.6 Email Verification Flow
1. User navigates to the verification link `/verify?token=[token-uuid]`.
2. Spring Boot extracts token, checks expiration, and validates active states.
3. If valid, the user's status is set to `ACTIVE` in the database, and the validation token is deleted.
4. Emits `EmailVerified` event.

### 2.7 Change Password Flow
1. Authenticated user submits their old password and new password parameters.
2. System verifies the old password matches the database hash.
3. System checks new password complexity, saves the new hash, and records the change in password history tables.
4. System revokes all active tokens for the user, requiring a fresh login.

### 2.8 Session Validation Flow
1. Spring Security filter intercepts inbound request, extracting JWT cookie.
2. Filter validates signature, checks Redis blacklist status, and checks expiration bounds.
3. If valid, user context is injected into the request security context.

---

## 3. JWT & Caching Strategy

```
Access Token (Stored in HttpOnly Cookie)
├── Header: RS256, type: JWT
└── Claims: Sub (User ID), Tenant, Email, Roles, Exp (15 Mins)

Refresh Token (Stored in Secure HTTP Header / Database)
├── Cryptographically secure random UUID
└── Database Map: User ID, Tenant ID, Expiration (7 Days), Rotated State
```

### 3.1 Token Details & Signatures
* **Signing Algorithm:** RSA-256 (RS256) signature scheme. Private keys remain secure inside Spring Boot configurations. Public keys are exposed via JWKS endpoints.
* **Access Token:**
  * Lifetime: **15 minutes**.
  * Storage: Set as a `Secure`, `HttpOnly`, `SameSite=Strict` browser cookie.
  * Claims Schema:
    ```json
    {
      "sub": "usr_99f2a3821a",
      "iss": "theplatform.com",
      "tenant_id": "org_acme_corp",
      "email": "david.chen@acme.com",
      "roles": ["ROLE_VIEWER", "ROLE_EDITOR"],
      "exp": 1782500000
    }
    ```
* **Refresh Token:**
  * Lifetime: **7 days**.
  * Format: Cryptographically secure random UUID.
  * Storage: Persisted in the database. Client includes it in HTTP header requests to the refresh endpoint.

### 3.2 Token Rotation, Revocation, & Blacklisting
* **Rotation:** Every call to the refresh endpoint rotates the Refresh Token. The previous token is marked as rotated in the database. If a rotated token is reused, it indicates a replay exploit: the system invalidates all tokens in the user's family and logs a security alert.
* **Revocation & Caching:** Revoked Access Tokens are cached in Redis. The blacklist key format is `blacklist:token:[jti]`, with a TTL equal to the token's remaining lifetime. The authorization filter checks this Redis cache before parsing token contents.

---

## 4. Session Strategy

* **Concurrent Logins Limit:** Organizations can define seat limits (e.g., maximum 3 concurrent active sessions per user account). If limits are exceeded during login, the oldest session is revoked.
* **Remember Me:** Remember Me functionality uses the Refresh Token lifespan to maintain sessions. Next.js automatically refreshes tokens in the background as long as the Refresh Token cookie remains valid.
* **Device Tracking:** During authentication, the system logs client properties (IP addresses, User-Agent strings, and localization details), linking them to active Refresh Tokens. If client locations shift unexpectedly, the session triggers MFA or requires re-authentication.

---

## 5. Password & Lockout Policy

* **Strength Requirements:** Passwords must meet complexity requirements:
  * Minimum 12 characters, maximum 128 characters.
  * Must contain at least one uppercase letter, one lowercase letter, one number, and one special character.
* **Hashing Algorithm:** Hashed using **BCrypt** with a work factor cost of **12**.
* **Password History:** The system tracks the last 5 password hashes in history tables, preventing users from reusing recent passwords.
* **Lockout Policy:**
  * Account lock is triggered after 5 consecutive failed login attempts within 10 minutes.
  * Initial lockout duration: **15 minutes**.
  * Repeat lockouts within 24 hours double the duration up to a maximum of 24 hours.
* **Rate Limiting:** Login endpoints are rate-limited in Redis, restricting IPs to 10 login requests per minute.

---

## 6. Authentication Events

The module decouples operations using Spring Application Events:

```
[Login Action] ---> Security Endpoint ---> Publish UserLoggedIn Event
                                                  |
                                                  +---> Trigger AuditLogs Module
                                                  +---> Trigger Analytics Telemetry
```

* `UserLoggedIn`: Emitted after successful credentials or SAML validation. Triggers audit logging and analytics telemetry.
* `UserLoggedOut`: Emitted during session termination. Invalidates cache entries.
* `PasswordChanged`: Emitted when a password is updated. Triggers security notification emails.
* `PasswordResetRequested`: Emitted during forgot-password submissions. Triggers email delivery.
* `EmailVerified`: Emitted on validation link clicks. Activates account state.
* `TokenRefreshed`: Emitted during token rotation. Logs active session updates.

---

## 7. REST API Specifications

### 7.1 POST `/api/v1/auth/login`
* **Purpose:** Authenticate user using local password fallback.
* **Request Schema:**
  ```json
  {
    "email": "user@domain.com",
    "password": "Password123!",
    "tenant": "acme"
  }
  ```
* **Response Schema (200 OK):**
  ```json
  {
    "accessToken": "eyJhbGciOiJSUzI1NiIs...",
    "refreshToken": "ef92a12a-881c-4392-a1f9-cf9a0a19001f",
    "expiresIn": 900
  }
  ```
* **Validation Rules:** Email must be well-formed; password cannot be blank.
* **Error Responses:**
  * `400 Bad Request`: Input validation failed.
  * `401 Unauthorized`: Mismatched credentials or locked account.

### 7.2 POST `/api/v1/auth/refresh`
* **Purpose:** Rotate access and refresh tokens.
* **Request Header:** `Authorization: Bearer ef92a12a-881c-4392-a1f9-cf9a0a19001f`
* **Response Schema (200 OK):**
  ```json
  {
    "accessToken": "eyJhbGciOiJSUzI1NiIs...",
    "refreshToken": "bc82a12a-771c-3292-a1f8-df9a0a29002f",
    "expiresIn": 900
  }
  ```
* **Error Responses:**
  * `401 Unauthorized`: Expired or rotated refresh token.

### 7.3 POST `/api/v1/auth/logout`
* **Purpose:** Terminate active sessions.
* **Request Header:** `Authorization: Bearer [refresh-token]`
* **Response Schema:** `200 OK` (deletes session cookies).

---

## 8. Security Controls & Threat Mitigations

* **CSRF (Cross-Site Request Forgery):** Access tokens are stored in HttpOnly cookies with `SameSite=Strict` flags. API requests are protected against CSRF via header validation.
* **Brute-Force & Credential Stuffing:** Rate-limiting policies lock IPs and user accounts in Redis, mitigating brute-force and credential stuffing threats.
* **Session Hijacking:** JWT payloads include device properties (hashed IP and User-Agent). The filter validates these values on every request.
* **Replay Exploits:** The system enforces Refresh Token reuse detection. Any double use of a Refresh Token triggers the immediate invalidation of the entire token family.

---

## 9. Frontend Integration Flow

```
Next.js Client
  |
  +---> Route Guard (Checks Context State)
  |       |
  |       +---> [Authenticated] ---> Allow View
  |       +---> [Unauthenticated] -> Read HttpOnly Access Cookie
  |                                    |
  |                                    +---> [Expired/Missing] ---> Post Refresh API
```

* **Route Protection:** Next.js middleware inspects route metadata. Protected routes require active JWT contexts, redirecting unauthenticated users to `/login`.
* **Token Refresh Interceptors:** The frontend API client (Axios wrapper) checks response status codes. If a request returns `401 Unauthorized` due to token expiration, the client queues requests, triggers a POST to `/refresh`, and retries the original requests with the updated token.
* **Public Routes:** `/` (Landing Home), `/login` (SSO auth stage), `/not-found`, and `/error` are configured to bypass middleware route guards.

---

## 10. Testing Strategy

* **Unit Testing:** Focuses on JWT validation logic, BCrypt hashing verification, password complexity checks, and parsing token assertions.
* **Integration Testing:** Uses Testcontainers to run local PostgreSQL and Redis databases to verify repository methods, user lockout transitions, and Redis caching.
* **Security Testing:** Validates token replay mitigations, concurrent session limits, and authentication bypass attempts.
* **Performance Testing:** Load-tests the `/login` and `/refresh` endpoints to ensure response latencies remain stable during peak traffic.
