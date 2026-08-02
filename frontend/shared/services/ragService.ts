import { apiClient } from "./apiClient";

export interface SourceAttribution {
  documentId: string;
  versionId: string;
  title: string;
  snippet: string;
  relevanceScore: number;
}

export interface RagResponse {
  text: string;
  attributions: SourceAttribution[];
  metadata: Record<string, unknown>;
  status: string;
  tookMs: number;
}

export const ragService = {
  async generateResponse(query: string, searchType: string = "HYBRID", limit: number = 5): Promise<RagResponse> {
    const response = await apiClient.post("/rag/generate", {
      query,
      searchType,
      limit,
    });
    return response.data;
  },

  async streamResponse(
    query: string,
    onChunk: (chunk: string) => void,
    searchType: string = "HYBRID",
    limit: number = 5
  ): Promise<void> {
    const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";
    
    // Use Fetch API for streaming response chunk reader
    const response = await fetch(`${API_URL}/rag/stream`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ query, searchType, limit }),
    });

    if (!response.ok || !response.body) {
      throw new Error(`Streaming failed with status ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      const textChunk = decoder.decode(value, { stream: true });
      onChunk(textChunk);
    }
  },
};
