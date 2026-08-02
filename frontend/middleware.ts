import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// Path classifications
const GUEST_PATHS = ["/login", "/register", "/forgot-password", "/reset-password"];
const PROTECTED_PREFIXES = ["/dashboard", "/search", "/chat", "/admin", "/settings"];

// Simple server-side JWT claims decoder
function decodeRolesFromToken(token: string): string[] {
  try {
    const base64Url = token.split(".")[1];
    if (!base64Url) return [];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = Buffer.from(base64, "base64").toString("utf8");
    const claims = JSON.parse(jsonPayload);
    return Array.isArray(claims.roles) ? claims.roles : [];
  } catch (error) {
    return [];
  }
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const refreshToken = request.cookies.get("refresh_token")?.value;

  const isGuestPath = GUEST_PATHS.some((path) => pathname === path);
  const isProtectedPath = PROTECTED_PREFIXES.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`)
  );

  // 1. Redirect loop prevention & token checking for protected pages
  if (isProtectedPath && !refreshToken) {
    const loginUrl = new URL("/login", request.url);
    // Remember original destination
    loginUrl.searchParams.set("callbackUrl", pathname + request.nextUrl.search);
    return NextResponse.redirect(loginUrl);
  }

  // 2. Redirect authenticated users away from guest-only paths
  if (isGuestPath && refreshToken) {
    const dashboardUrl = new URL("/dashboard", request.url);
    return NextResponse.redirect(dashboardUrl);
  }

  // 3. Dynamic permission checks for admin zones
  if (pathname.startsWith("/admin") && refreshToken) {
    const roles = decodeRolesFromToken(refreshToken);
    const { getPermissionsForRoles } = require("./features/authorization/utils/permissionUtils");
    const permissions = getPermissionsForRoles(roles);
    const hasAdminAccess = permissions.includes("users:manage") || permissions.includes("roles:manage");
    if (!hasAdminAccess) {
      const unauthorizedUrl = new URL("/unauthorized", request.url);
      return NextResponse.redirect(unauthorizedUrl);
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (Next.js BFF API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - images, icons, or standard design assets
     */
    "/((?!api|_next/static|_next/image|favicon.ico|.*\\..*).*)",
  ],
};
