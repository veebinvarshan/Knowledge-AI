# Authentication Database Design Specification
## Document ID: PL-DSN-DB-001 | Version: 1.0.0 | Status: DRAFT-FOR-REVIEW
## Target Module: `com.enterprise.platform.modules.authentication`

---

## 1. Database Model Overview

This specification defines the relational database model for the **Authentication** module of the Enterprise AI Knowledge Management Platform. The design leverages **PostgreSQL** features (such as UUIDs and JSONB configurations) to establish a highly performant, secure, and extensible data schema.

### Conceptual Schema Layout
```
+------------------+          +-------------------+          +-------------------+
|     tenants      | -------> |  auth_identities  | -------> | auth_credentials  |
| (Existing global)|          |   (User Identity) |          | (Hashed Passwords)|
+------------------+          +---------+---------+          +-------------------+
                                        |
                 +----------------------+----------------------+
                 |                      |                      |
                 v                      v                      v
      +----------+-------+   +----------+-------+   +----------+-------+
      |  refresh_tokens  |   |  active_sessions |   |  identity_links  |
      | (Rotation tokens)|   | (Active sessions)|   |  (SSO mappings)  |
      +------------------+   +----------+-------+   +------------------+
                                        |
                                        v
                             +----------+-------+
                             |   auth_devices   |
                             |  (Fingerprints)  |
                             +------------------+
```

---

## 2. Table Specifications

### 2.1 Table: `auth_identities`
* **Purpose:** Represents the core authentication identity for a user. It stores only user status and tenant routing metrics. User profile attributes (name, avatar, title) are delegated to the `users` module.
* **Soft Delete Strategy:** Soft delete via `deleted_at` timestamp.
* **Retention Policy:** Kept indefinitely unless hard-purged after soft-deletion timeout (30 days).

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `tenant_id` | `VARCHAR(64)` | No | None | FOREIGN KEY -> `tenants(id)` |
| `email` | `VARCHAR(255)` | No | None | UNIQUE with `tenant_id`, Lowercase constraint |
| `status` | `VARCHAR(32)` | No | `'PENDING_VERIFICATION'` | CHECK (`status` IN ('PENDING_VERIFICATION', 'ACTIVE', 'LOCKED', 'SUSPENDED')) |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |
| `deleted_at` | `TIMESTAMP WITH TIME ZONE` | Yes | `NULL` | None |

* **Indexes:**
  * `idx_auth_identities_tenant_email` (UNIQUE): B-Tree on `(tenant_id, email)` WHERE `deleted_at IS NULL`.
* **Unique Constraints:** `uq_auth_identities_tenant_email` on `(tenant_id, email)`.

---

### 2.2 Table: `auth_credentials`
* **Purpose:** Stores hashed passwords for identities using local password fallback.
* **Soft Delete Strategy:** Not applicable. Deleted immediately if user identity is removed.
* **Retention Policy:** Only the active credential hash and history window are retained.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | UNIQUE, FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `password_hash` | `VARCHAR(255)` | No | None | None |
| `password_history` | `JSONB` | No | `'[]'::jsonb` | Array containing up to 5 previous hashed strings |
| `password_changed_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |

* **Indexes:**
  * `idx_auth_credentials_identity`: B-Tree on `(identity_id)`.

---

### 2.3 Table: `refresh_tokens`
* **Purpose:** Stores rotated refresh tokens linked to active login chains.
* **Soft Delete Strategy:** Hard delete on revocation or expiration.
* **Retention Policy:** Automatically pruned 7 days after expiration.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `token_value` | `UUID` | No | `gen_random_uuid()` | UNIQUE |
| `rotated` | `BOOLEAN` | No | `FALSE` | None |
| `parent_token_id` | `UUID` | Yes | `NULL` | FOREIGN KEY -> `refresh_tokens(id)` ON DELETE SET NULL |
| `expires_at` | `TIMESTAMP WITH TIME ZONE` | No | None | None |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |

* **Indexes:**
  * `idx_refresh_tokens_value` (UNIQUE): B-Tree on `(token_value)`.
  * `idx_refresh_tokens_identity`: B-Tree on `(identity_id)`.
  * `idx_refresh_tokens_expiry`: B-Tree on `(expires_at)`.

---

### 2.4 Table: `active_sessions`
* **Purpose:** Tracks active user login sessions.
* **Soft Delete Strategy:** Hard delete on logout or invalidation.
* **Retention Policy:** Deleted immediately on logout or session expiration.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `device_id` | `UUID` | No | None | FOREIGN KEY -> `auth_devices(id)` ON DELETE CASCADE |
| `expires_at` | `TIMESTAMP WITH TIME ZONE` | No | None | None |
| `last_active_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |

