"use client";

import { useEffect, useState, useCallback } from "react";
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

interface AuditLog {
  id: number;
  userId?: number | null;
  userEmail: string;
  action: string;
  details?: string | null;
  timestamp: string;
}

interface AdminDashboardData {
  totalUsers: number;
  totalProperties: number;
  totalReports: number;
  reportsLast7Days: number;
  recentAuditLogs: AuditLog[];
  userRoles: Record<string, number>;
}

interface AuditLogPageResponse {
  content: AuditLog[];
  currentPage: number;
  totalElements: number;
  totalPages: number;
  pageSize: number;
}

export default function AdminDashboardPage() {
  const router = useRouter();

  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  // Admin Dashboard Metrics
  const [metrics, setMetrics] = useState<AdminDashboardData | null>(null);

  // Audit Log Table State
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [auditTotal, setAuditTotal] = useState(0);
  const [auditPages, setAuditPages] = useState(1);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [filterUser, setFilterUser] = useState("");
  const [filterAction, setFilterAction] = useState("ALL");
  const [filterStartDate, setFilterStartDate] = useState("");
  const [filterEndDate, setFilterEndDate] = useState("");
  const [auditLoading, setAuditLoading] = useState(false);

  // Security test console
  const [testResult, setTestResult] = useState<string | null>(null);
  const [testingEndpoint, setTestingEndpoint] = useState(false);

  // 1. Initial Admin Verification & Dashboard Metrics Load
  useEffect(() => {
    async function verifyAndLoadAdmin() {
      const token = getToken();

      if (!token) {
        router.replace("/login");
        return;
      }

      try {
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

        // Fetch dedicated Admin Dashboard metrics (/api/admin/dashboard)
        try {
          const dashData = await apiRequest<AdminDashboardData>("/api/admin/dashboard", {
            token,
          });
          setMetrics(dashData);
        } catch {
          // Fallback if network glitch
        }
      } catch {
        removeToken();
        router.replace("/login");
      } finally {
        setLoading(false);
      }
    }

    verifyAndLoadAdmin();
  }, [router]);

  // 2. Fetch Audit Logs with Filtering & Pagination
  const fetchAuditLogs = useCallback(
    async (
      currentPage: number,
      size: number,
      uFilter: string,
      aFilter: string,
      sDate: string,
      eDate: string
    ) => {
      const token = getToken();
      if (!token) return;

      try {
        setAuditLoading(true);
        const params = new URLSearchParams();
        params.append("page", String(currentPage));
        params.append("size", String(size));

        if (uFilter.trim()) params.append("user", uFilter.trim());
        if (aFilter && aFilter !== "ALL") params.append("action", aFilter);
        if (sDate) params.append("startDate", sDate);
        if (eDate) params.append("endDate", eDate);

        const res = await apiRequest<AuditLogPageResponse>(
          `/api/admin/audit-logs?${params.toString()}`,
          { token }
        );

        if (res && Array.isArray(res.content)) {
          setAuditLogs(res.content);
          setAuditTotal(res.totalElements);
          setAuditPages(res.totalPages || 1);
        }
      } catch {
        // Non-blocking
      } finally {
        setAuditLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    if (user && user.role === "ADMINISTRATOR") {
      fetchAuditLogs(page, pageSize, filterUser, filterAction, filterStartDate, filterEndDate);
    }
  }, [user, page, pageSize, filterAction, fetchAuditLogs, filterUser, filterStartDate, filterEndDate]);

  function handleFilterSubmit(e: React.FormEvent) {
    e.preventDefault();
    setPage(0);
    fetchAuditLogs(0, pageSize, filterUser, filterAction, filterStartDate, filterEndDate);
  }

  function handleResetFilters() {
    setFilterUser("");
    setFilterAction("ALL");
    setFilterStartDate("");
    setFilterEndDate("");
    setPage(0);
    fetchAuditLogs(0, pageSize, "", "ALL", "", "");
  }

  async function handleTestAdminEndpoint() {
    const token = getToken();
    if (!token) return;

    try {
      setTestingEndpoint(true);
      setTestResult(null);

      const result = await apiRequest<string>("/api/test/admin-only", {
        token,
      });

      setTestResult(typeof result === "string" ? result : "Admin endpoint verified successfully (200 OK).");
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
    <div className="min-h-screen bg-gray-50 pb-16">
      <Navbar />

      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-gray-200 pb-6">
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
                Administrator Command Center
              </h1>
              <span className="inline-flex rounded-full bg-purple-100 px-3 py-1 text-xs font-bold text-purple-800 uppercase tracking-wider">
                ADMINISTRATOR
              </span>
            </div>
            <p className="mt-2 text-sm text-gray-600">
              Platform-wide telemetry, user governance, report velocity, and full immutable audit log table.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => router.push("/dashboard")}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors shadow-xs"
            >
              Standard Workspace
            </button>
          </div>
        </div>

        {/* 4 Core Admin KPI Cards */}
        <section className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {/* 1. Total Registered Users */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                Registered Users
              </span>
              <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-blue-100 text-blue-700 font-bold text-xs">
                U
              </span>
            </div>
            <p className="mt-3 text-3xl font-extrabold text-gray-900">
              {metrics ? metrics.totalUsers : "..."}
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Active user accounts in database
            </p>
          </div>

          {/* 2. Total Properties Searched/Stored */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                Properties Stored
              </span>
              <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-emerald-100 text-emerald-700 font-bold text-xs">
                P
              </span>
            </div>
            <p className="mt-3 text-3xl font-extrabold text-gray-900">
              {metrics ? metrics.totalProperties : "..."}
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Properties indexed in platform catalog
            </p>
          </div>

          {/* 3. Total Reports Generated */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                Total Reports Generated
              </span>
              <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-purple-100 text-purple-700 font-bold text-xs">
                R
              </span>
            </div>
            <p className="mt-3 text-3xl font-extrabold text-purple-700">
              {metrics ? metrics.totalReports : "..."}
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Historical due diligence dossiers
            </p>
          </div>

          {/* 4. Reports (Last 7 Days) */}
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                7-Day Report Velocity
              </span>
              <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-amber-100 text-amber-700 font-bold text-xs">
                7D
              </span>
            </div>
            <p className="mt-3 text-3xl font-extrabold text-amber-800">
              {metrics ? metrics.reportsLast7Days : "0"}
            </p>
            <p className="mt-1 text-xs text-gray-500">
              Generated in the trailing 7 days
            </p>
          </div>
        </section>

        {/* User Roles Distribution Bar */}
        {metrics && metrics.userRoles && (
          <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-xs">
            <h3 className="text-xs font-bold uppercase tracking-wider text-gray-500 mb-3">
              Platform Role Distribution
            </h3>
            <div className="flex flex-wrap gap-2.5">
              {Object.entries(metrics.userRoles).map(([roleName, count]) => (
                <div
                  key={roleName}
                  className="flex items-center gap-2 rounded-lg border border-gray-200 bg-gray-50/70 px-3 py-1.5"
                >
                  <span className="text-xs font-bold text-gray-800">
                    {roleName.replace(/_/g, " ")}
                  </span>
                  <span className="rounded-full bg-blue-600 px-2 py-0.2 text-[11px] font-bold text-white">
                    {count}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 5. Full Audit Log Table (with Filters & Pagination) */}
        <section id="audit-logs" className="rounded-2xl border border-gray-200 bg-white p-6 shadow-xs space-y-5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-gray-100 pb-4">
            <div>
              <div className="flex items-center gap-2">
                <span className="flex h-2.5 w-2.5 rounded-full bg-purple-600" />
                <h2 className="text-lg font-bold text-gray-900">
                  Full Platform Audit Log Table
                </h2>
              </div>
              <p className="text-xs text-gray-500 mt-0.5">
                Immutable security and action event log tracking report generation, registrations, and administrative activities
              </p>
            </div>
            <span className="text-xs font-semibold text-purple-800 bg-purple-50 px-3 py-1 rounded-full border border-purple-200">
              Total Log Records: {auditTotal}
            </span>
          </div>

          {/* Audit Filters Form */}
          <form onSubmit={handleFilterSubmit} className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5 p-4 rounded-xl bg-gray-50 border border-gray-200">
            {/* Filter by User */}
            <div>
              <label className="block text-[11px] font-semibold text-gray-600 uppercase mb-1">
                Filter by User Email
              </label>
              <input
                type="text"
                value={filterUser}
                onChange={(e) => setFilterUser(e.target.value)}
                placeholder="e.g. sivasri@gmail.com..."
                className="w-full rounded-lg border border-gray-300 px-3 py-1.5 text-xs text-gray-900 outline-none focus:border-purple-500 bg-white"
              />
            </div>

            {/* Filter by Action Type */}
            <div>
              <label className="block text-[11px] font-semibold text-gray-600 uppercase mb-1">
                Filter by Action Type
              </label>
              <select
                value={filterAction}
                onChange={(e) => setFilterAction(e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-1.5 text-xs text-gray-900 outline-none focus:border-purple-500 bg-white"
              >
                <option value="ALL">All Action Types</option>
                <option value="REPORT_GENERATION">REPORT_GENERATION</option>
                <option value="USER_REGISTRATION">USER_REGISTRATION</option>
                <option value="PROPERTY_SEARCH">PROPERTY_SEARCH</option>
                <option value="USER_LOGIN">USER_LOGIN</option>
              </select>
            </div>

            {/* Start Date */}
            <div>
              <label className="block text-[11px] font-semibold text-gray-600 uppercase mb-1">
                Start Date
              </label>
              <input
                type="date"
                value={filterStartDate}
                onChange={(e) => setFilterStartDate(e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-1.5 text-xs text-gray-900 outline-none focus:border-purple-500 bg-white"
              />
            </div>

            {/* End Date */}
            <div>
              <label className="block text-[11px] font-semibold text-gray-600 uppercase mb-1">
                End Date
              </label>
              <input
                type="date"
                value={filterEndDate}
                onChange={(e) => setFilterEndDate(e.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-1.5 text-xs text-gray-900 outline-none focus:border-purple-500 bg-white"
              />
            </div>

            {/* Submit & Reset */}
            <div className="flex items-end gap-2">
              <button
                type="submit"
                className="w-full rounded-lg bg-purple-600 px-3 py-2 text-xs font-semibold text-white hover:bg-purple-700 transition"
              >
                Apply Filters
              </button>
              <button
                type="button"
                onClick={handleResetFilters}
                className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs text-gray-600 hover:bg-gray-100 transition"
              >
                Reset
              </button>
            </div>
          </form>

          {/* Audit Log Table */}
          <div className="overflow-x-auto rounded-xl border border-gray-200">
            <table className="w-full text-left text-xs text-gray-700">
              <thead className="bg-gray-100 text-[11px] uppercase tracking-wider text-gray-700 border-b border-gray-200 font-bold">
                <tr>
                  <th className="py-3 px-4">Event ID</th>
                  <th className="py-3 px-4">Timestamp</th>
                  <th className="py-3 px-4">User</th>
                  <th className="py-3 px-4">Action Type</th>
                  <th className="py-3 px-4">Activity Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {auditLoading ? (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-xs text-gray-500">
                      Loading audit log records...
                    </td>
                  </tr>
                ) : auditLogs.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-xs text-gray-400">
                      No audit log records found for the selected filter parameters.
                    </td>
                  </tr>
                ) : (
                  auditLogs.map((log) => (
                    <tr key={log.id} className="hover:bg-gray-50 transition">
                      <td className="py-3 px-4 font-mono font-bold text-gray-500">
                        #{log.id}
                      </td>
                      <td className="py-3 px-4 text-gray-600 whitespace-nowrap">
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                      <td className="py-3 px-4 font-semibold text-gray-900 whitespace-nowrap">
                        {log.userEmail}
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        <span className={`inline-flex rounded-full px-2.5 py-0.5 text-[10px] font-bold ${
                          log.action === "REPORT_GENERATION"
                            ? "bg-purple-100 text-purple-800"
                            : log.action === "USER_REGISTRATION"
                            ? "bg-blue-100 text-blue-800"
                            : "bg-gray-100 text-gray-800"
                        }`}>
                          {log.action}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-gray-700 max-w-md line-clamp-2">
                        {log.details || "—"}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination Toolbar */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-2">
            <div className="flex items-center gap-2 text-xs text-gray-500">
              <span>Rows per page:</span>
              <select
                value={pageSize}
                onChange={(e) => {
                  setPageSize(Number(e.target.value));
                  setPage(0);
                }}
                className="rounded border border-gray-300 px-2 py-1 text-xs"
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
              <span>• Page {page + 1} of {auditPages}</span>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || auditLoading}
                className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-40 transition"
              >
                ← Previous
              </button>
              <button
                onClick={() => setPage((p) => Math.min(auditPages - 1, p + 1))}
                disabled={page >= auditPages - 1 || auditLoading}
                className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-40 transition"
              >
                Next →
              </button>
            </div>
          </div>
        </section>

        {/* Security & RBAC Diagnostic Console */}
        <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-xs space-y-4">
          <div>
            <h2 className="text-lg font-bold text-gray-900">
              Security & RBAC Diagnostic Console
            </h2>
            <p className="text-xs text-gray-500 mt-0.5">
              Verify active session authorization against Spring Security method constraints (<code>@PreAuthorize(&quot;hasRole(&apos;ADMINISTRATOR&apos;)&quot;)</code>).
            </p>
          </div>

          <div className="rounded-xl bg-gray-50 p-4 border border-gray-200">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <p className="text-xs font-mono text-gray-800 font-semibold">
                  GET /api/test/admin-only
                </p>
                <p className="text-xs text-gray-500 mt-0.5">
                  Restricted exclusively to Spring Security ADMINISTRATOR authority.
                </p>
              </div>

              <button
                onClick={handleTestAdminEndpoint}
                disabled={testingEndpoint}
                className="rounded-lg bg-purple-600 px-4 py-2 text-xs font-semibold text-white hover:bg-purple-700 disabled:opacity-60 transition shadow-xs whitespace-nowrap"
              >
                {testingEndpoint ? "Verifying..." : "Verify Administrator Role"}
              </button>
            </div>

            {testResult && (
              <div className="mt-3 rounded-lg bg-white p-3 border border-purple-200 text-xs font-mono text-purple-900">
                <span className="font-bold">Backend Response:</span> {testResult}
              </div>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}
