"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiRequest } from "@/lib/api";
import { getToken } from "@/lib/auth";
import Navbar from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";

interface ReportSummary {
  id: number;
  riskScore: number | null;
  status: "REQUESTED" | "IN_PROGRESS" | "COMPLETED" | "FAILED" | string;
  createdAt: string;
  executiveSummary?: string | null;
  propertyTimeline?: string | null;
  pdfUrl?: string | null;
  excelUrl?: string | null;
}

interface EnrichedReport extends ReportSummary {
  propertyId?: number | null;
  propertyAddress?: string | null;
}

interface UserProfile {
  id: number;
  fullName: string;
  email: string;
}

interface PropertyBasic {
  id: number;
  address: string;
}

export default function ReportsHistoryPage() {
  const router = useRouter();

  const [reports, setReports] = useState<EnrichedReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [error, setError] = useState("");
  const [downloadError, setDownloadError] = useState("");

  useEffect(() => {
    async function loadReportHistory() {
      const token = getToken();

      if (!token) {
        router.replace("/login");
        return;
      }

      try {
        setLoading(true);
        setError("");

        // Step 1: Load current user profile to obtain user ID
        const profile = await apiRequest<UserProfile>("/api/users/profile", {
          token,
        });

        // Step 2: Load reports for this user via ReportHistoryController (GET /api/reports?user={userId})
        const userReports = await apiRequest<ReportSummary[]>(
          `/api/reports?user=${profile.id}`,
          {
            token,
          }
        );

        if (!Array.isArray(userReports) || userReports.length === 0) {
          setReports([]);
          setLoading(false);
          return;
        }

        // Step 3: Load properties to map propertyId -> address
        let propertyMap: Record<number, string> = {};
        try {
          const allProperties = await apiRequest<PropertyBasic[]>("/api/properties", {
            token,
          });
          if (Array.isArray(allProperties)) {
            allProperties.forEach((p) => {
              propertyMap[p.id] = p.address;
            });
          }
        } catch {
          // Property address mapping is optional; report will still show property ID
        }

        // Step 4: Enrich each report with propertyId via ReportQueryController (GET /api/reports/{id})
        const enriched: EnrichedReport[] = await Promise.all(
          userReports.map(async (report) => {
            try {
              const detail = await apiRequest<{ propertyId: number }>(
                `/api/reports/${report.id}`,
                { token }
              );
              return {
                ...report,
                propertyId: detail.propertyId,
                propertyAddress: propertyMap[detail.propertyId] || null,
              };
            } catch {
              return {
                ...report,
                propertyId: null,
                propertyAddress: null,
              };
            }
          })
        );

        // Sort descending by ID / creation
        enriched.sort((a, b) => b.id - a.id);
        setReports(enriched);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load report history. Please try again."
        );
      } finally {
        setLoading(false);
      }
    }

    loadReportHistory();
  }, [router]);

  async function downloadReportFile(
    propertyId: number,
    reportId: number,
    type: "pdf" | "excel"
  ) {
    const token = getToken();

    if (!token) {
      router.push("/login");
      return;
    }

    const downloadKey = `${reportId}-${type}`;
    setDownloadingId(downloadKey);
    setDownloadError("");

    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const response = await fetch(
        `${apiUrl}/api/properties/${propertyId}/reports/${reportId}/${type}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error(`Download failed with status ${response.status}`);
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download =
        type === "pdf" ? `report-${reportId}.pdf` : `report-${reportId}.xlsx`;

      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setDownloadError(
        err instanceof Error
          ? err.message
          : `Failed to download ${type.toUpperCase()} file.`
      );
    } finally {
      setDownloadingId(null);
    }
  }

  const formatDate = (dateStr?: string | null) => {
    if (!dateStr) return "N/A";
    const date = new Date(dateStr);
    if (Number.isNaN(date.getTime())) return dateStr;
    return date.toLocaleDateString(undefined, {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return (
          <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-semibold text-green-800">
            COMPLETED
          </span>
        );
      case "IN_PROGRESS":
        return (
          <span className="inline-flex items-center rounded-full bg-yellow-100 px-2.5 py-0.5 text-xs font-semibold text-yellow-800">
            IN PROGRESS
          </span>
        );
      case "FAILED":
        return (
          <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-semibold text-red-800">
            FAILED
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-semibold text-gray-800">
            {status}
          </span>
        );
    }
  };

  const getRiskBadge = (score: number | null) => {
    if (score == null) {
      return <span className="text-gray-400 font-medium text-sm">—</span>;
    }

    if (score <= 33) {
      return (
        <span className="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-bold text-green-700 border border-green-200">
          {score} / 100 (Low)
        </span>
      );
    }
    if (score <= 66) {
      return (
        <span className="inline-flex items-center rounded-md bg-amber-50 px-2 py-1 text-xs font-bold text-amber-700 border border-amber-200">
          {score} / 100 (Moderate)
        </span>
      );
    }
    return (
      <span className="inline-flex items-center rounded-md bg-red-50 px-2 py-1 text-xs font-bold text-red-700 border border-red-200">
        {score} / 100 (High)
      </span>
    );
  };

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Navbar />

        <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                Report History
              </h1>
              <p className="mt-1 text-sm text-gray-600">
                View and download previously generated due diligence reports.
              </p>
            </div>

            <button
              onClick={() => router.push("/properties")}
              className="rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-blue-700 transition-colors shadow-xs"
            >
              Generate New Report
            </button>
          </div>

          {/* Download Error Banner */}
          {downloadError && (
            <div className="mb-6 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              {downloadError}
            </div>
          )}

          {/* Content States */}
          {loading ? (
            <div className="flex min-h-[300px] flex-col items-center justify-center rounded-xl border border-gray-200 bg-white p-12">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
              <p className="mt-4 text-sm text-gray-600">Loading your report history...</p>
            </div>
          ) : error ? (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-700">
              <h3 className="text-lg font-semibold">Error Loading Reports</h3>
              <p className="mt-1 text-sm">{error}</p>
              <button
                onClick={() => window.location.reload()}
                className="mt-4 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
              >
                Retry
              </button>
            </div>
          ) : reports.length === 0 ? (
            <div className="flex min-h-[300px] flex-col items-center justify-center rounded-xl border border-dashed border-gray-300 bg-white p-12 text-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-50 text-blue-600 mb-4">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <h3 className="text-base font-semibold text-gray-900">
                No reports generated yet
              </h3>
              <p className="mt-1 text-sm text-gray-500 max-w-sm">
                You have not generated any due diligence reports. Search for a property to generate your first analysis.
              </p>
              <button
                onClick={() => router.push("/properties")}
                className="mt-5 rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-700"
              >
                Browse Properties
              </button>
            </div>
          ) : (
            <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-xs">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="border-b border-gray-200 bg-gray-50/75 text-xs font-semibold uppercase text-gray-500">
                    <tr>
                      <th className="px-6 py-4">Report ID</th>
                      <th className="px-6 py-4">Property / Address</th>
                      <th className="px-6 py-4">Risk Score</th>
                      <th className="px-6 py-4">Status</th>
                      <th className="px-6 py-4">Created Date</th>
                      <th className="px-6 py-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {reports.map((report) => (
                      <tr key={report.id} className="hover:bg-gray-50/80 transition-colors">
                        <td className="px-6 py-4 font-mono font-semibold text-gray-900">
                          #{report.id}
                        </td>
                        <td className="px-6 py-4">
                          {report.propertyAddress ? (
                            <div>
                              <p className="font-medium text-gray-900 line-clamp-1">
                                {report.propertyAddress}
                              </p>
                              <p className="text-xs text-gray-400">
                                Property ID: {report.propertyId}
                              </p>
                            </div>
                          ) : report.propertyId ? (
                            <span className="font-medium text-gray-900">
                              Property ID #{report.propertyId}
                            </span>
                          ) : (
                            <span className="text-gray-400">—</span>
                          )}
                        </td>
                        <td className="px-6 py-4">
                          {getRiskBadge(report.riskScore)}
                        </td>
                        <td className="px-6 py-4">
                          {getStatusBadge(report.status)}
                        </td>
                        <td className="px-6 py-4 text-xs text-gray-600 whitespace-nowrap">
                          {formatDate(report.createdAt)}
                        </td>
                        <td className="px-6 py-4 text-right whitespace-nowrap">
                          <div className="flex items-center justify-end gap-2">
                            {report.propertyId && (
                              <button
                                onClick={() =>
                                  router.push(
                                    `/properties/${report.propertyId}/reports/${report.id}`
                                  )
                                }
                                className="rounded-md bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-700 hover:bg-blue-100 transition-colors"
                              >
                                View Report
                              </button>
                            )}

                            {report.status === "COMPLETED" && report.propertyId && (
                              <>
                                <button
                                  onClick={() =>
                                    downloadReportFile(
                                      report.propertyId!,
                                      report.id,
                                      "pdf"
                                    )
                                  }
                                  disabled={
                                    downloadingId === `${report.id}-pdf`
                                  }
                                  className="rounded-md border border-red-200 bg-red-50 px-3 py-1.5 text-xs font-semibold text-red-700 hover:bg-red-100 disabled:opacity-50 transition-colors"
                                  title="Download PDF"
                                >
                                  {downloadingId === `${report.id}-pdf`
                                    ? "..."
                                    : "PDF"}
                                </button>

                                <button
                                  onClick={() =>
                                    downloadReportFile(
                                      report.propertyId!,
                                      report.id,
                                      "excel"
                                    )
                                  }
                                  disabled={
                                    downloadingId === `${report.id}-excel`
                                  }
                                  className="rounded-md border border-green-200 bg-green-50 px-3 py-1.5 text-xs font-semibold text-green-700 hover:bg-green-100 disabled:opacity-50 transition-colors"
                                  title="Download Excel"
                                >
                                  {downloadingId === `${report.id}-excel`
                                    ? "..."
                                    : "Excel"}
                                </button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </main>
      </div>
    </AuthGuard>
  );
}
