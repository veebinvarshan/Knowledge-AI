import { useAtomValue } from "jotai";
import { accessTokenAtom } from "../../authentication/store/authAtom";
import { decodeJwtRoles, getPermissionsForRoles } from "../utils/permissionUtils";
import { useMemo } from "react";

export function useAuthorization() {
  const token = useAtomValue(accessTokenAtom);

  const { roles, permissions } = useMemo(() => {
    if (!token) {
      return { roles: [], permissions: [] };
    }
    const roles = decodeJwtRoles(token);
    const permissions = getPermissionsForRoles(roles);
    return { roles, permissions };
  }, [token]);

  const hasPermission = (permission: string): boolean => {
    return permissions.includes(permission);
  };

  const hasAnyPermission = (requiredPermissions: string[]): boolean => {
    return requiredPermissions.some((p) => permissions.includes(p));
  };

  const hasAllPermissions = (requiredPermissions: string[]): boolean => {
    return requiredPermissions.every((p) => permissions.includes(p));
  };

  return {
    roles,
    permissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
  };
}
