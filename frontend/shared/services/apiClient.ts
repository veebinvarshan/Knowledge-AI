import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { getDefaultStore } from "jotai";
import { accessTokenAtom, authStatusAtom, userProfileAtom } from "../../features/authentication/store/authAtom";

// Retrieve config from environment variables
const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";

export const apiClient = axios.create({
  baseURL: API_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

interface FailedRequest {
  resolve: (value: unknown) => void;
  reject: (error: unknown) => void;
  config: InternalAxiosRequestConfig;
}

let isRefreshing = false;
let failedQueue: FailedRequest[] = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.config.headers.Authorization = `Bearer ${token}`;
      apiClient(prom.config)
        .then((res) => prom.resolve(res))
        .catch((err) => prom.reject(err));
    }
  });
  failedQueue = [];
};

// Request Interceptor: Attach the in-memory access token to all outbound queries
apiClient.interceptors.request.use(
  (config) => {
    const store = getDefaultStore();
    const token = store.get(accessTokenAtom);
    
    if (token && !config.headers.Authorization) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Catch 401 errors, trigger refresh flow, queue subsequent failures, and replay
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config;
    if (!originalRequest) return Promise.reject(error);

    // If unauthorized and we haven't already retried this request
    if (error.response?.status === 401 && !(originalRequest as any)._retry) {
      // Prevent loop if the refresh request itself fails
      if (originalRequest.url?.includes("/api/auth/refresh") || originalRequest.url?.includes("/auth/refresh")) {
        return Promise.reject(error);
      }

      const store = getDefaultStore();

      if (isRefreshing) {
        // Queue the request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject, config: originalRequest });
        });
      }

      (originalRequest as any)._retry = true;
      isRefreshing = true;

      try {
        // Request token rotation from Next.js BFF endpoint
        const response = await axios.post("/api/auth/refresh");
        const { accessToken } = response.data;

        if (!accessToken) {
          throw new Error("No access token returned from refresh channel");
        }

        // Save access token to in-memory store
        store.set(accessTokenAtom, accessToken);
        store.set(authStatusAtom, "AUTHENTICATED");

        // Broadcast to other tabs
        const syncChannel = new BroadcastChannel("auth_session_sync");
        syncChannel.postMessage({ type: "REFRESH_SUCCESS", token: accessToken });
        syncChannel.close();

        // Process queued requests
        processQueue(null, accessToken);

        // Replay original request
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshErr) {
        // Refresh token failed/expired: clean local session
        processQueue(refreshErr as Error, null);
        
        store.set(accessTokenAtom, null);
        store.set(userProfileAtom, null);
        store.set(authStatusAtom, "UNAUTHENTICATED");

        // Broadcast logout event
        const syncChannel = new BroadcastChannel("auth_session_sync");
        syncChannel.postMessage({ type: "SESSION_EXPIRED" });
        syncChannel.close();

        // Redirect to session-expired
        if (typeof window !== "undefined") {
          window.location.href = "/session-expired";
        }

        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
