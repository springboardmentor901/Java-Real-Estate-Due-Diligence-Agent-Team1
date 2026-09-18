import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Due Diligence Agent - Real Estate Intelligence Platform",
  description: "AI-Powered Due Diligence for Real Estate Investments",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full flex flex-col font-sans bg-gray-50 text-gray-900">{children}</body>
    </html>
  );
}
