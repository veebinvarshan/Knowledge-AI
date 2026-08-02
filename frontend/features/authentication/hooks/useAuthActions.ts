import { useAtom, useSetAtom } from "jotai";
import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  accessTokenAtom,
  authStatusAtom,
  userProfileAtom,
  UserProfile,
} from "../store/authAtom";
import { authService, decodeUserProfile } from "../services/authService";

export function useAuthActions() {
  const [accessToken, setAccessToken] = useAtom(accessTokenAtom);
  const [userProfile, setUserProfile] = useAtom(userProfileAtom);
  const [authStatus, setAuthStatus] = useAtom(authStatusAtom);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const router = useRouter();

  const handleLogin = async (email: string, password: string, tenant: string) => {
    setIsLoading(true);
    setError(null);
    setAuthStatus("AUTHENTICATING");
    try {
      const { accessToken } = await authService.login(email, password, tenant);
      setAccessToken(accessToken);
      
      const profile = decodeUserProfile(accessToken);
      setUserProfile(profile);
      setAuthStatus("AUTHENTICATED");
      
      // Notify other tabs
      const syncChannel = new BroadcastChannel("auth_session_sync");
      syncChannel.postMessage({ type: "LOGIN_SUCCESS", token: accessToken });
      syncChannel.close();

      return profile;
    } catch (err: any) {
      const errMsg = err.response?.data?.error || "Invalid credentials or login failure";
      setError(errMsg);
      setAuthStatus("UNAUTHENTICATED");
      throw new Error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    setIsLoading(true);
    try {
      await authService.logout();
    } catch (err) {
      console.error("Logout network warning:", err);
    } finally {
      // Clear in-memory state regardless of network response
      setAccessToken(null);
      setUserProfile(null);
      setAuthStatus("UNAUTHENTICATED");

      // Broadcast to other tabs
      const syncChannel = new BroadcastChannel("auth_session_sync");
      syncChannel.postMessage({ type: "LOGOUT" });
      syncChannel.close();

      setIsLoading(false);
      router.push("/login");
    }
  };

  const handleRefresh = async () => {
    setError(null);
    try {
      const { accessToken } = await authService.refresh();
      setAccessToken(accessToken);
      
      const profile = decodeUserProfile(accessToken);
      setUserProfile(profile);
      setAuthStatus("AUTHENTICATED");
      return accessToken;
    } catch (err) {
      setAccessToken(null);
      setUserProfile(null);
      setAuthStatus("UNAUTHENTICATED");
      throw err;
    }
  };

  const handleRegister = async (email: string, password: string, tenant: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await authService.register(email, password, tenant);
    } catch (err: any) {
      const errMsg = err.response?.data?.error || "Registration request rejected";
      setError(errMsg);
      throw new Error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleForgotPassword = async (email: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await authService.forgotPassword(email);
    } catch (err: any) {
      const errMsg = err.response?.data?.error || "Failed to process recovery request";
      setError(errMsg);
      throw new Error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetPassword = async (token: string, newPassword: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await authService.resetPassword(token, newPassword);
    } catch (err: any) {
      const errMsg = err.response?.data?.error || "Password reset token invalid or expired";
      setError(errMsg);
      throw new Error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyEmail = async (token: string) => {
    setIsLoading(true);
    setError(null);
    try {
      await authService.verifyEmail(token);
    } catch (err: any) {
      const errMsg = err.response?.data?.error || "Email verification token invalid or expired";
      setError(errMsg);
      throw new Error(errMsg);
    } finally {
      setIsLoading(false);
    }
  };

  return {
    accessToken,
    userProfile,
    authStatus,
    error,
    isLoading,
    login: handleLogin,
    logout: handleLogout,
    refresh: handleRefresh,
    register: handleRegister,
    forgotPassword: handleForgotPassword,
    resetPassword: handleResetPassword,
    verifyEmail: handleVerifyEmail,
  };
}
