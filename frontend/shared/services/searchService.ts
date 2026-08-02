import { apiClient } from "./apiClient";

export interface SearchHit {
  documentId: string;
  versionId: string;
  title: string;
  filename: string;
  score: number;
  snippet?: string;
  highlights?: string[];
  pageReferences?: number[];
}

export interface SearchResult {
  query: string;
  searchType: string;
  totalHits: number;
  tookMs: number;
  hits: SearchHit[];
}

export const searchService = {
  async searchLexicalOrHybrid(query: string, searchType: string = "hybrid", limit: number = 10): Promise<SearchResult> {
    const response = await apiClient.get("/search", {
      params: { query, searchType, limit },
    });
    return response.data;
  },

  async searchSemantic(query: string, limit: number = 10): Promise<SearchResult> {
    const response = await apiClient.post("/semantic-search", {
      query,
      limit,
    });
    return response.data;
  },
};
