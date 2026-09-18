"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { getToken, getRole } from "@/lib/auth";

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    const token = getToken();
    if (token) {
      const role = getRole();
      if (role === "ADMINISTRATOR") {
        router.replace("/admin/dashboard");
      } else {
        router.replace("/dashboard");
      }
    } else {
      router.replace("/login");
    }
  }, [router]);

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <p className="mt-4 text-sm text-gray-600">Loading Due Diligence Agent...</p>
      </div>
    </main>
  );
}
