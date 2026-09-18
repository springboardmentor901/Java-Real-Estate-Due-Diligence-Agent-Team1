"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getToken, removeToken, saveRole } from "@/lib/auth";
import { apiRequest } from "@/lib/api";
import Navbar from "@/components/Navbar";

interface UserProfile {
  id: number;
  fullName: string;
  email: string;
  role: string;
  createdAt: string;
}

interface PropertyBasic {
  id: number;
  address: string;
}

export default function AdminDashboardPage() {
  const router = useRouter();

  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [propertiesCount, setPropertiesCount] = useState<number | null>(null);
  const [testResult, setTestResult] = useState<string | null>(null);
  const [testingEndpoint, setTestingEndpoint] = useState(false);

  useEffect(() => {
    async function verifyAdminAccess() {
      const token = getToken();

      if (!token) {
        router.replace("/login");
        return;
      }

      try {
        // Authenticate and obtain authoritative profile and role
        const profile = await apiRequest<UserProfile>("/api/users/profile", {
          token,
        });

        // Strict RBAC check: only ADMINISTRATOR is permitted
        if (profile.role !== "ADMINISTRATOR") {
          saveRole(profile.role);
          router.replace("/dashboard");
          return;
        }

        setUser(profile);
        saveRole(profile.role);

        // Fetch platform metrics
        try {
          const props = await apiRequest<PropertyBasic[]>("/api/properties", {
            token,
          });
          if (Array.isArray(props)) {
            setPropertiesCount(props.length);
          }
        } catch {
          // Non-blocking metric load
        }
      } catch {
        removeToken();
        router.replace("/login");
      } finally {
        setLoading(false);
      }
    }

    verifyAdminAccess();
  }, [router]);

  async function handleTestAdminEndpoint() {
    const token = getToken();
    if (!token) return;

    try {
      setTestingEndpoint(true);
      setTestResult(null);

      // Verify backend @PreAuthorize("hasRole('ADMINISTRATOR')")
      const result = await apiRequest<string>("/api/test/admin-only", {
        token,
      });

      setTestResult(typeof result === "string" ? result : "Admin endpoint verified successfully.");
    } catch (err) {
      setTestResult(
        err instanceof Error
          ? `Authorization check failed: ${err.message}`
          : "Admin verification failed."
      );
    } finally {
      setTestingEndpoint(false);
    }
  }

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-purple-600 border-t-transparent" />
          <p className="mt-4 text-sm text-gray-600">Verifying administrator authorization...</p>
        </div>
      </main>
    );
  }

  if (!user || user.role !== "ADMINISTRATOR") {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8 flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-gray-200 pb-6">
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-extrabold text-gray-900">
                Administrator Command Center
              </h1>
              <span className="inline-flex rounded-full bg-purple-100 px-3 py-1 text-xs font-bold text-purple-800 uppercase tracking-wider">
                ADMINISTRATOR
              </span>
            </div>
            <p className="mt-2 text-sm text-gray-600">
              Platform administration, role-based access management, and system intelligence.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => router.push("/dashboard")}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors shadow-xs"
            >
              Switch to Standard View
            </button>
          </div>
        </div>

        {/* System Overview Cards */}
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4 mb-8">
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Admin User
            </p>
            <p className="mt-2 text-xl font-bold text-gray-900 line-clamp-1">
              {user.fullName}
            </p>
            <p className="mt-1 text-xs text-gray-500 line-clamp-1">
              {user.email}
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Active Role
            </p>
            <p className="mt-2 text-xl font-bold text-purple-700">
              ADMINISTRATOR
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Full System Privileges
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Registered Properties
            </p>
            <p className="mt-2 text-2xl font-bold text-gray-900">
              {propertiesCount != null ? propertiesCount : "..."}
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Active in platform database
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Security Status
            </p>
            <p className="mt-2 text-xl font-bold text-green-600">
              Verified
            </p>
            <p className="mt-1 text-xs text-gray-500">
              JWT Authenticated Session
            </p>
          </div>
        </div>

        {/* Admin Sections */}
        <div className="grid gap-8 lg:grid-cols-3">
          {/* Left Column: Management Actions */}
          <div className="lg:col-span-2 space-y-6">
            <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
              <h2 className="text-lg font-bold text-gray-900 mb-1">
                System Operations
              </h2>
              <p className="text-xs text-gray-500 mb-5">
                Manage platform data, inspect due diligence outputs, and configure intelligence.
              </p>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-lg border border-gray-200 p-4 hover:border-purple-300 transition-colors">
                  <h3 className="font-semibold text-gray-900 text-sm">
                    Properties Database
                  </h3>
                  <p className="mt-1 text-xs text-gray-500">
                    Search and inspect all registered properties across the system.
                  </p>
                  <button
                    onClick={() => router.push("/properties")}
                    className="mt-4 inline-flex items-center text-xs font-semibold text-purple-700 hover:text-purple-900"
                  >
                    Manage Properties →
                  </button>
                </div>

                <div className="rounded-lg border border-gray-200 p-4 hover:border-purple-300 transition-colors">
                  <h3 className="font-semibold text-gray-900 text-sm">
                    Reports Audit & Export
                  </h3>
                  <p className="mt-1 text-xs text-gray-500">
                    Audit generated due diligence reports, risk scores, and download PDF/Excel copies.
                  </p>
                  <button
                    onClick={() => router.push("/reports")}
                    className="mt-4 inline-flex items-center text-xs font-semibold text-purple-700 hover:text-purple-900"
                  >
                    Audit Reports →
                  </button>
                </div>
              </div>
            </div>

            {/* Backend RBAC Diagnostics */}
            <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
              <h2 className="text-lg font-bold text-gray-900 mb-1">
                Security & RBAC Diagnostic Console
              </h2>
              <p className="text-xs text-gray-500 mb-4">
                Verify that your active session satisfies backend method security constraints (<code>@PreAuthorize(&quot;hasRole(&apos;ADMINISTRATOR&apos;)&quot;)</code>).
              </p>

              <div className="rounded-lg bg-gray-50 p-4 border border-gray-200">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div>
                    <p className="text-xs font-mono text-gray-700 font-semibold">
                      GET /api/test/admin-only
                    </p>
                    <p className="text-xs text-gray-500 mt-0.5">
                      Restricted exclusively to Spring Security ADMINISTRATOR role.
                    </p>
                  </div>

                  <button
                    onClick={handleTestAdminEndpoint}
                    disabled={testingEndpoint}
                    className="rounded-lg bg-purple-600 px-4 py-2 text-xs font-semibold text-white hover:bg-purple-700 disabled:opacity-60 transition-colors shadow-xs"
                  >
                    {testingEndpoint ? "Testing..." : "Verify Administrator Role"}
                  </button>
                </div>

                {testResult && (
                  <div className="mt-3 rounded-md bg-white p-3 border border-purple-200 text-xs font-mono text-purple-900">
                    <span className="font-bold">Backend Response:</span> {testResult}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Right Column: Platform Role Matrix */}
          <div className="lg:col-span-1 space-y-6">
            <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
              <h2 className="text-lg font-bold text-gray-900 mb-1">
                Platform Roles (RBAC)
              </h2>
              <p className="text-xs text-gray-500 mb-4">
                Overview of the 5 platform roles and their access boundaries.
              </p>

              <div className="space-y-3">
                <div className="rounded-lg border border-purple-200 bg-purple-50/50 p-3">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-purple-900">ADMINISTRATOR</span>
                    <span className="rounded-full bg-purple-200 px-2 py-0.5 text-[10px] font-bold text-purple-800">Your Role</span>
                  </div>
                  <p className="mt-1 text-xs text-purple-800">
                    Full access to Admin Command Center, management operations, and all user capabilities.
                  </p>
                </div>

                <div className="rounded-lg border border-gray-200 p-3">
                  <span className="text-xs font-bold text-gray-900">BUYER</span>
                  <p className="mt-1 text-xs text-gray-600">
                    Property searches, due diligence checks, risk reports, and value trends.
                  </p>
                </div>

                <div className="rounded-lg border border-gray-200 p-3">
                  <span className="text-xs font-bold text-gray-900">REAL_ESTATE_AGENT</span>
                  <p className="mt-1 text-xs text-gray-600">
                    Market comparables, pricing trends, and client report generation.
                  </p>
                </div>

                <div className="rounded-lg border border-gray-200 p-3">
                  <span className="text-xs font-bold text-gray-900">LEGAL_REVIEWER</span>
                  <p className="mt-1 text-xs text-gray-600">
                    Zoning, deed, permit histories, ownership checks, and compliance audits.
                  </p>
                </div>

                <div className="rounded-lg border border-gray-200 p-3">
                  <span className="text-xs font-bold text-gray-900">FINANCIAL_INSTITUTION</span>
                  <p className="mt-1 text-xs text-gray-600">
                    Risk evaluation, property value assessments, and flood zone underwriting.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
