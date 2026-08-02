import axios from "axios";
import { apiClient } from "../../../shared/services/apiClient";
import { UserProfile } from "../store/authAtom";

export interface LoginResponse {
  accessToken: string;
  expiresIn: number;
}

export interface RegisterResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

// Utility to decode basic user info from JWT claims
export function decodeUserProfile(token: string): UserProfile | null {
  try {
    const base64Url = token.split(".")[1];
    if (!base64Url) return null;
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    const claims = JSON.parse(jsonPayload);
    
    // Extract subject as email, role from roles list (first role or defaults)
    const email = claims.sub || "";
    const role = Array.isArray(claims.roles) && claims.roles.length > 0 ? claims.roles[0] : "ROLE_USER";
    const tenantId = claims.tenant_id || "";

    return {
      id: email, // use email as ID for local purposes
      email,
      role,
      tenantId,
    };
  } catch (error) {
    console.error("Failed to decode JWT claims:", error);
    return null;
  }
}

export const authService = {
  /**
   * Log in user using credentials via BFF route.
   */
  async login(email: string, password: string, tenant: string): Promise<LoginResponse> {
    const response = await axios.post<LoginResponse>("/api/auth/login", {
      email,
      password,
      tenant,
    });
    return response.data;
  },

  /**
   * Refresh current active session via BFF route.
   */
  async refresh(): Promise<LoginResponse> {
    const response = await axios.post<LoginResponse>("/api/auth/refresh");
    return response.data;
  },

  /**
   * Terminate user session and clear BFF cookies.
   */
  async logout(): Promise<void> {
    await axios.post("/api/auth/logout");
  },

  /**
   * Register a new user credential on the backend database.
   */
  async register(email: string, password: string, tenant: string): Promise<void> {
    await apiClient.post("/auth/register", {
      email,
      password,
      tenant,
    });
  },

  /**
   * Request a password reset link to be sent to user email.
   */
  async forgotPassword(email: string): Promise<void> {
    await apiClient.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);
  },

  /**
   * Submit new credentials using the validation token.
   */
  async resetPassword(token: string, newPassword: string): Promise<void> {
    await apiClient.post("/auth/reset-password", {
      token,
      newPassword,
    });
  },

  /**
   * Submit email validation request.
   */
  async verifyEmail(token: string): Promise<void> {
    await apiClient.get(`/auth/verify-email?token=${encodeURIComponent(token)}`);
  },

  /**
   * Change current authenticated password.
   */
  async changePassword(password: string, newPassword: string): Promise<void> {
    await apiClient.post("/auth/change-password", {
      password,
      newPassword,
    });
  }
};