* **Indexes:**
  * `idx_active_sessions_identity`: B-Tree on `(identity_id)`.
  * `idx_active_sessions_device`: B-Tree on `(device_id)`.

---

### 2.5 Table: `auth_devices`
* **Purpose:** Stores details and fingerprints of devices used to access the system.
* **Soft Delete Strategy:** Not applicable.
* **Retention Policy:** Pruned if inactive for more than 180 days.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `fingerprint_hash` | `VARCHAR(64)` | No | None | Hash of user agent and browser metrics |
| `device_name` | `VARCHAR(100)` | No | `'Unknown Device'` | None |
| `last_ip_address` | `VARCHAR(45)` | No | None | Supports IPv4 and IPv6 |
| `last_seen_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |

* **Indexes:**
  * `idx_auth_devices_identity_fingerprint` (UNIQUE): B-Tree on `(identity_id, fingerprint_hash)`.

---

### 2.6 Table: `email_verification_tokens`
* **Purpose:** Stores verification tokens sent to users during registration.
* **Soft Delete Strategy:** Hard delete on completion.
* **Retention Policy:** Deleted immediately on verification. Unverified entries expire after 2 hours and are auto-pruned.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | UNIQUE, FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `token_value` | `UUID` | No | `gen_random_uuid()` | UNIQUE |
| `expires_at` | `TIMESTAMP WITH TIME ZONE` | No | None | None |

* **Indexes:**
  * `idx_email_verification_token_value` (UNIQUE): B-Tree on `(token_value)`.

---

### 2.7 Table: `password_reset_tokens`
* **Purpose:** Stores validation tokens for account recovery requests.
* **Soft Delete Strategy:** Hard delete on consumption.
* **Retention Policy:** Expire and are deleted 30 minutes after issuance.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `token_value` | `UUID` | No | `gen_random_uuid()` | UNIQUE |
| `expires_at` | `TIMESTAMP WITH TIME ZONE` | No | None | None |

* **Indexes:**
  * `idx_password_reset_token_value` (UNIQUE): B-Tree on `(token_value)`.

---

### 2.8 Table: `login_attempts`
* **Purpose:** Tracks consecutive failed login attempts per user identity or IP address.
* **Soft Delete Strategy:** Hard delete after lockout reset.
* **Retention Policy:** Entries older than 24 hours are auto-pruned.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | Yes | `NULL` | FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `ip_address` | `VARCHAR(45)` | No | None | None |
| `attempted_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |
| `is_successful` | `BOOLEAN` | No | `FALSE` | None |

* **Indexes:**
  * `idx_login_attempts_identity_time`: B-Tree on `(identity_id, attempted_at)`.
  * `idx_login_attempts_ip_time`: B-Tree on `(ip_address, attempted_at)`.

---

