"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { apiRequest } from "@/lib/api";
import { getToken } from "@/lib/auth";
import Navbar from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";

interface Property {
  id: number;
  address: string;
  propertyType?: string | null;
  bedrooms?: number | null;
  bathrooms?: number | null;
  squareFeet?: number | null;
  yearBuilt?: number | null;
  latitude?: number | null;
  longitude?: number | null;
}

interface ComparableListing {
  id: number;
  comparableAddress: string;
  price?: number | null;
  squareFeet?: number | null;
  distanceMiles?: number | null;
  listedDate?: string | null;
  source?: string | null;
}

interface MarketTrend {
  averagePrice: number;
  averagePricePerSquareFoot: number;
  trend: string;
}

interface ValueHistoryEntry {
  year: number;
  assessedValue?: number | null;
}

interface PropertyTimelineEntry {
  date: string;
  label: string;
  description: string;
}

interface PropertyReportSummary {
  id: number;
  riskScore: number | null;
  status: string;
  createdAt: string;
  executiveSummary?: string | null;
}

export default function PropertyDetailsPage() {
  const router = useRouter();
  const params = useParams();
  const propertyId = params.id as string;
  const [reportLoading, setReportLoading] = useState(false);
  const [reportError, setReportError] = useState("");

  const [property, setProperty] = useState<Property | null>(null);

  const [comparables, setComparables] = useState<ComparableListing[]>([]);
  const [marketTrend, setMarketTrend] = useState<MarketTrend | null>(null);
  const [valueHistory, setValueHistory] = useState<ValueHistoryEntry[]>([]);
  const [timeline, setTimeline] = useState<PropertyTimelineEntry[]>([]);

  const [riskReport, setRiskReport] = useState<PropertyReportSummary | null>(null);
  const [riskLoading, setRiskLoading] = useState(true);
  const [riskError, setRiskError] = useState("");

  const [sortBy, setSortBy] = useState("distance");

  const [loading, setLoading] = useState(true);
  const [comparablesLoading, setComparablesLoading] = useState(true);
  const [valueHistoryLoading, setValueHistoryLoading] = useState(true);
  const [timelineLoading, setTimelineLoading] = useState(true);

  const [error, setError] = useState("");
  const [comparablesError, setComparablesError] = useState("");
  const [valueHistoryError, setValueHistoryError] = useState("");
  const [timelineError, setTimelineError] = useState("");

  useEffect(() => {
    const token = getToken();

    if (!token) {
      router.push("/login");
      return;
    }

    const fetchProperty = async () => {
      try {
        setLoading(true);
        setError("");

        const data = await apiRequest<Property>(
          `/api/properties/${propertyId}/details`,
          {
            token,
          }
        );

        setProperty(data);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load property details."
        );
      } finally {
        setLoading(false);
      }
    };

    fetchProperty();
  }, [propertyId, router]);

  useEffect(() => {
    const token = getToken();

    if (!token) {
      return;
    }

    const fetchComparables = async () => {
      try {
        setComparablesLoading(true);
        setComparablesError("");

        const data = await apiRequest<ComparableListing[]>(
          `/api/properties/${propertyId}/comparables?sortBy=${sortBy}`,
          {
            token,
          }
        );

        setComparables(Array.isArray(data) ? data : []);
      } catch (err) {
        setComparablesError(
          err instanceof Error
            ? err.message
            : "Failed to load comparable properties."
        );
      } finally {
        setComparablesLoading(false);
      }
    };

    fetchComparables();
  }, [propertyId, sortBy]);

  useEffect(() => {
    const token = getToken();

    if (!token) {
      return;
    }

    const fetchMarketTrend = async () => {
      try {
        const data = await apiRequest<MarketTrend>(
          `/api/properties/${propertyId}/comparables/trends`,
          {
            token,
          }
        );

        setMarketTrend(data);
      } catch {
        setMarketTrend(null);
      }
    };

    fetchMarketTrend();
  }, [propertyId]);

  useEffect(() => {
    const token = getToken();

    if (!token) {
      return;
    }

    const fetchValueHistory = async () => {
      try {
        setValueHistoryLoading(true);
        setValueHistoryError("");

        const data = await apiRequest<ValueHistoryEntry[]>(
          `/api/properties/${propertyId}/value-history`,
          {
            token,
          }
        );

        setValueHistory(Array.isArray(data) ? data : []);
      } catch (err) {
        setValueHistoryError(
          err instanceof Error
            ? err.message
            : "Failed to load value history."
        );
      } finally {
        setValueHistoryLoading(false);
      }
    };

    fetchValueHistory();
  }, [propertyId]);

  useEffect(() => {
    const token = getToken();

    if (!token) {
      return;
    }

    const fetchTimeline = async () => {
      try {
        setTimelineLoading(true);
        setTimelineError("");

        const data = await apiRequest<PropertyTimelineEntry[]>(
          `/api/properties/${propertyId}/history`,
          {
            token,
          }
        );

        setTimeline(Array.isArray(data) ? data : []);
      } catch (err) {
        setTimelineError(
          err instanceof Error
            ? err.message
            : "Failed to load property timeline."
        );
      } finally {
        setTimelineLoading(false);
      }
    };

    fetchTimeline();
  }, [propertyId]);

  useEffect(() => {
    const token = getToken();

    if (!token || !propertyId) {
      return;
    }

    const fetchRiskReport = async () => {
      try {
        setRiskLoading(true);
        setRiskError("");

        const profile = await apiRequest<{ id: number }>("/api/users/profile", {
          token,
        });

        const userReports = await apiRequest<PropertyReportSummary[]>(
          `/api/reports?user=${profile.id}`,
          {
            token,
          }
        );

        if (Array.isArray(userReports) && userReports.length > 0) {
          let found: PropertyReportSummary | null = null;
          // Traverse in reverse to get most recent report
          for (const rep of [...userReports].reverse()) {
            try {
              const detail = await apiRequest<{ propertyId: number }>(
                `/api/reports/${rep.id}`,
                { token }
              );
              if (detail.propertyId === Number(propertyId)) {
                found = rep;
                break;
              }
            } catch {
              // Ignore single report query error
            }
          }
          setRiskReport(found);
        } else {
          setRiskReport(null);
        }
      } catch {
        setRiskError("Risk assessment data is temporarily unavailable.");
      } finally {
        setRiskLoading(false);
      }
    };

    fetchRiskReport();
  }, [propertyId]);
  const handleRequestReport = async () => {
  const token = getToken();

  if (!token) {
    router.push("/login");
    return;
  }

  try {
    setReportLoading(true);
    setReportError("");

    const response = await apiRequest<{ id: number }>(
      `/api/properties/${propertyId}/reports`,
      {
        method: "POST",
        token,
      }
    );

    router.push(
      `/properties/${propertyId}/reports/${response.id}`
    );
  } catch (err) {
    setReportError(
      err instanceof Error
        ? err.message
        : "Failed to generate report."
    );
  } finally {
    setReportLoading(false);
  }
};

  if (loading) {
    return (
      <main className="min-h-screen bg-gray-50 p-8">
        <div className="mx-auto max-w-6xl">
          <p className="text-gray-600">Loading property details...</p>
        </div>
      </main>
    );
  }

  if (error || !property) {
    return (
      <main className="min-h-screen bg-gray-50 p-8">
        <div className="mx-auto max-w-6xl">
          <div className="rounded-lg bg-red-50 p-6 text-red-700">
            {error || "Property not found."}
          </div>

          <button
            onClick={() => router.push("/properties")}
            className="mt-4 rounded-lg bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
          >
            Back to Properties
          </button>
        </div>
      </main>
    );
  }

  const formatCurrency = (value?: number | null) => {
    if (value == null) {
      return "N/A";
    }

    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
      maximumFractionDigits: 0,
    }).format(value);
  };

  const formatNumber = (value?: number | null) => {
    if (value == null) {
      return "N/A";
    }

    return new Intl.NumberFormat("en-US").format(value);
  };

  const formatDate = (date?: string | null) => {
    if (!date) {
      return "N/A";
    }

    const parsedDate = new Date(date);

    if (Number.isNaN(parsedDate.getTime())) {
      return date;
    }

    return parsedDate.toLocaleDateString();
  };

  const maxAssessedValue =
    valueHistory.length > 0
      ? Math.max(
          ...valueHistory.map((item) => item.assessedValue ?? 0)
        )
      : 0;

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Navbar />

        <main className="mx-auto max-w-6xl space-y-8 p-4 sm:p-6 lg:p-8">
          {/* Header */}
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <button
                onClick={() => router.push("/properties")}
                className="mb-3 text-sm text-blue-600 hover:underline"
              >
                ← Back to Properties
              </button>

              <h1 className="text-3xl font-bold text-gray-900">
                Property Details
              </h1>

              <p className="mt-2 text-gray-600">{property.address}</p>
            </div>

            <button
              onClick={() =>
                router.push(`/properties/${propertyId}/due-diligence`)
              }
              className="rounded-lg bg-blue-600 px-5 py-3 font-medium text-white hover:bg-blue-700"
            >
              View Due Diligence
            </button>
          </div>

          {/* Property Information */}
          <section className="rounded-xl bg-white p-6 shadow-sm">
            <h2 className="mb-5 text-xl font-semibold text-gray-900">
              Property Information
            </h2>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Property Type</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {property.propertyType || "N/A"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Bedrooms</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {property.bedrooms ?? "N/A"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Bathrooms</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {property.bathrooms ?? "N/A"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Square Feet</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {formatNumber(property.squareFeet)}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Year Built</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {property.yearBuilt ?? "N/A"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Latitude</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {property.latitude ?? "N/A"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Longitude</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {property.longitude ?? "N/A"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-4">
                <p className="text-sm text-gray-500">Property ID</p>
                <p className="mt-1 font-semibold text-gray-900">
                  {property.id}
                </p>
              </div>
            </div>
          </section>

          {/* Risk Dashboard */}
          <section className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
            <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center mb-6">
              <div>
                <h2 className="text-xl font-bold text-gray-900">
                  Risk Assessment Dashboard
                </h2>
                <p className="mt-1 text-sm text-gray-500">
                  Automated multi-factor due diligence risk scoring.
                </p>
              </div>

              {riskReport && (
                <span
                  className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${
                    riskReport.status === "COMPLETED"
                      ? "bg-green-100 text-green-800"
                      : riskReport.status === "FAILED"
                      ? "bg-red-100 text-red-800"
                      : "bg-yellow-100 text-yellow-800"
                  }`}
                >
                  Status: {riskReport.status}
                </span>
              )}
            </div>

            {riskLoading ? (
              <div className="rounded-lg bg-gray-50 p-6 text-center">
                <div className="mx-auto h-6 w-6 animate-spin rounded-full border-2 border-blue-600 border-t-transparent mb-2" />
                <p className="text-sm text-gray-500">Loading risk assessment data...</p>
              </div>
            ) : riskError ? (
              <div className="rounded-lg border border-yellow-200 bg-yellow-50 p-5 text-yellow-800">
                <p className="font-semibold text-sm">Risk Assessment Notice</p>
                <p className="mt-1 text-xs">{riskError}</p>
              </div>
            ) : !riskReport ? (
              <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 p-8 text-center">
                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-blue-600 mb-3">
                  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                </div>
                <p className="text-base font-medium text-gray-900">
                  No risk report available yet.
                </p>
                <p className="mt-1 text-sm text-gray-500 max-w-md mx-auto">
                  Generate a comprehensive due diligence report to evaluate risk score, environmental flags, flood risk, and permits.
                </p>
                <button
                  onClick={handleRequestReport}
                  disabled={reportLoading}
                  className="mt-5 rounded-lg bg-green-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-green-700 disabled:opacity-60 transition-colors"
                >
                  {reportLoading ? "Generating Report..." : "Request Report"}
                </button>
              </div>
            ) : (
              <div className="space-y-6">
                <div className="grid gap-4 sm:grid-cols-3">
                  <div className="rounded-lg bg-gray-50 p-5 border border-gray-100">
                    <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Overall Risk Score
                    </p>
                    <div className="mt-2 flex items-baseline gap-2">
                      <span className="text-4xl font-extrabold text-gray-900">
                        {riskReport.riskScore != null ? riskReport.riskScore : "N/A"}
                      </span>
                      {riskReport.riskScore != null && (
                        <span className="text-sm font-medium text-gray-500">/ 100</span>
                      )}
                    </div>
                    <div className="mt-3">
                      {riskReport.riskScore != null ? (
                        riskReport.riskScore <= 33 ? (
                          <span className="inline-flex rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-semibold text-green-800">
                            Low Risk
                          </span>
                        ) : riskReport.riskScore <= 66 ? (
                          <span className="inline-flex rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-semibold text-amber-800">
                            Moderate Risk
                          </span>
                        ) : (
                          <span className="inline-flex rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-semibold text-red-800">
                            High Risk
                          </span>
                        )
                      ) : (
                        <span className="text-xs text-gray-400">Score pending calculation</span>
                      )}
                    </div>
                  </div>

                  <div className="rounded-lg bg-gray-50 p-5 border border-gray-100">
                    <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Report Status
                    </p>
                    <p className="mt-2 text-2xl font-bold text-gray-900">
                      {riskReport.status}
                    </p>
                    <p className="mt-3 text-xs text-gray-500">
                      Generated on {formatDate(riskReport.createdAt)}
                    </p>
                  </div>

                  <div className="rounded-lg bg-gray-50 p-5 border border-gray-100 flex flex-col justify-between">
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                        Full Intelligence Report
                      </p>
                      <p className="mt-2 text-xs text-gray-600">
                        Access complete breakdown including PDF & Excel download exports.
                      </p>
                    </div>
                    <button
                      onClick={() =>
                        router.push(
                          `/properties/${propertyId}/reports/${riskReport.id}`
                        )
                      }
                      className="mt-4 w-full rounded-lg bg-blue-600 px-4 py-2 text-center text-xs font-semibold text-white hover:bg-blue-700 transition-colors"
                    >
                      View Full Report →
                    </button>
                  </div>
                </div>

                {riskReport.executiveSummary && (
                  <div className="rounded-lg border border-gray-100 bg-gray-50 p-4">
                    <h4 className="text-xs font-semibold uppercase tracking-wider text-gray-500 mb-2">
                      Executive Summary Preview
                    </h4>
                    <p className="text-sm text-gray-700 leading-relaxed line-clamp-3">
                      {riskReport.executiveSummary}
                    </p>
                  </div>
                )}
              </div>
            )}
          </section>

        {/* Comparable Properties */}
        <section className="rounded-xl bg-white p-6 shadow-sm">
          <div className="mb-5 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">
                Comparable Properties
              </h2>

              <p className="mt-1 text-sm text-gray-500">
                Similar properties from the market.
              </p>
            </div>

            <select
              value={sortBy}
              onChange={(event) => setSortBy(event.target.value)}
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
            >
              <option value="distance">Sort by Distance</option>
              <option value="price">Sort by Price</option>
              <option value="listeddate">Sort by Listed Date</option>
            </select>
          </div>

          {comparablesLoading ? (
            <p className="text-gray-500">
              Loading comparable properties...
            </p>
          ) : comparablesError ? (
            <div className="rounded-lg bg-yellow-50 p-4 text-yellow-800">
              {comparablesError}
            </div>
          ) : comparables.length === 0 ? (
            <p className="text-gray-500">
              No comparable properties available.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse text-left">
                <thead>
                  <tr className="border-b bg-gray-50">
                    <th className="p-3 text-sm font-semibold text-gray-700">
                      Address
                    </th>
                    <th className="p-3 text-sm font-semibold text-gray-700">
                      Price
                    </th>
                    <th className="p-3 text-sm font-semibold text-gray-700">
                      Sq Ft
                    </th>
                    <th className="p-3 text-sm font-semibold text-gray-700">
                      Distance
                    </th>
                    <th className="p-3 text-sm font-semibold text-gray-700">
                      Listed Date
                    </th>
                    <th className="p-3 text-sm font-semibold text-gray-700">
                      Source
                    </th>
                  </tr>
                </thead>

                <tbody>
                  {comparables.map((comparable) => (
                    <tr
                      key={comparable.id}
                      className="border-b last:border-b-0"
                    >
                      <td className="p-3 text-sm text-gray-900">
                        {comparable.comparableAddress}
                      </td>

                      <td className="p-3 text-sm text-gray-700">
                        {formatCurrency(comparable.price)}
                      </td>

                      <td className="p-3 text-sm text-gray-700">
                        {formatNumber(comparable.squareFeet)}
                      </td>

                      <td className="p-3 text-sm text-gray-700">
                        {comparable.distanceMiles != null
                          ? `${comparable.distanceMiles.toFixed(2)} mi`
                          : "N/A"}
                      </td>

                      <td className="p-3 text-sm text-gray-700">
                        {formatDate(comparable.listedDate)}
                      </td>

                      <td className="p-3 text-sm text-gray-700">
                        {comparable.source || "N/A"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        {/* Market Trends */}
        <section className="rounded-xl bg-white p-6 shadow-sm">
          <h2 className="mb-5 text-xl font-semibold text-gray-900">
            Market Trends
          </h2>

          {!marketTrend || marketTrend.trend === "NO_DATA" ? (
            <div>
              <p className="mb-4 text-sm text-gray-500">
                No comparable market data available.
              </p>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                <div className="rounded-lg bg-gray-50 p-5">
                  <p className="text-sm text-gray-500">Average Price</p>
                  <p className="mt-2 text-2xl font-bold text-gray-400">
                    No data
                  </p>
                </div>

                <div className="rounded-lg bg-gray-50 p-5">
                  <p className="text-sm text-gray-500">
                    Average Price / Sq Ft
                  </p>
                  <p className="mt-2 text-2xl font-bold text-gray-400">
                    No data
                  </p>
                </div>

                <div className="rounded-lg bg-gray-50 p-5">
                  <p className="text-sm text-gray-500">Market Trend</p>
                  <p className="mt-2 text-2xl font-bold text-gray-400">
                    No data
                  </p>
                </div>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <div className="rounded-lg bg-gray-50 p-5">
                <p className="text-sm text-gray-500">Average Price</p>
                <p className="mt-2 text-2xl font-bold text-gray-900">
                  {marketTrend.averagePrice > 0
                    ? formatCurrency(marketTrend.averagePrice)
                    : "No data"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-5">
                <p className="text-sm text-gray-500">
                  Average Price / Sq Ft
                </p>
                <p className="mt-2 text-2xl font-bold text-gray-900">
                  {marketTrend.averagePricePerSquareFoot > 0
                    ? formatCurrency(marketTrend.averagePricePerSquareFoot)
                    : "No data"}
                </p>
              </div>

              <div className="rounded-lg bg-gray-50 p-5">
                <p className="text-sm text-gray-500">Market Trend</p>
                <p className="mt-2 text-2xl font-bold text-gray-900">
                  {marketTrend.trend && marketTrend.trend !== "NO_DATA"
                    ? marketTrend.trend
                    : "No data"}
                </p>
              </div>
            </div>
          )}
        </section>

        {/* Value History */}
        <section className="rounded-xl bg-white p-6 shadow-sm">
          <h2 className="mb-2 text-xl font-semibold text-gray-900">
            Property Value History
          </h2>

          <p className="mb-6 text-sm text-gray-500">
            Historical assessed values from available tax records.
          </p>

          {valueHistoryLoading ? (
            <p className="text-gray-500">
              Loading value history...
            </p>
          ) : valueHistoryError ? (
            <div className="rounded-lg bg-yellow-50 p-4 text-yellow-800">
              {valueHistoryError}
            </div>
          ) : valueHistory.length === 0 ? (
            <p className="text-gray-500">
              No value history available.
            </p>
          ) : (
            <div className="space-y-4">
              {valueHistory.map((item) => {
                const value = item.assessedValue ?? 0;

                const width =
                  maxAssessedValue > 0
                    ? Math.max(
                        (value / maxAssessedValue) * 100,
                        2
                      )
                    : 2;

                return (
                  <div key={item.year}>
                    <div className="mb-1 flex items-center justify-between text-sm">
                      <span className="font-medium text-gray-700">
                        {item.year}
                      </span>

                      <span className="font-semibold text-gray-900">
                        {formatCurrency(item.assessedValue)}
                      </span>
                    </div>

                    <div className="h-6 w-full overflow-hidden rounded bg-gray-100">
                      <div
                        className="h-full rounded bg-blue-500"
                        style={{ width: `${width}%` }}
                      />
                    </div>
                  </div>
                );
              })}

              <div className="mt-6 overflow-x-auto">
                <table className="w-full border-collapse text-left">
                  <thead>
                    <tr className="border-b bg-gray-50">
                      <th className="p-3 text-sm font-semibold text-gray-700">
                        Year
                      </th>
                      <th className="p-3 text-sm font-semibold text-gray-700">
                        Assessed Value
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {valueHistory.map((item) => (
                      <tr
                        key={item.year}
                        className="border-b last:border-b-0"
                      >
                        <td className="p-3 text-sm text-gray-900">
                          {item.year}
                        </td>

                        <td className="p-3 text-sm text-gray-700">
                          {formatCurrency(item.assessedValue)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </section>

        {/* Property Timeline */}
        <section className="rounded-xl bg-white p-6 shadow-sm">
          <h2 className="mb-2 text-xl font-semibold text-gray-900">
            Property Timeline
          </h2>

          <p className="mb-6 text-sm text-gray-500">
            Important ownership, tax, and permit events associated
            with this property.
          </p>

          {timelineLoading ? (
            <p className="text-gray-500">
              Loading property timeline...
            </p>
          ) : timelineError ? (
            <div className="rounded-lg bg-yellow-50 p-4 text-yellow-800">
              {timelineError}
            </div>
          ) : timeline.length === 0 ? (
            <p className="text-gray-500">
              No timeline events available.
            </p>
          ) : (
            <div className="relative">
              <div className="absolute bottom-0 left-[9px] top-0 w-px bg-gray-200" />

              <div className="space-y-6">
                {timeline.map((event, index) => (
                  <div
                    key={`${event.date}-${event.label}-${index}`}
                    className="relative flex gap-4"
                  >
                    <div className="relative z-10 mt-1 h-5 w-5 shrink-0 rounded-full border-4 border-white bg-blue-600 shadow" />

                    <div className="flex-1 rounded-lg border border-gray-200 bg-gray-50 p-4">
                      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                        <h3 className="font-semibold text-gray-900">
                          {event.label || "Event"}
                        </h3>

                        <span className="text-sm text-gray-500">
                          {formatDate(event.date)}
                        </span>
                      </div>

                      <p className="mt-2 text-sm leading-6 text-gray-700">
                        {event.description || "No description available."}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </section>

        {/* Bottom Actions */}
        <div className="flex flex-wrap gap-3">
  <button
    onClick={() =>
      router.push(`/properties/${propertyId}/due-diligence`)
    }
    className="rounded-lg bg-blue-600 px-5 py-3 font-medium text-white hover:bg-blue-700"
  >
    View Due Diligence
  </button>

  <button
    onClick={handleRequestReport}
    disabled={reportLoading}
    className="rounded-lg bg-green-600 px-5 py-3 font-medium text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-60"
  >
    {reportLoading ? "Generating Report..." : "Request Report"}
  </button>

  <button
    onClick={() => router.push("/properties")}
    className="rounded-lg border border-gray-300 bg-white px-5 py-3 font-medium text-gray-700 hover:bg-gray-50"
  >
    Back to Properties
  </button>
</div>

{reportError && (
  <div className="mt-3 rounded-lg bg-red-50 p-3 text-sm text-red-700">
    {reportError}
  </div>
)}
        </main>
      </div>
    </AuthGuard>
  );
}