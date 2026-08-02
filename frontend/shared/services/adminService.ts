import axios from "axios";

export interface HealthProbeResult {
  status: "UP" | "DOWN" | "DEGRADED";
  components?: Record<string, { status: string }>;
}

export const adminService = {
  async checkHealth(): Promise<HealthProbeResult> {
    const API_BASE = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1").replace("/api/v1", "");
    try {
      const response = await axios.get(`${API_BASE}/actuator/health`);
      return response.data;
    } catch {
      return { status: "DOWN" };
    }
  },

  async checkReadiness(): Promise<HealthProbeResult> {
    const API_BASE = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1").replace("/api/v1", "");
    try {
      const response = await axios.get(`${API_BASE}/actuator/health/readiness`);
      return response.data;
    } catch {
      return { status: "DOWN" };
    }
  },

  async checkLiveness(): Promise<HealthProbeResult> {
    const API_BASE = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1").replace("/api/v1", "");
    try {
      const response = await axios.get(`${API_BASE}/actuator/health/liveness`);
      return response.data;
    } catch {
      return { status: "DOWN" };
    }
  },
};
