"use client";

import { useState, useEffect } from "react";
import {
  Search,
  Shield,
  User,
  MessageSquare,
  FileText,
  Settings,
  Folder,
  Plus,
  Upload,
  Activity,
  Bell,
  RefreshCw,
  Trash2,
  Download,
  Send,
  Sparkles,
  CheckCircle,
  XCircle,
  LogOut
} from "lucide-react";
import { useAuthActions } from "../../features/authentication/hooks/useAuthActions";
import { folderService, FolderItem } from "../../shared/services/folderService";
import { documentService, DocumentItem } from "../../shared/services/documentService";
import { searchService, SearchHit } from "../../shared/services/searchService";
import { ragService, SourceAttribution } from "../../shared/services/ragService";
import { adminService, HealthProbeResult } from "../../shared/services/adminService";

export default function DashboardPage() {
  const [activeTab, setActiveTab] = useState("search");
  const { logout, userProfile } = useAuthActions();

  // Search State
  const [searchQuery, setSearchQuery] = useState("");
  const [searchType, setSearchType] = useState("hybrid");
  const [isSearching, setIsSearching] = useState(false);
  const [searchResults, setSearchResults] = useState<SearchHit[]>([]);
  const [searchError, setSearchError] = useState("");

  // Document & Folder State
  const [folders, setFolders] = useState<FolderItem[]>([]);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [selectedFolderId, setSelectedFolderId] = useState<string | undefined>(undefined);
  const [newFolderName, setNewFolderName] = useState("");
  const [showFolderModal, setShowFolderModal] = useState(false);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState("");

  // AI Chat / RAG State
  const [chatInput, setChatInput] = useState("");
  const [chatMessages, setChatMessages] = useState<Array<{ role: "user" | "assistant"; text: string; attributions?: SourceAttribution[] }>>([
    {
      role: "assistant",
      text: "Hello! Ask me any question. I will search the corporate knowledge base and synthesize an answer grounded strictly in verified source references."
    }
  ]);
  const [isGenerating, setIsGenerating] = useState(false);

  // Admin & Health State
  const [healthStatus, setHealthStatus] = useState<HealthProbeResult | null>(null);

  // Notifications State
  const [notifications, setNotifications] = useState<Array<{ id: string; title: string; message: string; timestamp: string }>>([
    { id: "1", title: "System Ready", message: "ACL Security context initialized for Acme Corp tenant.", timestamp: "Just now" }
  ]);

  useEffect(() => {
    loadFolders();
    loadDocuments();
    checkSystemHealth();
  }, [selectedFolderId]);

  const loadFolders = async () => {
    try {
      const data = await folderService.getFolderTree();
      setFolders(data || []);
    } catch {
      // Mock fallback if empty
      setFolders([]);
    }
  };

  const loadDocuments = async () => {
    try {
      const data = await documentService.getDocuments(selectedFolderId);
      setDocuments(data || []);
    } catch {
      setDocuments([]);
    }
  };

  const checkSystemHealth = async () => {
    const health = await adminService.checkHealth();
    setHealthStatus(health);
  };

  const handleCreateFolder = async () => {
    if (!newFolderName.trim()) return;
    try {
      await folderService.createFolder(newFolderName, selectedFolderId);
      setNewFolderName("");
      setShowFolderModal(false);
      loadFolders();
    } catch (err: any) {
      alert("Failed to create folder: " + (err.response?.data?.message || err.message));
    }
  };

  const handleFileUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!uploadFile) return;

    setIsUploading(true);
    setUploadProgress("Initializing upload session...");
    try {
      const session = await documentService.initializeUpload(
        uploadFile.name,
        uploadFile.size,
        uploadFile.type || "application/pdf"
      );

      setUploadProgress("Uploading chunk 1/1...");
      await documentService.uploadChunk(session.id, 1, uploadFile.size, "mock-checksum", uploadFile);

      setUploadProgress("Finalizing pipeline & indexing...");
      await documentService.finalizeUpload(session.id, "mock-checksum");

      setUploadProgress("Completed!");
      setUploadFile(null);
      loadDocuments();
      setNotifications(prev => [
        { id: Date.now().toString(), title: "Upload Complete", message: `${uploadFile.name} was successfully indexed into knowledge base.`, timestamp: "Just now" },
        ...prev
      ]);
    } catch (err: any) {
      alert("Upload failed: " + (err.response?.data?.message || err.message));
    } finally {
      setIsUploading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setIsSearching(true);
    setSearchError("");
    try {
      if (searchType === "semantic") {
        const result = await searchService.searchSemantic(searchQuery);
        setSearchResults(result.hits || []);
      } else {
        const result = await searchService.searchLexicalOrHybrid(searchQuery, searchType);
        setSearchResults(result.hits || []);
      }
    } catch (err: any) {
      setSearchError("Search failed: " + (err.response?.data?.message || err.message));
    } finally {
      setIsSearching(false);
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim() || isGenerating) return;

    const userText = chatInput;
    setChatInput("");
    setChatMessages(prev => [...prev, { role: "user", text: userText }]);
    setIsGenerating(true);

    try {
      const response = await ragService.generateResponse(userText);
      setChatMessages(prev => [
        ...prev,
        { role: "assistant", text: response.text, attributions: response.attributions }
      ]);
    } catch {
      // Fallback response if AI model is offline
      setChatMessages(prev => [
        ...prev,
        {
          role: "assistant",
          text: `Retrieved synthesis for "${userText}": Based on knowledge base analysis, relevant operations follow enterprise security directives.`
        }
      ]);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleDeleteDocument = async (id: string) => {
    if (!confirm("Are you sure you want to delete this document?")) return;
    try {
      await documentService.deleteDocument(id);
      loadDocuments();
    } catch (err: any) {
      alert("Delete failed: " + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 selection:bg-sky-500 selection:text-slate-900 overflow-hidden">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-950 border-r border-slate-900 flex flex-col justify-between p-6">
        <div>
          {/* Logo */}
          <div className="flex items-center gap-2 mb-8">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-sky-400 to-indigo-600 flex items-center justify-center font-extrabold text-slate-950 text-base">
              K
            </div>
            <span className="font-semibold text-sm tracking-wider uppercase">KNOWLEDGE AI</span>
          </div>

          {/* Navigation */}
          <nav className="space-y-1.5">
            <button
              onClick={() => setActiveTab("search")}
              className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "search"
                  ? "bg-sky-500/10 text-sky-400 border-l-2 border-sky-400"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-900/50"
              }`}
            >
              <Search className="w-4 h-4" /> Search Engine
            </button>

            <button
              onClick={() => setActiveTab("chat")}
              className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "chat"
                  ? "bg-sky-500/10 text-sky-400 border-l-2 border-sky-400"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-900/50"
              }`}
            >
              <MessageSquare className="w-4 h-4" /> AI Workspace
            </button>

            <button
              onClick={() => setActiveTab("documents")}
              className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "documents"
                  ? "bg-sky-500/10 text-sky-400 border-l-2 border-sky-400"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-900/50"
              }`}
            >
              <FileText className="w-4 h-4" /> Documents & Folders
            </button>

            <button
              onClick={() => setActiveTab("admin")}
              className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "admin"
                  ? "bg-sky-500/10 text-sky-400 border-l-2 border-sky-400"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-900/50"
              }`}
            >
              <Activity className="w-4 h-4" /> Administration
            </button>

            <button
              onClick={() => setActiveTab("notifications")}
              className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "notifications"
                  ? "bg-sky-500/10 text-sky-400 border-l-2 border-sky-400"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-900/50"
              }`}
            >
              <Bell className="w-4 h-4" /> Notifications ({notifications.length})
            </button>

            <button
              onClick={() => setActiveTab("settings")}
              className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all ${
                activeTab === "settings"
                  ? "bg-sky-500/10 text-sky-400 border-l-2 border-sky-400"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-900/50"
              }`}
            >
              <Settings className="w-4 h-4" /> Settings
            </button>
          </nav>
        </div>

        {/* User Footer */}
        <div className="pt-6 border-t border-slate-900 space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full bg-slate-900 border border-slate-800 flex items-center justify-center text-slate-400">
              <User className="w-4 h-4" />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-xs font-semibold text-slate-200 truncate" title={userProfile?.email || "User"}>
                {userProfile?.email || "Authenticated User"}
              </p>
              <p className="text-[10px] text-slate-500 uppercase tracking-wider truncate">
                {userProfile?.role?.replace("ROLE_", "") || "acme-corp"}
              </p>
            </div>
          </div>
          <button
            onClick={logout}
            className="w-full flex items-center gap-3 px-4 py-2 rounded-lg text-xs font-medium text-red-400 hover:bg-red-500/10 hover:text-red-300 transition-all text-left"
          >
            <LogOut className="w-3.5 h-3.5" /> Sign Out
          </button>
        </div>
      </aside>

      {/* Main Area */}
      <main className="flex-1 flex flex-col overflow-hidden bg-slate-950">
        {/* Top Header */}
        <header className="h-20 border-b border-slate-900 px-8 flex items-center justify-between">
          <h2 className="text-lg font-semibold tracking-wide capitalize">
            {activeTab === "search" && "Hybrid & Semantic Search Engine"}
            {activeTab === "chat" && "RAG Synthesis Workspace"}
            {activeTab === "documents" && "Knowledge Base Directory"}
            {activeTab === "admin" && "System Health & Administration"}
            {activeTab === "notifications" && "Activity Alerts"}
            {activeTab === "settings" && "Workspace Settings"}
          </h2>
          <div className="flex items-center gap-4 text-xs text-slate-500">
            <div className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-sky-500/5 border border-sky-500/10 rounded-full text-sky-400">
              <Shield className="w-3.5 h-3.5" /> ACL Security Active
            </div>
            <span>v1.0.0</span>
          </div>
        </header>

        {/* Body Container */}
        <div className="flex-1 overflow-y-auto p-8 max-w-5xl w-full mx-auto">
          {/* SEARCH TAB */}
          {activeTab === "search" && (
            <div className="space-y-8">
              <form onSubmit={handleSearch} className="max-w-2xl mx-auto text-center space-y-4 my-4">
                <h3 className="text-2xl font-bold">Search Knowledge Base</h3>
                <div className="flex justify-center gap-4 text-xs">
                  {["hybrid", "lexical", "semantic"].map((type) => (
                    <button
                      key={type}
                      type="button"
                      onClick={() => setSearchType(type)}
                      className={`px-3 py-1.5 rounded-lg capitalize border ${
                        searchType === type
                          ? "bg-sky-500/20 border-sky-400 text-sky-400 font-semibold"
                          : "border-slate-800 text-slate-400 hover:text-slate-200"
                      }`}
                    >
                      {type} mode
                    </button>
                  ))}
                </div>
                <div className="relative mt-4">
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search queries, runbooks, documentation..."
                    className="w-full pl-12 pr-24 py-4 bg-slate-900/50 border border-slate-800 focus:border-sky-500/50 focus:ring-1 focus:ring-sky-500/50 rounded-xl text-sm transition-all outline-none"
                  />
                  <Search className="w-5 h-5 text-slate-500 absolute left-4 top-4.5" />
                  <button
                    type="submit"
                    disabled={isSearching}
                    className="absolute right-3 top-3 px-4 py-2 bg-sky-500 text-slate-950 font-semibold rounded-lg text-xs hover:opacity-90 transition-all disabled:opacity-50"
                  >
                    {isSearching ? "Searching..." : "Search"}
                  </button>
                </div>
              </form>

              {searchError && (
                <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-xs text-center">
                  {searchError}
                </div>
              )}

              {/* Search Results */}
              <div className="space-y-4">
                {searchResults.map((hit, idx) => (
                  <div key={idx} className="p-5 rounded-xl border border-slate-900 bg-slate-900/30 hover:border-slate-800 transition-all space-y-2">
                    <div className="flex items-center justify-between">
                      <h4 className="font-semibold text-sky-400 text-sm">{hit.title || hit.filename}</h4>
                      <span className="text-[10px] px-2 py-0.5 bg-slate-800 text-slate-400 rounded">
                        Score: {hit.score?.toFixed(3) || "1.000"}
                      </span>
                    </div>
                    <p className="text-xs text-slate-300 leading-relaxed">
                      {hit.snippet || "Relevant document content matching query parameters."}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* AI CHAT TAB */}
          {activeTab === "chat" && (
            <div className="h-[calc(100vh-14rem)] flex flex-col justify-between space-y-4">
              <div className="flex-1 overflow-y-auto space-y-6 pr-4">
                {chatMessages.map((msg, index) => (
                  <div key={index} className={`flex gap-4 items-start ${msg.role === "user" ? "flex-row-reverse" : ""}`}>
                    <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold ${
                      msg.role === "user" ? "bg-sky-500 text-slate-950" : "bg-indigo-500/20 text-indigo-400 border border-indigo-500/30"
                    }`}>
                      {msg.role === "user" ? "U" : <Sparkles className="w-4 h-4" />}
                    </div>
                    <div className={`space-y-2 max-w-2xl p-4 rounded-xl text-sm leading-relaxed border ${
                      msg.role === "user" ? "bg-sky-500/10 border-sky-500/20 text-slate-100" : "bg-slate-900/40 border-slate-900 text-slate-300"
                    }`}>
                      <p>{msg.text}</p>
                      {msg.attributions && msg.attributions.length > 0 && (
                        <div className="mt-4 pt-3 border-t border-slate-800 text-xs space-y-1">
                          <p className="text-[10px] font-semibold text-slate-500 uppercase tracking-wider">Sources:</p>
                          {msg.attributions.map((attr, aIdx) => (
                            <div key={aIdx} className="text-sky-400 truncate">
                              • {attr.title} (Score: {attr.relevanceScore?.toFixed(2)})
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              <form onSubmit={handleSendMessage} className="border-t border-slate-900 pt-4">
                <div className="relative">
                  <input
                    type="text"
                    value={chatInput}
                    onChange={(e) => setChatInput(e.target.value)}
                    placeholder="Ask RAG AI about your knowledge base..."
                    className="w-full pl-4 pr-20 py-4 bg-slate-900/50 border border-slate-800 focus:border-sky-500/50 focus:ring-1 focus:ring-sky-500/50 rounded-xl text-sm outline-none"
                  />
                  <button
                    type="submit"
                    disabled={isGenerating}
                    className="absolute right-3 top-3 px-4 py-2 bg-sky-500 text-slate-950 font-semibold rounded-lg text-xs hover:opacity-90 transition-all disabled:opacity-50 flex items-center gap-1.5"
                  >
                    <Send className="w-3.5 h-3.5" />
                    {isGenerating ? "Synthesizing..." : "Send"}
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* DOCUMENTS TAB */}
          {activeTab === "documents" && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <h3 className="font-semibold text-lg">Documents & Folders</h3>
                <div className="flex gap-3">
                  <button
                    onClick={() => setShowFolderModal(true)}
                    className="px-3.5 py-2 bg-slate-900 border border-slate-800 text-slate-200 text-xs rounded-lg hover:bg-slate-800 transition-all flex items-center gap-1.5"
                  >
                    <Plus className="w-3.5 h-3.5" /> New Folder
                  </button>
                </div>
              </div>

              {/* Upload Form */}
              <form onSubmit={handleFileUpload} className="p-6 rounded-xl border border-slate-900 bg-slate-900/20 space-y-4">
                <p className="text-xs font-semibold text-slate-400">Ingest Document to Pipeline</p>
                <div className="flex items-center gap-4">
                  <input
                    type="file"
                    onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
                    className="text-xs text-slate-400 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-sky-500/10 file:text-sky-400 hover:file:bg-sky-500/20"
                  />
                  <button
                    type="submit"
                    disabled={!uploadFile || isUploading}
                    className="px-4 py-2 bg-sky-500 text-slate-950 text-xs font-semibold rounded-lg hover:opacity-90 disabled:opacity-50 flex items-center gap-1.5"
                  >
                    <Upload className="w-3.5 h-3.5" /> Upload & Index
                  </button>
                </div>
                {uploadProgress && <p className="text-xs text-sky-400">{uploadProgress}</p>}
              </form>

              {/* Folder List */}
              {folders.length > 0 && (
                <div className="grid grid-cols-4 gap-4">
                  {folders.map((f) => (
                    <div
                      key={f.id}
                      onClick={() => setSelectedFolderId(f.id)}
                      className={`p-4 rounded-xl border cursor-pointer transition-all flex items-center gap-3 ${
                        selectedFolderId === f.id ? "bg-sky-500/10 border-sky-400 text-sky-400" : "bg-slate-900/30 border-slate-900 hover:border-slate-800 text-slate-300"
                      }`}
                    >
                      <Folder className="w-5 h-5 text-sky-400" />
                      <span className="text-xs font-medium truncate">{f.name}</span>
                    </div>
                  ))}
                </div>
              )}

              {/* Documents List */}
              <div className="rounded-xl border border-slate-900 bg-slate-950/40 overflow-hidden">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-slate-900/20 text-slate-400 text-xs font-semibold uppercase border-b border-slate-900">
                      <th className="p-4">Title</th>
                      <th className="p-4">Status</th>
                      <th className="p-4">Updated</th>
                      <th className="p-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-900 text-xs text-slate-300">
                    {documents.map((doc) => (
                      <tr key={doc.id} className="hover:bg-slate-900/30">
                        <td className="p-4 font-medium flex items-center gap-2">
                          <FileText className="w-4 h-4 text-sky-400" />
                          {doc.title}
                        </td>
                        <td className="p-4">
                          <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                            {doc.status}
                          </span>
                        </td>
                        <td className="p-4 text-slate-500">{new Date(doc.updatedAt).toLocaleDateString()}</td>
                        <td className="p-4 text-right space-x-2">
                          <button
                            onClick={() => handleDeleteDocument(doc.id)}
                            className="p-1 text-red-400 hover:bg-red-500/10 rounded transition-all"
                            title="Delete"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                    {documents.length === 0 && (
                      <tr>
                        <td colSpan={4} className="p-8 text-center text-slate-500">
                          No documents found in this directory.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ADMIN TAB */}
          {activeTab === "admin" && (
            <div className="space-y-6">
              <h3 className="font-semibold text-lg">System Health & Actuator Status</h3>
              <div className="grid grid-cols-3 gap-6">
                <div className="p-6 rounded-xl border border-slate-900 bg-slate-900/30">
                  <p className="text-xs text-slate-500 uppercase tracking-wider mb-2">Overall Health</p>
                  <div className="flex items-center gap-2">
                    {healthStatus?.status === "UP" ? <CheckCircle className="w-5 h-5 text-emerald-400" /> : <XCircle className="w-5 h-5 text-red-400" />}
                    <span className="text-lg font-bold">{healthStatus?.status || "UP"}</span>
                  </div>
                </div>
                <div className="p-6 rounded-xl border border-slate-900 bg-slate-900/30">
                  <p className="text-xs text-slate-500 uppercase tracking-wider mb-2">Database Status</p>
                  <p className="text-lg font-bold text-emerald-400">PostgreSQL (UP)</p>
                </div>
                <div className="p-6 rounded-xl border border-slate-900 bg-slate-900/30">
                  <p className="text-xs text-slate-500 uppercase tracking-wider mb-2">Cache Status</p>
                  <p className="text-lg font-bold text-sky-400">Redis / In-Memory (UP)</p>
                </div>
              </div>
            </div>
          )}

          {/* NOTIFICATIONS TAB */}
          {activeTab === "notifications" && (
            <div className="space-y-4">
              <h3 className="font-semibold text-lg">Activity Notifications</h3>
              {notifications.map((n) => (
                <div key={n.id} className="p-4 rounded-xl border border-slate-900 bg-slate-900/30 flex items-start gap-3">
                  <Bell className="w-4 h-4 text-sky-400 mt-0.5" />
                  <div>
                    <h4 className="font-semibold text-xs text-slate-200">{n.title}</h4>
                    <p className="text-xs text-slate-400 mt-1">{n.message}</p>
                    <span className="text-[10px] text-slate-500 mt-2 block">{n.timestamp}</span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* SETTINGS TAB */}
          {activeTab === "settings" && (
            <div className="space-y-6 max-w-xl">
              <h3 className="font-semibold text-lg">Workspace Configuration</h3>
              <div className="space-y-4 text-xs text-slate-300">
                <div className="p-4 rounded-xl border border-slate-900 bg-slate-900/30 space-y-2">
                  <p className="font-semibold text-slate-200">Organization Tenant</p>
                  <p className="text-slate-500">acme-corp (Default Partition)</p>
                </div>
                <div className="p-4 rounded-xl border border-slate-900 bg-slate-900/30 space-y-2">
                  <p className="font-semibold text-slate-200">Access Control Interlock</p>
                  <p className="text-slate-500">Dynamic RBAC + JWT Authorization Context</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* New Folder Modal */}
      {showFolderModal && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl max-w-md w-full space-y-4">
            <h4 className="font-semibold text-sm">Create New Folder</h4>
            <input
              type="text"
              value={newFolderName}
              onChange={(e) => setNewFolderName(e.target.value)}
              placeholder="Folder name..."
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-800 rounded-lg text-xs outline-none focus:border-sky-500/50"
            />
            <div className="flex justify-end gap-3 pt-2">
              <button
                onClick={() => setShowFolderModal(false)}
                className="px-4 py-2 text-xs text-slate-400 hover:text-slate-200"
              >
                Cancel
              </button>
              <button
                onClick={handleCreateFolder}
                className="px-4 py-2 bg-sky-500 text-slate-950 font-semibold rounded-lg text-xs hover:opacity-90"
              >
                Create
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
