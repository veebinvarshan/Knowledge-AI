import { apiClient } from "./apiClient";

export interface DocumentItem {
  id: string;
  title: string;
  folderId?: string;
  tenantId: string;
  workspaceId: string;
  ownerId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  metadata?: Record<string, string>;
  tags?: string[];
}

export interface VersionItem {
  versionNumber: number;
  filename: string;
  fileSizeBytes: number;
  mimeType: string;
  checksum: string;
  createdAt: string;
}

export interface UploadSession {
  id: string;
  tenantId: string;
  userId: string;
  fileName: string;
  fileSizeBytes: number;
  mimeType: string;
  status: string;
  chunksTotal: number;
}

export const documentService = {
  async getDocuments(folderId?: string): Promise<DocumentItem[]> {
    const response = await apiClient.get("/documents", {
      params: folderId ? { folderId } : {},
    });
    return response.data;
  },

  async getDocument(id: string): Promise<DocumentItem> {
    const response = await apiClient.get(`/documents/${id}`);
    return response.data;
  },

  async createDocument(title: string, folderId?: string, workspaceId: string = "00000000-0000-0000-0000-000000000000"): Promise<DocumentItem> {
    const response = await apiClient.post("/documents", {
      title,
      folderId,
      workspaceId,
    });
    return response.data;
  },

  async renameDocument(id: string, title: string): Promise<DocumentItem> {
    const response = await apiClient.put(`/documents/${id}`, null, {
      params: { title },
    });
    return response.data;
  },

  async deleteDocument(id: string): Promise<void> {
    await apiClient.delete(`/documents/${id}`);
  },

  async restoreDocument(id: string): Promise<DocumentItem> {
    const response = await apiClient.post(`/documents/${id}/restore`);
    return response.data;
  },

  async getVersionHistory(id: string): Promise<{ versions: VersionItem[] }> {
    const response = await apiClient.get(`/documents/${id}/versions`);
    return response.data;
  },

  // Upload Pipeline
  async initializeUpload(fileName: string, fileSizeBytes: number, mimeType: string, chunksTotal: number = 1): Promise<UploadSession> {
    const response = await apiClient.post("/uploads", {
      fileName,
      fileSizeBytes,
      mimeType,
      chunksTotal,
    });
    return response.data;
  },

  async uploadChunk(sessionId: string, chunkNumber: number, sizeBytes: number, checksum: string, fileBlob: Blob): Promise<void> {
    const formData = new FormData();
    formData.append("chunkNumber", chunkNumber.toString());
    formData.append("sizeBytes", sizeBytes.toString());
    formData.append("checksum", checksum);
    formData.append("file", fileBlob);

    await apiClient.post(`/uploads/${sessionId}/chunks`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  async finalizeUpload(sessionId: string, checksum: string): Promise<UploadSession> {
    const response = await apiClient.post(`/uploads/${sessionId}/finalize`, null, {
      params: { checksum },
    });
    return response.data;
  },
};
