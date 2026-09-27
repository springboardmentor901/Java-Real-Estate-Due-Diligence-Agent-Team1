"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getToken, removeToken, saveRole } from "@/lib/auth";
import { apiRequest } from "@/lib/api";

import Navbar from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";

import BuyerDashboard from "@/components/dashboard/BuyerDashboard";
import AgentDashboard from "@/components/dashboard/AgentDashboard";
import LegalDashboard from "@/components/dashboard/LegalDashboard";
import FinancialDashboard from "@/components/dashboard/FinancialDashboard";

interface UserProfile {
  id: number;
  fullName: string;
  email: string;
  role: string;
  createdAt: string;
}

export default function DashboardPage() {
  const router = useRouter();

  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadProfile() {
      const token = getToken();

      if (!token) {
        router.replace("/login");
        return;
      }

      try {
        const profile = await apiRequest<UserProfile>(
          "/api/users/profile",
          {
            token,
          }
        );

        setUser(profile);
        if (profile?.role) {
          saveRole(profile.role);
          // If ADMINISTRATOR lands on /dashboard, redirect to /admin/dashboard
          if (profile.role === "ADMINISTRATOR") {
            router.replace("/admin/dashboard");
            return;
          }
        }
      } catch {
        removeToken();
        router.replace("/login");
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
  }, [router]);

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
          <p className="mt-4 text-sm text-gray-600">Loading your workspace...</p>
        </div>
      </main>
    );
  }

  if (!user) {
    return null;
  }

  // Render role-specific dashboard experience
  function renderRoleDashboard() {
    if (!user) return null;

    switch (user.role) {
      case "BUYER":
        return <BuyerDashboard user={user} />;
      case "REAL_ESTATE_AGENT":
        return <AgentDashboard user={user} />;
      case "LEGAL_REVIEWER":
        return <LegalDashboard user={user} />;
      case "FINANCIAL_INSTITUTION":
        return <FinancialDashboard user={user} />;
      case "ADMINISTRATOR":
        return (
          <div className="rounded-xl border border-purple-200 bg-purple-50 p-6 text-center">
            <h2 className="text-lg font-bold text-purple-900">Administrator Command Center</h2>
            <p className="text-xs text-purple-700 mt-1 mb-4">
              You are signed in with administrator privileges.
            </p>
            <button
              onClick={() => router.push("/admin/dashboard")}
              className="rounded-lg bg-purple-600 px-4 py-2 text-xs font-semibold text-white hover:bg-purple-700 transition"
            >
              Go to Admin Dashboard →
            </button>
          </div>
        );
      default:
        return <BuyerDashboard user={user} />;
    }
  }

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50 pb-12">
        <Navbar />
        <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pt-6">
          {renderRoleDashboard()}
        </main>
      </div>
    </AuthGuard>
  );
}