### 2.9 Table: `account_lockouts`
* **Purpose:** Records active lock states for locked identities.
* **Soft Delete Strategy:** Hard delete when lockout window expires or is manually cleared.
* **Retention Policy:** Kept in historical logging for audit; active lockout checks use expiration targets.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `locked_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |
| `unlocks_at` | `TIMESTAMP WITH TIME ZONE` | No | None | None |
| `reason` | `VARCHAR(100)` | No | `'MAX_FAILED_ATTEMPTS'` | None |

* **Indexes:**
  * `idx_account_lockouts_identity_active`: B-Tree on `(identity_id, unlocks_at)`.

---

### 2.10 Table: `oauth_identity_links`
* **Purpose:** Maps local identities to external OAuth / OIDC Identity Provider profiles.
* **Soft Delete Strategy:** Not applicable.
* **Retention Policy:** Retained until identity is unlinked by user or administrator.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `identity_id` | `UUID` | No | None | FOREIGN KEY -> `auth_identities(id)` ON DELETE CASCADE |
| `provider_name` | `VARCHAR(50)` | No | None | e.g. `'GOOGLE'`, `'OKTA'`, `'GITHUB'` |
| `provider_user_id` | `VARCHAR(100)` | No | None | Unique user ID from IdP |
| `provider_metadata` | `JSONB` | No | `'{}'::jsonb` | Additional fields (e.g. token scopes) |
| `linked_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |

* **Indexes:**
  * `idx_oauth_identity_provider_user` (UNIQUE): B-Tree on `(provider_name, provider_user_id)`.
  * `idx_oauth_identity_link_identity`: B-Tree on `(identity_id)`.

---

### 2.11 Table: `auth_audit_events`
* **Purpose:** Stores audit logs of authentication and session lifecycle changes.
* **Soft Delete Strategy:** Prohibited. Database rules must deny UPDATE or DELETE operations on this table.
* **Retention Policy:** Kept for 1 year before archiving to cold storage.

| Column Name | Data Type | Nullable | Default Value | Constraints |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `UUID` | No | `gen_random_uuid()` | PRIMARY KEY |
| `tenant_id` | `VARCHAR(64)` | No | None | FOREIGN KEY -> `tenants(id)` |
| `identity_id` | `UUID` | Yes | `NULL` | FOREIGN KEY -> `auth_identities(id)` ON DELETE SET NULL |
| `event_type` | `VARCHAR(50)` | No | None | e.g., `'LOGIN_SUCCESS'`, `'PASSWORD_RESET'` |
| `ip_address` | `VARCHAR(45)` | No | None | None |
| `user_agent` | `VARCHAR(255)` | Yes | `NULL` | None |
| `event_details` | `JSONB` | No | `'{}'::jsonb` | Structured metadata parameters |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | No | `CURRENT_TIMESTAMP` | None |

* **Indexes:**
  * `idx_auth_audit_tenant_time`: B-Tree on `(tenant_id, created_at)`.
  * `idx_auth_audit_identity_time`: B-Tree on `(identity_id, created_at)`.

---

## 3. Entity Relationships

1. **`auth_identities` (1) : (1) `auth_credentials`**
   * *Rationale:* An identity has at most one fallback credentials password profile. Decoupled to keep domain boundaries clean and allow passwordless login flows.
2. **`auth_identities` (1) : (Many) `refresh_tokens`**
   * *Rationale:* A user can log in from multiple devices, each maintaining a separate refresh token chain.
3. **`auth_identities` (1) : (Many) `active_sessions`**
   * *Rationale:* A user can run multiple active concurrent browser sessions across workspaces.
4. **`active_sessions` (Many) : (1) `auth_devices`**
   * *Rationale:* Multiple active sessions (e.g. different tabs or workspace states) can be associated with a single physical device.
5. **`auth_identities` (1) : (Many) `oauth_identity_links`**
   * *Rationale:* Users can link multiple external identity providers (e.g. Google and Okta) to a single local platform identity.
6. **`auth_identities` (1) : (Many) `auth_audit_events`**
   * *Rationale:* A complete history of security actions is recorded for compliance tracking.

---

## 4. Security Specifications

* **Password Hashing:** Passwords must be hashed using BCrypt. Direct database writes must fail if password length is under 12 characters or contains raw text indicators.
* **Token Protection:** Refresh tokens are stored as cryptographically secure random UUIDs in the database. Raw Access Tokens (JWTs) are never saved in the database, reducing database breach risks.
* **Device Fingerprinting:** Devices are fingerprinted using a SHA-256 hash of:
  $$\text{Fingerprint} = \text{SHA-256}(\text{UserAgent} + \text{AcceptLanguage} + \text{ScreenResolution})$$
  This fingerprint is checked on every refresh request. If client fingerprints mismatch during session use, the system revokes the session and triggers a security event.
