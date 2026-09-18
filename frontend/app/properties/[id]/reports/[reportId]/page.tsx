"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { apiRequest } from "@/lib/api";
import { getToken } from "@/lib/auth";
import Navbar from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";

interface Report {
  id: number;
  riskScore: number | null;
  executiveSummary: string | null;
  propertyTimeline: string | null;
  pdfUrl: string | null;
  excelUrl: string | null;
  status: "REQUESTED" | "IN_PROGRESS" | "COMPLETED" | "FAILED";
  createdAt: string;
}

interface TimelineEntry {
  date: string | null;
  label: string | null;
  description: string | null;
}

export default function ReportPage() {
  const params = useParams();
  const router = useRouter();

  const propertyId = params.id as string;
  const reportId = params.reportId as string;

  const [report, setReport] = useState<Report | null>(null);
  const [timeline, setTimeline] = useState<TimelineEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadReport = async () => {
    const token = getToken();

    if (!token) {
      router.push("/login");
      return;
    }

    try {
      const data = await apiRequest<Report>(
        `/api/properties/${propertyId}/reports/${reportId}`,
        {
          method: "GET",
          token,
        }
      );

      setReport(data);

      if (data.propertyTimeline) {
        try {
          const parsed = JSON.parse(data.propertyTimeline);

          if (Array.isArray(parsed)) {
            setTimeline(parsed);
          }
        } catch {
          console.log("Property timeline is not valid JSON.");
        }
      }

      setError("");
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to load report."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReport();
  }, [propertyId, reportId]);

  const getStatusMessage = () => {
    if (!report) return "";

    switch (report.status) {
      case "REQUESTED":
        return "Report has been requested.";
      case "IN_PROGRESS":
        return "Report is currently being generated.";
      case "COMPLETED":
        return "Report generation completed successfully.";
      case "FAILED":
        return "Report generation failed.";
      default:
        return "";
    }
  };

  const downloadFile = async (type: "pdf" | "excel") => {
    const token = getToken();

    if (!token) {
      router.push("/login");
      return;
    }

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
        throw new Error(`Download failed (${response.status})`);
      }

      const blob = await response.blob();

      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download =
        type === "pdf"
          ? `report-${reportId}.pdf`
          : `report-${reportId}.xlsx`;

      document.body.appendChild(link);
      link.click();
      link.remove();

      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to download file."
      );
    }
  };

  if (loading) {
    return (
      <main className="min-h-screen bg-gray-50 p-8 flex items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
          <p className="mt-4 text-sm text-gray-600">Loading report...</p>
        </div>
      </main>
    );
  }

  if (error || !report) {
    return (
      <AuthGuard>
        <div className="min-h-screen bg-gray-50">
          <Navbar />
          <div className="mx-auto max-w-5xl p-6 sm:p-8">
            <div className="rounded-xl bg-white p-6 shadow-xs border border-gray-200">
              <h1 className="mb-3 text-2xl font-bold text-gray-900">
                Report
              </h1>

              <p className="mb-5 text-red-600">
                {error || "Report not found."}
              </p>

              <button
                onClick={() =>
                  router.push(`/properties/${propertyId}`)
                }
                className="rounded-lg bg-blue-600 px-5 py-2.5 font-medium text-white hover:bg-blue-700 transition-colors"
              >
                Back to Property
              </button>
            </div>
          </div>
        </div>
      </AuthGuard>
    );
  }

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Navbar />

        <main className="mx-auto max-w-5xl space-y-6 p-4 sm:p-6 lg:p-8">

        {/* Header */}
        <div className="rounded-xl bg-white p-6 shadow">
          <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                Due Diligence Report
              </h1>

              <p className="mt-1 text-gray-600">
                Report ID: {report.id}
              </p>
            </div>

            <div
              className={`inline-flex rounded-full px-4 py-2 text-sm font-semibold ${
                report.status === "COMPLETED"
                  ? "bg-green-100 text-green-700"
                  : report.status === "FAILED"
                  ? "bg-red-100 text-red-700"
                  : "bg-yellow-100 text-yellow-700"
              }`}
            >
              {report.status}
            </div>
          </div>

          <p className="mt-4 text-gray-600">
            {getStatusMessage()}
          </p>
        </div>

        {/* Risk Score */}
        <section className="rounded-xl bg-white p-6 shadow">
          <h2 className="mb-4 text-xl font-bold text-gray-900">
            Risk Assessment
          </h2>

          <div className="rounded-lg bg-gray-50 p-5">
            <p className="text-sm text-gray-500">
              Overall Risk Score
            </p>

            <p className="mt-2 text-4xl font-bold text-gray-900">
              {report.riskScore ?? "N/A"}
            </p>

            <p className="mt-2 text-sm text-gray-500">
              Risk score generated from the available due diligence
              assessments.
            </p>
          </div>
        </section>

        {/* Executive Summary */}
        <section className="rounded-xl bg-white p-6 shadow">
          <h2 className="mb-4 text-xl font-bold text-gray-900">
            Executive Summary
          </h2>

          {report.executiveSummary ? (
            <div className="whitespace-pre-wrap rounded-lg bg-gray-50 p-5 leading-7 text-gray-700">
              {report.executiveSummary}
            </div>
          ) : (
            <p className="text-gray-500">
              No executive summary is available.
            </p>
          )}
        </section>

        {/* Property Timeline */}
        <section className="rounded-xl bg-white p-6 shadow">
          <h2 className="mb-5 text-xl font-bold text-gray-900">
            Property Timeline
          </h2>

          {timeline.length === 0 ? (
            <p className="text-gray-500">
              No timeline events are available.
            </p>
          ) : (
            <div className="space-y-5">
              {timeline.map((event, index) => (
                <div
                  key={`${event.date}-${index}`}
                  className="border-l-4 border-blue-500 pl-5"
                >
                  <p className="text-sm font-medium text-blue-600">
                    {event.date || "Unknown date"}
                  </p>

                  <h3 className="mt-1 font-semibold text-gray-900">
                    {event.label || "Event"}
                  </h3>

                  <p className="mt-1 text-gray-600">
                    {event.description || "No description available."}
                  </p>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Downloads */}
        {report.status === "COMPLETED" && (
          <section className="rounded-xl bg-white p-6 shadow">
            <h2 className="mb-4 text-xl font-bold text-gray-900">
              Download Report
            </h2>

            <div className="flex flex-wrap gap-3">
              <button
                onClick={() => downloadFile("pdf")}
                className="rounded-lg bg-red-600 px-5 py-3 font-medium text-white hover:bg-red-700"
              >
                Download PDF
              </button>

              <button
                onClick={() => downloadFile("excel")}
                className="rounded-lg bg-green-600 px-5 py-3 font-medium text-white hover:bg-green-700"
              >
                Download Excel
              </button>
            </div>
          </section>
        )}

        {/* Navigation */}
        <div className="flex flex-wrap gap-3">
          <button
            onClick={() =>
              router.push(`/properties/${propertyId}`)
            }
            className="rounded-lg border border-gray-300 bg-white px-5 py-3 font-medium text-gray-700 hover:bg-gray-50"
          >
            Back to Property
          </button>

          <button
            onClick={() =>
              router.push(`/properties/${propertyId}/due-diligence`)
            }
            className="rounded-lg bg-blue-600 px-5 py-3 font-medium text-white hover:bg-blue-700"
          >
            View Due Diligence
          </button>
        </div>
        </main>
      </div>
    </AuthGuard>
  );
}