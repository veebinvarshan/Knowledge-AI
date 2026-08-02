import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_URL = process.env.BACKEND_API_URL || "http://localhost:8080/api/v1";

export async function POST(request: Request) {
  try {
    const cookieStore = await cookies();
    const refreshTokenCookie = cookieStore.get("refresh_token");
    const refreshToken = refreshTokenCookie?.value;

    if (!refreshToken) {
      return NextResponse.json({ error: "No refresh token available" }, { status: 401 });
    }

    const userAgent = request.headers.get("user-agent") || "NextJS-BFF";

    const response = await fetch(`${BACKEND_URL}/auth/refresh`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${refreshToken}`,
        "User-Agent": userAgent,
        "X-Device-Fingerprint": "next-bff-fingerprint",
      },
    });

    if (!response.ok) {
      // Invalidate the cookie if backend rejects it
      cookieStore.delete("refresh_token");
      return NextResponse.json(
        { error: "Session expired or invalid token" },
        { status: 401 }
      );
    }

    const data = await response.json(); // contains accessToken, refreshToken, expiresIn
    const { accessToken, refreshToken: newRefreshToken, expiresIn } = data;

    // Rotate refresh token
    cookieStore.set({
      name: "refresh_token",
      value: newRefreshToken,
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      path: "/",
      maxAge: 60 * 60 * 24 * 7, // 7 days
    });

    return NextResponse.json({ accessToken, expiresIn });
  } catch (error: any) {
    console.error("BFF refresh proxy error:", error);
    return NextResponse.json(
      { error: "Internal security gateway error during refresh" },
      { status: 500 }
    );
  }
}
