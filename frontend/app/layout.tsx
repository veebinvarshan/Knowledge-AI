import type { Metadata } from "next";
import "../styles/globals.css";
import AuthProvider from "../shared/providers/AuthProvider";

export const metadata: Metadata = {
  title: "Enterprise AI Knowledge Management Platform",
  description: "Secure semantic search and RAG orchestration for enterprise information",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet" />
      </head>
      <body className="antialiased min-h-screen">
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
