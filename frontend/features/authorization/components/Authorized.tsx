"use client";

import React from "react";
import { useAuthorization } from "../hooks/useAuthorization";

interface AuthorizedProps {
  children: React.ReactNode;
  permissions?: string[];
  roles?: string[];
  fallback?: React.ReactNode;
}

export default function Authorized({
  children,
  permissions = [],
  roles = [],
  fallback = null,
}: AuthorizedProps) {
  const { hasAnyPermission, roles: userRoles } = useAuthorization();

  // 1. Evaluate specific role clearances if provided
  if (roles.length > 0) {
    const hasRole = roles.some((r) => userRoles.includes(r));
    if (!hasRole) {
      return <>{fallback}</>;
    }
  }

  // 3. Evaluate specific permission clearances if provided
  if (permissions.length > 0) {
    const hasPerm = hasAnyPermission(permissions);
    if (!hasPerm) {
      return <>{fallback}</>;
    }
  }

  return <>{children}</>;
}
