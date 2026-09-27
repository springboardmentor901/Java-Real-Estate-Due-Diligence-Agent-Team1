"use client";

import { useEffect, useState, useCallback, FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { apiRequest } from "@/lib/api";
import { getToken } from "@/lib/auth";

interface UserProfile {
  id: number;
  fullName: string;
  email: string;
  role: string;
}

interface Property {
  id: number;
  address: string;
  propertyType?: string | null;
  bedrooms?: number | null;
  bathrooms?: number | null;
  squareFeet?: number | null;
  yearBuilt?: number | null;
}

interface ReportSummary {
  id: number;
  riskScore: number | null;
  status: string;
  createdAt: string;
}

interface EnrichedReport extends ReportSummary {
  propertyId?: number | null;
  propertyAddress?: string | null;
}

interface DueDiligenceRiskData {
  overallScore: number | null;
  floodStatus: string;
  taxStatus: string;
  propertyAddress: string;
  propertyId: number;
}

export default function BuyerDashboard({ user }: { user: UserProfile }) {
  const router = useRouter();

  const [properties, setProperties] = useState<Property[]>([]);
  const [reports, setReports] = useState<EnrichedReport[]>([]);
  const [latestRisk, setLatestRisk] = useState<DueDiligenceRiskData | null>(null);
  const [loading, setLoading] = useState(true);

  // Search state
  const [searchQuery, setSearchQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [searchResults, setSearchResults] = useState<Property[] | null>(null);
  const [searchError, setSearchError] = useState("");

  const loadData = useCallback(async () => {
    const token = getToken();
    if (!token) return;

    try {
      setLoading(true);

      // 1. Fetch available properties
      let propList: Property[] = [];
      try {
        const pData = await apiRequest<Property[]>("/api/properties", { token });
        if (Array.isArray(pData)) {
          propList = pData;
          setProperties(pData.slice(0, 6)); // Display top 6
        }
      } catch {
        // Non-blocking
      }

      // 2. Fetch Buyer's requested reports
      let repList: EnrichedReport[] = [];
      try {
        const rData = await apiRequest<ReportSummary[]>(`/api/reports?user=${user.id}`, { token });
        if (Array.isArray(rData) && rData.length > 0) {
          const propertyMap: Record<number, string> = {};
          propList.forEach((p) => {
            propertyMap[p.id] = p.address;
          });

          // Enrich reports with property details
          const enriched = await Promise.all(
            rData.map(async (r) => {
              try {
                const detail = await apiRequest<{ propertyId: number }>(`/api/reports/${r.id}`, { token });
                return {
                  ...r,
                  propertyId: detail.propertyId,
                  propertyAddress: propertyMap[detail.propertyId] || `Property #${detail.propertyId}`,
                };
              } catch {
                return {
                  ...r,
                  propertyId: null,
                  propertyAddress: "Property Evaluation",
                };
              }
            })
          );

          enriched.sort((a, b) => b.id - a.id);
          repList = enriched;
          setReports(enriched);
        }
      } catch {
        // Non-blocking
      }

      // 3. Inspect latest evaluated property for Risk summary
      if (repList.length > 0 && typeof repList[0].propertyId === "number") {
        const topRep = repList[0];
        const currentPropId = repList[0].propertyId;
        try {
          // Fetch due-diligence data for the latest property
          const dd = await apiRequest<{
            floodZone?: { data?: { floodRiskRating?: string; floodZone?: string } };
            taxHistory?: { data?: Array<{ paymentStatus?: string }> };
          }>(`/api/properties/${currentPropId}/due-diligence`, { token });

          let floodStatus = "Minimal Risk";
          if (dd?.floodZone?.data?.floodRiskRating) {
            floodStatus = dd.floodZone.data.floodRiskRating;
          } else if (dd?.floodZone?.data?.floodZone) {
            floodStatus = dd.floodZone.data.floodZone.includes("A") || dd.floodZone.data.floodZone.includes("V")
              ? "High Risk"
              : "Minimal Risk";
          }

          let taxStatus = "Current";
          if (dd?.taxHistory?.data && dd.taxHistory.data.length > 0) {
            const hasDelinquent = dd.taxHistory.data.some(
              (t) => t.paymentStatus && t.paymentStatus.toUpperCase().includes("DUE")
            );
            taxStatus = hasDelinquent ? "Tax Due" : "Current";
          }

          setLatestRisk({
            overallScore: topRep.riskScore,
            floodStatus,
            taxStatus,
            propertyAddress: topRep.propertyAddress || `Property #${currentPropId}`,
            propertyId: currentPropId,
          });
        } catch {
          setLatestRisk({
            overallScore: topRep.riskScore,
            floodStatus: "Data Unavailable",
            taxStatus: "Analysis Pending",
            propertyAddress: topRep.propertyAddress || `Property #${currentPropId}`,
            propertyId: currentPropId,
          });
        }
      } else if (propList.length > 0) {
        // Fallback to first property in database if no reports requested yet
        const firstProp = propList[0];
        setLatestRisk({
          overallScore: null,
          floodStatus: "Evaluation Ready",
          taxStatus: "Evaluation Ready",
          propertyAddress: firstProp.address,
          propertyId: firstProp.id,
        });
      }
    } finally {
      setLoading(false);
    }
  }, [user.id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  async function handleSearch(e: FormEvent) {
    e.preventDefault();
    const query = searchQuery.trim();
    if (!query) {
      setSearchResults(null);
      setSearchError("");
      return;
    }

    const token = getToken();
    if (!token) return;

    try {
      setSearching(true);
      setSearchError("");
      const results = await apiRequest<Property[]>(
        `/api/properties/search?address=${encodeURIComponent(query)}`,
        { token }
      );
      if (Array.isArray(results) && results.length > 0) {
        setSearchResults(results);
      } else {
        setSearchResults([]);
        setSearchError("No properties matched your search query. Try another address.");
      }
    } catch (err) {
      setSearchError(err instanceof Error ? err.message : "Search failed. Please try again.");
    } finally {
      setSearching(false);
    }
  }

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <div className="rounded-2xl bg-linear-to-r from-blue-700 to-indigo-800 p-6 sm:p-8 text-white shadow-md">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2.5">
              <span className="rounded-full bg-blue-500/30 px-3 py-1 text-xs font-semibold uppercase tracking-wider text-blue-100">
                Buyer Dashboard
              </span>
              <span className="text-xs text-blue-200">Personal Purchase Evaluation</span>
            </div>
            <h1 className="mt-2 text-2xl sm:text-3xl font-bold tracking-tight">
              Welcome, {user.fullName}
            </h1>
            <p className="mt-1 text-sm text-blue-100 max-w-2xl">
              Evaluate real estate risk, inspect comprehensive due diligence data, and verify title, flood, and tax history before making your purchase decision.
            </p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={() => router.push("/properties")}
              className="rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-blue-700 hover:bg-blue-50 transition-colors shadow-xs"
            >
              Browse All Properties →
            </button>
          </div>
        </div>
      </div>

      {/* 1. Prominent Property Search Bar */}
      <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-xs">
        <div className="mb-4">
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            Search Property to Evaluate
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Enter a property address to inspect details, flood exposure, tax status, and generate a due diligence report.
          </p>
        </div>

        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="e.g. 1825 Montcalm St, Indianapolis, IN or 123 Main Street..."
              className="w-full rounded-xl border border-gray-300 px-4 py-3 pl-11 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition"
            />
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            {searchQuery && (
              <button
                type="button"
                onClick={() => {
                  setSearchQuery("");
                  setSearchResults(null);
                  setSearchError("");
                }}
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-xs text-gray-400 hover:text-gray-600"
              >
                Clear
              </button>
            )}
          </div>
          <button
            type="submit"
            disabled={searching}
            className="rounded-xl bg-blue-600 px-6 py-3 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60 transition shadow-xs whitespace-nowrap"
          >
            {searching ? "Searching..." : "Evaluate Address"}
          </button>
        </form>

        {searchError && (
          <p className="mt-3 text-xs text-amber-700 bg-amber-50 p-2.5 rounded-lg border border-amber-200">
            {searchError}
          </p>
        )}

        {/* Live Search Results */}
        {searchResults && searchResults.length > 0 && (
          <div className="mt-4 border-t border-gray-100 pt-4">
            <h3 className="text-xs font-semibold uppercase tracking-wider text-gray-500 mb-3">
              Search Matches ({searchResults.length})
            </h3>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {searchResults.map((prop) => (
                <div
                  key={prop.id}
                  className="rounded-xl border border-blue-100 bg-blue-50/40 p-4 hover:border-blue-300 transition flex flex-col justify-between"
                >
                  <div>
                    <span className="rounded-full bg-blue-100 px-2 py-0.5 text-[10px] font-bold text-blue-800 uppercase">
                      {prop.propertyType || "Residential"}
                    </span>
                    <h4 className="mt-2 text-sm font-bold text-gray-900 line-clamp-2">
                      {prop.address}
                    </h4>
                    <p className="mt-1 text-xs text-gray-600">
                      {[
                        prop.bedrooms != null ? `${prop.bedrooms} Beds` : null,
                        prop.bathrooms != null ? `${prop.bathrooms} Baths` : null,
                        prop.squareFeet != null ? `${prop.squareFeet.toLocaleString()} SqFt` : null,
                      ].filter(Boolean).join(" • ") || "Property specifications available"}
                    </p>
                  </div>
                  <div className="mt-4 flex items-center gap-2">
                    <button
                      onClick={() => router.push(`/properties/${prop.id}`)}
                      className="w-full rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-blue-700 transition"
                    >
                      View Details & Due Diligence →
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </section>

      {/* 2. Key Due Diligence Risk Indicators (Latest Evaluated Property) */}
      {latestRisk && (
        <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-xs">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4 border-b border-gray-100 pb-3">
            <div>
              <h2 className="text-lg font-bold text-gray-900">
                Single Property Risk Snapshot
              </h2>
              <p className="text-xs text-gray-500">
                Current evaluation for: <span className="font-semibold text-gray-800">{latestRisk.propertyAddress}</span>
              </p>
            </div>
            <Link
              href={`/properties/${latestRisk.propertyId}`}
              className="text-xs font-semibold text-blue-600 hover:text-blue-800"
            >
              Full Property Details →
            </Link>
          </div>

          <div className="grid gap-4 sm:grid-cols-3">
            {/* Overall Risk Score */}
            <div className="rounded-xl border border-gray-100 bg-gray-50/70 p-4">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                Overall Risk Score
              </span>
              <div className="mt-2 flex items-baseline gap-2">
                <span className="text-3xl font-extrabold text-gray-900">
                  {latestRisk.overallScore != null ? latestRisk.overallScore : "—"}
                </span>
                <span className="text-xs text-gray-500">/ 100</span>
              </div>
              <p className="mt-2 text-xs">
                {latestRisk.overallScore != null ? (
                  latestRisk.overallScore <= 33 ? (
                    <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 font-semibold text-green-800">
                      Low Risk Profile
                    </span>
                  ) : latestRisk.overallScore <= 66 ? (
                    <span className="inline-flex items-center rounded-full bg-yellow-100 px-2.5 py-0.5 font-semibold text-yellow-800">
                      Moderate Risk
                    </span>
                  ) : (
                    <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 font-semibold text-red-800">
                      Elevated Risk
                    </span>
                  )
                ) : (
                  <span className="text-gray-500">Report not requested yet</span>
                )}
              </p>
            </div>

            {/* Flood Risk */}
            <div className="rounded-xl border border-gray-100 bg-gray-50/70 p-4">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                Flood Risk Status
              </span>
              <div className="mt-2 text-xl font-bold text-gray-900">
                {latestRisk.floodStatus}
              </div>
              <p className="mt-2 text-xs text-gray-500">
                FEMA NFHL hazard assessment & flood zone evaluation
              </p>
            </div>

            {/* Tax Risk */}
            <div className="rounded-xl border border-gray-100 bg-gray-50/70 p-4">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                Property Tax Status
              </span>
              <div className="mt-2 text-xl font-bold text-gray-900">
                {latestRisk.taxStatus}
              </div>
              <p className="mt-2 text-xs text-gray-500">
                Municipal assessment & delinquent tax verification
              </p>
            </div>
          </div>
        </section>
      )}

      {/* 3. Buyer's Requested Reports */}
      <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-xs">
        <div className="flex items-center justify-between mb-4 border-b border-gray-100 pb-3">
          <div>
            <h2 className="text-lg font-bold text-gray-900">
              Your Requested Due Diligence Reports
            </h2>
            <p className="text-xs text-gray-500">
              Reports generated for your personal property evaluations
            </p>
          </div>
          <Link href="/reports" className="text-xs font-semibold text-blue-600 hover:text-blue-800">
            View All Reports ({reports.length}) →
          </Link>
        </div>

        {reports.length === 0 ? (
          <div className="rounded-xl border border-dashed border-gray-300 p-8 text-center">
            <svg className="mx-auto h-10 w-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <p className="mt-2 text-sm font-semibold text-gray-900">No reports requested yet</p>
            <p className="mt-1 text-xs text-gray-500">
              Search a property above and click &quot;Request Report&quot; on the property details page.
            </p>
            <button
              onClick={() => router.push("/properties")}
              className="mt-4 rounded-lg bg-blue-600 px-4 py-2 text-xs font-semibold text-white hover:bg-blue-700 transition"
            >
              Browse Properties to Evaluate
            </button>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {reports.slice(0, 5).map((rep) => (
              <div key={rep.id} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-sm text-gray-900">
                      Report #{rep.id}
                    </span>
                    <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-bold ${
                      rep.status === "COMPLETED"
                        ? "bg-green-100 text-green-800"
                        : rep.status === "FAILED"
                        ? "bg-red-100 text-red-800"
                        : "bg-blue-100 text-blue-800"
                    }`}>
                      {rep.status}
                    </span>
                  </div>
                  <p className="text-xs text-gray-600 mt-0.5">
                    {rep.propertyAddress}
                  </p>
                  <p className="text-[11px] text-gray-400 mt-0.5">
                    Generated: {new Date(rep.createdAt).toLocaleDateString()} • Risk Score: {rep.riskScore != null ? `${rep.riskScore}/100` : "Calculating..."}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  {rep.propertyId && (
                    <button
                      onClick={() => router.push(`/properties/${rep.propertyId}/reports/${rep.id}`)}
                      className="rounded-lg border border-gray-300 px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 transition"
                    >
                      View Report Details →
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* 4. Recently Available Properties */}
      <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-xs">
        <div className="flex items-center justify-between mb-4 border-b border-gray-100 pb-3">
          <div>
            <h2 className="text-lg font-bold text-gray-900">
              Evaluated & Available Properties
            </h2>
            <p className="text-xs text-gray-500">
              Quick access to properties available for due diligence evaluation
            </p>
          </div>
          <Link href="/properties" className="text-xs font-semibold text-blue-600 hover:text-blue-800">
            View All ({properties.length}) →
          </Link>
        </div>

        {loading ? (
          <div className="py-8 text-center text-sm text-gray-500">Loading properties...</div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {properties.map((p) => (
              <div
                key={p.id}
                className="rounded-xl border border-gray-200 p-4 hover:border-blue-300 hover:shadow-xs transition flex flex-col justify-between bg-white"
              >
                <div>
                  <span className="rounded-full bg-gray-100 px-2 py-0.5 text-[10px] font-semibold text-gray-700">
                    {p.propertyType || "Residential"}
                  </span>
                  <h3 className="mt-2 text-sm font-bold text-gray-900 line-clamp-1">
                    {p.address}
                  </h3>
                  <p className="mt-1 text-xs text-gray-500">
                    {[
                      p.bedrooms != null ? `${p.bedrooms} Beds` : null,
                      p.bathrooms != null ? `${p.bathrooms} Baths` : null,
                      p.squareFeet != null ? `${p.squareFeet.toLocaleString()} SqFt` : null,
                      p.yearBuilt != null ? `Built ${p.yearBuilt}` : null,
                    ].filter(Boolean).join(" • ") || "Details available"}
                  </p>
                </div>
                <button
                  onClick={() => router.push(`/properties/${p.id}`)}
                  className="mt-4 w-full rounded-lg bg-gray-50 border border-gray-200 px-3 py-1.5 text-xs font-semibold text-gray-700 hover:bg-blue-50 hover:text-blue-700 hover:border-blue-200 transition"
                >
                  Inspect Property →
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* 5. Quick Access Shortcuts */}
      <section className="grid gap-4 sm:grid-cols-4">
        <div
          onClick={() => router.push("/properties")}
          className="rounded-xl border border-gray-200 bg-white p-4 hover:border-blue-300 hover:shadow-xs transition cursor-pointer"
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100 text-blue-600 mb-2">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <h4 className="text-sm font-bold text-gray-900">Property Search</h4>
          <p className="text-xs text-gray-500 mt-0.5">Explore full registry</p>
        </div>

        <div
          onClick={() => router.push("/reports")}
          className="rounded-xl border border-gray-200 bg-white p-4 hover:border-blue-300 hover:shadow-xs transition cursor-pointer"
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600 mb-2">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h4 className="text-sm font-bold text-gray-900">Report History</h4>
          <p className="text-xs text-gray-500 mt-0.5">PDF & Excel downloads</p>
        </div>

        <div
          onClick={() => router.push("/notifications")}
          className="rounded-xl border border-gray-200 bg-white p-4 hover:border-blue-300 hover:shadow-xs transition cursor-pointer"
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 text-amber-600 mb-2">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
          </div>
          <h4 className="text-sm font-bold text-gray-900">Notifications</h4>
          <p className="text-xs text-gray-500 mt-0.5">Due diligence alerts</p>
        </div>

        <div
          onClick={() => router.push("/profile")}
          className="rounded-xl border border-gray-200 bg-white p-4 hover:border-blue-300 hover:shadow-xs transition cursor-pointer"
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-100 text-emerald-600 mb-2">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
          <h4 className="text-sm font-bold text-gray-900">Buyer Profile</h4>
          <p className="text-xs text-gray-500 mt-0.5">Security & settings</p>
        </div>
      </section>
    </div>
  );
}
