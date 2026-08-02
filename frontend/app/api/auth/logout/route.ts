import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_URL = process.env.BACKEND_API_URL || "http://localhost:8080/api/v1";

export async function POST(request: Request) {
  try {
    const cookieStore = await cookies();
    const refreshTokenCookie = cookieStore.get("refresh_token");
    const refreshToken = refreshTokenCookie?.value;

    if (refreshToken) {
      // Notify Spring Boot backend to revoke the refresh token
      await fetch(`${BACKEND_URL}/auth/logout`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${refreshToken}`,
        },
      }).catch((err) => {
        // Log backend logout failure but proceed with local cookie clearance
        console.error("Failed to revoke token on backend:", err);
      });
    }

    // Invalidate local cookie
    cookieStore.delete("refresh_token");

    return NextResponse.json({ success: true });
  } catch (error: any) {
    console.error("BFF logout proxy error:", error);
    return NextResponse.json(
      { error: "Internal security gateway error during logout" },
      { status: 500 }
    );
  }
}
