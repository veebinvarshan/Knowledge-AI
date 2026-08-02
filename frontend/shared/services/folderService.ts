import { apiClient } from "./apiClient";

export interface FolderItem {
  id: string;
  name: string;
  parentFolderId?: string;
  tenantId: string;
  workspaceId: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  children?: FolderItem[];
}

export const folderService = {
  async getFolderTree(parentId?: string): Promise<FolderItem[]> {
    const response = await apiClient.get("/folders/tree", {
      params: parentId ? { parentId } : {},
    });
    return response.data;
  },

  async createFolder(name: string, parentFolderId?: string, workspaceId: string = "00000000-0000-0000-0000-000000000000"): Promise<FolderItem> {
    const response = await apiClient.post("/folders", {
      name,
      parentFolderId,
      workspaceId,
    });
    return response.data;
  },

  async renameFolder(id: string, name: string): Promise<FolderItem> {
    const response = await apiClient.put(`/folders/${id}`, null, {
      params: { name },
    });
    return response.data;
  },

  async moveFolder(id: string, parentFolderId?: string): Promise<FolderItem> {
    const response = await apiClient.post(`/folders/${id}/move`, null, {
      params: parentFolderId ? { parentFolderId } : {},
    });
    return response.data;
  },

  async deleteFolder(id: string): Promise<void> {
    await apiClient.delete(`/folders/${id}`);
  },

  async restoreFolder(id: string): Promise<FolderItem> {
    const response = await apiClient.post(`/folders/${id}/restore`);
    return response.data;
  },
};
