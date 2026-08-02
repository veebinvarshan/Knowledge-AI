// Client-side permission mappings corresponding to standard database roles
const ROLE_PERMISSIONS: Record<string, string[]> = {
  ROLE_SUPER_ADMIN: [
    "documents:read", "documents:create", "documents:update", "documents:delete", "documents:share",
    "folders:read", "folders:create", "folders:update", "folders:delete", "folders:archive", "folders:restore",
    "knowledge:read", "knowledge:create", "knowledge:update", "knowledge:delete", "knowledge:manage",
    "chat:ask", "chat:history", "chat:manage",
    "users:read", "users:manage",
    "roles:read", "roles:manage",
    "analytics:view", "settings:manage", "system:manage"
  ],
  ROLE_ORG_ADMIN: [
    "documents:read", "documents:create", "documents:update", "documents:delete", "documents:share",
    "folders:read", "folders:create", "folders:update", "folders:delete", "folders:archive", "folders:restore",
    "knowledge:read", "knowledge:create", "knowledge:update", "knowledge:delete", "knowledge:manage",
    "chat:ask", "chat:history", "chat:manage",
    "users:read", "users:manage",
    "roles:read", "roles:manage",
    "analytics:view", "settings:manage"
  ],
  ROLE_MANAGER: [
    "documents:read", "documents:create", "documents:update", "documents:share",
    "folders:read", "folders:create", "folders:update", "folders:delete", "folders:archive", "folders:restore",
    "knowledge:read", "knowledge:manage",
    "chat:ask", "chat:history", "chat:manage",
    "users:read", "roles:read"
  ],
  ROLE_EDITOR: [
    "documents:read", "documents:create", "documents:update", "documents:share",
    "folders:read", "folders:create", "folders:update",
    "knowledge:read",
    "chat:ask", "chat:history"
  ],
  ROLE_CONTRIBUTOR: [
    "documents:read", "documents:create",
    "folders:read",
    "knowledge:read",
    "chat:ask", "chat:history"
  ],
  ROLE_VIEWER: [
    "documents:read", "folders:read", "knowledge:read", "chat:ask", "chat:history"
  ],
  ROLE_GUEST: [
    "documents:read", "chat:ask"
  ]
};

export function decodeJwtRoles(token: string): string[] {
  try {
    const base64Url = token.split(".")[1];
    if (!base64Url) return [];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    const claims = JSON.parse(jsonPayload);
    return Array.isArray(claims.roles) ? claims.roles : [];
  } catch (error) {
    return [];
  }
}

export function getPermissionsForRoles(roles: string[]): string[] {
  const permissions = new Set<String>();
  roles.forEach((role) => {
    const rolePerms = ROLE_PERMISSIONS[role.toUpperCase()];
    if (rolePerms) {
      rolePerms.forEach((p) => permissions.add(p));
    }
  });
  return Array.from(permissions) as string[];
}