* **PII Protection:** Email columns inside `auth_identities` are stored in lowercase, indexed, and can be encrypted using database column-level encryption configurations in high-compliance environments.
* **Audit Immutability:** A PostgreSQL trigger blocks updates and deletes on `auth_audit_events`:
  ```sql
  CREATE TRIGGER trg_prevent_audit_updates
  BEFORE UPDATE OR DELETE ON auth_audit_events
  FOR EACH ROW EXECUTE FUNCTION prevent_write_action();
  ```

---

## 5. Performance & Indexing Strategy

To maintain sub-second response times, we recommend the following indexes:

### 5.1 `idx_auth_identities_tenant_email`
* **Type:** B-Tree
* **Definition:** `CREATE UNIQUE INDEX idx_auth_identities_tenant_email ON auth_identities (tenant_id, LOWER(email)) WHERE deleted_at IS NULL;`
* **Justification:** Ensures $O(1)$ email lookup during login and registration. The `LOWER` function prevents casing injection duplicates.

### 5.2 `idx_refresh_tokens_value`
* **Type:** B-Tree
* **Definition:** `CREATE UNIQUE INDEX idx_refresh_tokens_value ON refresh_tokens (token_value);`
* **Justification:** Optimizes token verification during refresh requests.

### 5.3 `idx_active_sessions_identity`
* **Type:** B-Tree
* **Definition:** `CREATE INDEX idx_active_sessions_identity ON active_sessions (identity_id);`
* **Justification:** Speeds up concurrent session checks during login and allows bulk session invalidations when users log out of all devices.

### 5.4 `idx_auth_devices_identity_fingerprint`
* **Type:** B-Tree
* **Definition:** `CREATE UNIQUE INDEX idx_auth_devices_identity_fingerprint ON auth_devices (identity_id, fingerprint_hash);`
* **Justification:** Allows fast device fingerprint checks during refresh requests.

---

## 6. Future Compatibility & Extensions

### 6.1 Multi-Factor Authentication (MFA)
MFA is supported without database changes by adding a sub-table `auth_mfa_factors`:
```sql
CREATE TABLE auth_mfa_factors (
    id UUID PRIMARY KEY,
    identity_id UUID REFERENCES auth_identities(id),
    factor_type VARCHAR(32) NOT NULL, -- 'TOTP', 'SMS'
    secret_key VARCHAR(255) NOT NULL,
    verified BOOLEAN DEFAULT FALSE
);
```

### 6.2 Passkeys / WebAuthn
Passkey credentials can be added via an `auth_passkeys` table mapping credentials back to `auth_identities.id`:
```sql
CREATE TABLE auth_passkeys (
    id UUID PRIMARY KEY,
    identity_id UUID REFERENCES auth_identities(id),
    credential_id BYTEA UNIQUE NOT NULL,
    public_key BYTEA NOT NULL,
    sign_count INT NOT NULL
);
```

### 6.3 Enterprise SSO & Multi-Tenancy
The presence of `tenant_id` inside the primary `auth_identities` table ensures that multiple organizations can coexist in the same database without data leakage. The `oauth_identity_links` table supports integrating multiple OIDC/SAML configurations simultaneously.

---

## 7. Data Lifecycle & Archiving Strategy

* **Active Data Window:** Active sessions and verification tokens are deleted immediately upon consumption or expiration to keep tables clean.
* **Audit Retention:** Authentication audit logs are kept in PostgreSQL for **365 days** to support real-time reporting.
* **Archiving Process:** A nightly automated cron job moves log records older than 365 days to secure cold storage (compressed JSON files on S3/Block storage):
  ```sql
  INSERT INTO archive_auth_audit_events SELECT * FROM auth_audit_events WHERE created_at < NOW() - INTERVAL '1 year';
  DELETE FROM auth_audit_events WHERE created_at < NOW() - INTERVAL '1 year';
  ```
