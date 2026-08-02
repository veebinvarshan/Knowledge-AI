"use client";

import React, { useEffect, useState } from "react";
import { useAtom, useSetAtom } from "jotai";
import { useRouter, usePathname } from "next/navigation";
import {
  accessTokenAtom,
  authStatusAtom,
  userProfileAtom,
  sessionExpiryWarningAtom,
} from "../../features/authentication/store/authAtom";
import { authService, decodeUserProfile } from "../../features/authentication/services/authService";
import LoadingOverlay from "../../components/feedback/LoadingOverlay";
import { AlertTriangle, Clock } from "lucide-react";

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useAtom(accessTokenAtom);
  const [userProfile, setUserProfile] = useAtom(userProfileAtom);
  const [authStatus, setAuthStatus] = useAtom(authStatusAtom);
  const [showWarning, setShowWarning] = useAtom(sessionExpiryWarningAtom);
  const [isExtending, setIsExtending] = useState(false);
  const router = useRouter();
  const pathname = usePathname();

  // 1. Session Hydration and Startup Silent Refresh
  useEffect(() => {
    let active = true;

    async function hydrateSession() {
      try {
        setAuthStatus("INITIALIZING");
        const { accessToken } = await authService.refresh();
        if (active) {
          setAccessToken(accessToken);
          const profile = decodeUserProfile(accessToken);
          setUserProfile(profile);
          setAuthStatus("AUTHENTICATED");
        }
      } catch (err) {
        if (active) {
          setAccessToken(null);
          setUserProfile(null);
          setAuthStatus("UNAUTHENTICATED");
        }
      }
    }

    hydrateSession();

    return () => {
      active = false;
    };
  }, [setAccessToken, setAuthStatus, setUserProfile]);

  // 2. Cross-Tab Session Sync using BroadcastChannel
  useEffect(() => {
    const syncChannel = new BroadcastChannel("auth_session_sync");

    syncChannel.onmessage = (event) => {
      const { type, token } = event.data;
      switch (type) {
        case "LOGIN_SUCCESS":
case "REFRESH_SUCCESS":
          if (token) {
            setAccessToken(token);
            setUserProfile(decodeUserProfile(token));
            setAuthStatus("AUTHENTICATED");
          }
          break;
        case "LOGOUT":
          setAccessToken(null);
          setUserProfile(null);
          setAuthStatus("UNAUTHENTICATED");
          router.push("/login");
          break;
        case "SESSION_EXPIRED":
          setAccessToken(null);
          setUserProfile(null);
          setAuthStatus("UNAUTHENTICATED");
          router.push("/session-expired");
          break;
        default:
          break;
      }
    };

    return () => {
      syncChannel.close();
    };
  }, [setAccessToken, setAuthStatus, setUserProfile, router]);

  // 3. Session Expiration & Auto-Refresh Timer Management
  useEffect(() => {
    if (!accessToken) return;

    // Decode token to get expiration time
    let expirationTime: number;
    try {
      const base64Url = accessToken.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const claims = JSON.parse(atob(base64));
      expirationTime = claims.exp * 1000; // convert to ms
    } catch (e) {
      console.error("Failed to parse token expiration", e);
      return;
    }

    const timeRemaining = expirationTime - Date.now();
    
    // Show warning modal 2 minutes (120,000 ms) before token expires
    const warningThreshold = 120000; 
    const warningDelay = Math.max(0, timeRemaining - warningThreshold);
    
    // Auto logout timer
    const logoutDelay = Math.max(0, timeRemaining);

    // Setup warning timer
    const warningTimer = setTimeout(() => {
      setShowWarning(true);
    }, warningDelay);

    // Setup logout timer
    const logoutTimer = setTimeout(async () => {
      setShowWarning(false);
      setAccessToken(null);
      setUserProfile(null);
      setAuthStatus("UNAUTHENTICATED");
      
      try {
        await authService.logout();
      } catch (e) {
        // Suppress network logs for safety
      }
      
      const syncChannel = new BroadcastChannel("auth_session_sync");
      syncChannel.postMessage({ type: "SESSION_EXPIRED" });
      syncChannel.close();
      
      router.push("/session-expired");
    }, logoutDelay);

    return () => {
      clearTimeout(warningTimer);
      clearTimeout(logoutTimer);
    };
  }, [accessToken, setShowWarning, setAccessToken, setAuthStatus, setUserProfile, router]);

  const extendSession = async () => {
    setIsExtending(true);
    try {
      const { accessToken } = await authService.refresh();
      setAccessToken(accessToken);
      setUserProfile(decodeUserProfile(accessToken));
      setAuthStatus("AUTHENTICATED");
      setShowWarning(false);
    } catch (err) {
      console.error("Failed to extend session:", err);
      // Invalidate session on failure
      setAccessToken(null);
      setUserProfile(null);
      setAuthStatus("UNAUTHENTICATED");
      router.push("/session-expired");
    } finally {
      setIsExtending(false);
    }
  };

  return (
    <>
      <LoadingOverlay isVisible={authStatus === "INITIALIZING"} message="Synchronizing enterprise security keys..." />
      
      {/* Session Timeout Warning Modal */}
      {showWarning && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/70 backdrop-blur-sm p-4 animate-fade-in">
          <div className="w-full max-w-md bg-slate-900 border border-amber-500/30 rounded-xl p-6 shadow-2xl space-y-6">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-amber-500/10 rounded-lg text-amber-500">
                <Clock className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-100">Session Expiring</h3>
                <p className="text-xs text-slate-400">Security Isolation Notice</p>
              </div>
            </div>
            
            <p className="text-xs leading-relaxed text-slate-300">
              For security compliance, your authentication lease is expiring soon due to token lifetime limits. Do you wish to extend the session?
            </p>

            <div className="flex items-center gap-3 justify-end">
              <button
                type="button"
                onClick={async () => {
                  setShowWarning(false);
                  await authService.logout();
                  setAccessToken(null);
                  setUserProfile(null);
                  setAuthStatus("UNAUTHENTICATED");
                  router.push("/login");
                }}
                className="px-4 py-2 border border-slate-800 hover:bg-slate-800 text-slate-300 text-xs font-semibold rounded-lg transition-all"
              >
                Log Out
              </button>
              <button
                type="button"
                onClick={extendSession}
                disabled={isExtending}
                className="px-4 py-2 bg-gradient-to-r from-amber-500 to-orange-600 text-slate-950 font-bold text-xs rounded-lg shadow-lg hover:opacity-95 disabled:opacity-50 transition-all"
              >
                {isExtending ? "Extending..." : "Extend Session"}
              </button>
            </div>
          </div>
        </div>
      )}

      {children}
    </>
  );
}
