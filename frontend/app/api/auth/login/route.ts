import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_URL = process.env.BACKEND_API_URL || "http://localhost:8080/api/v1";

export async function POST(request: Request) {
  try {
    const { email, password, tenant } = await request.json();

    if (!email || !password || !tenant) {
      return NextResponse.json(
        { error: "Email, password, and organization domain are required" },
        { status: 400 }
      );
    }

    const ipAddress = request.headers.get("x-forwarded-for") || "127.0.0.1";
    const userAgent = request.headers.get("user-agent") || "NextJS-BFF";

    const response = await fetch(`${BACKEND_URL}/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Forwarded-For": ipAddress,
        "User-Agent": userAgent,
        "X-Device-Fingerprint": "next-bff-fingerprint",
      },
      body: JSON.stringify({ email, password, tenant }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      return NextResponse.json(
        { error: errorData.error || "Authentication failed" },
        { status: response.status }
      );
    }

    const data = await response.json(); // contains accessToken, refreshToken, expiresIn
    const { accessToken, refreshToken, expiresIn } = data;

    // Save refresh token to HttpOnly secure cookie
    const cookieStore = await cookies();
    cookieStore.set({
      name: "refresh_token",
      value: refreshToken,
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "strict",
      path: "/",
      maxAge: 60 * 60 * 24 * 7, // 7 days
    });

    return NextResponse.json({ accessToken, expiresIn });
  } catch (error: any) {
    console.error("BFF login proxy error:", error);
    return NextResponse.json(
      { error: "Internal security gateway error" },
      { status: 500 }
    );
  }
}
