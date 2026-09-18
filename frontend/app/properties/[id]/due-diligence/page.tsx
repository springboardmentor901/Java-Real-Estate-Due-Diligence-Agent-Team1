"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { apiRequest } from "@/lib/api";
import { getToken } from "@/lib/auth";
import Navbar from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";

interface SectionResult<T> {
  status: string;
  data: T | null;
  error: string | null;
}

interface OwnershipRecord {
  id?: number;
  ownerName?: string | null;
  ownershipType?: string | null;
  deedType?: string | null;
  recordedDate?: string | null;
}

interface TaxHistory {
  id?: number;
  taxYear?: number | null;
  assessedValue?: number | null;
  taxAmount?: number | null;
  paymentStatus?: string | null;
}

interface ZoningInformation {
  id?: number;
  zoningCode?: string | null;
  zoningDescription?: string | null;
  zoningClassification?: string | null;
  landUse?: string | null;
  zoningCompliance?: boolean | string | null;
  setbackRequirements?: string | null;
  retrievedAt?: string | null;
}

interface FloodZoneData {
  id?: number;
  floodZone?: string | null;
  floodRiskRating?: string | null;
  source?: string | null;
}

interface Permit {
  id?: number;
  permitNumber?: string | null;
  permitType?: string | null;
  status?: string | null;
  issueDate?: string | null;
  description?: string | null;
}

interface EnvironmentalRecord {
  id?: number;
  recordType?: string | null;
  description?: string | null;
  severity?: string | null;
  source?: string | null;
  retrievedAt?: string | null;
  // Fallbacks
  facilityName?: string | null;
  facilityType?: string | null;
  distanceMiles?: number | null;
  status?: string | null;
}


interface UtilityInformation {
  id?: number;
  utilityType?: string | null;
  provider?: string | null;
  status?: string | null;
}

interface DueDiligenceResponse {
  ownership: SectionResult<OwnershipRecord[]>;
  taxHistory: SectionResult<TaxHistory[]>;
  zoning: SectionResult<ZoningInformation>;
  floodZone: SectionResult<FloodZoneData>;
  permits: SectionResult<Permit[]>;
  environmental: SectionResult<EnvironmentalRecord[]>;
  utilities: SectionResult<UtilityInformation[]>;
}

function SectionUnavailable({
  title,
  section,
}: {
  title: string;
  section: SectionResult<unknown> | null | undefined;
}) {
  return (
    <div className="rounded-xl border border-yellow-200 bg-yellow-50 p-5">
      <h3 className="text-lg font-semibold text-gray-900">
        {title}
      </h3>

      <p className="mt-2 text-sm text-yellow-800">
        This section is temporarily unavailable.
      </p>

      {section?.error && (
        <p className="mt-2 text-xs text-gray-600">
          {section.error}
        </p>
      )}
    </div>
  );
}

function SectionHeader({
  title,
  status,
}: {
  title: string;
  status?: string;
}) {
  return (
    <div className="mb-4 flex items-center justify-between">
      <h3 className="text-xl font-semibold text-gray-900">
        {title}
      </h3>

      {status && (
        <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-600">
          {status}
        </span>
      )}
    </div>
  );
}

function parseEnvironmentalDescription(description?: string | null) {
  if (!description) return null;
  const parts = description.split(" | ");
  const dict: Record<string, string> = {};
  let hasKeyVal = false;

  for (const part of parts) {
    const idx = part.indexOf(":");
    if (idx !== -1) {
      const k = part.substring(0, idx).trim();
      const v = part.substring(idx + 1).trim();
      if (k && v) {
        dict[k] = v;
        hasKeyVal = true;
      }
    }
  }

  if (!hasKeyVal) {
    return { rawText: description };
  }

  return {
    facilityName: dict["Facility"],
    address: dict["Address"],
    city: dict["City"],
    state: dict["State"],
    zip: dict["ZIP"],
    registryId: dict["EPA Registry ID"] || dict["Registry ID"],
    program: dict["Program"],
    programId: dict["Program ID"],
    programFacility: dict["Program Facility"],
    rawText: undefined,
  };
}

function getSeverityBadge(severity?: string | null) {
  switch (severity?.toUpperCase()) {
    case "HIGH":
      return "bg-red-50 text-red-700 border-red-200";
    case "MEDIUM":
      return "bg-amber-50 text-amber-700 border-amber-200";
    case "LOW":
      return "bg-green-50 text-green-700 border-green-200";
    case "INFO":
    default:
      return "bg-blue-50 text-blue-700 border-blue-200";
  }
}

function formatDate(dateStr?: string | null) {
  if (!dateStr) return null;
  const d = new Date(dateStr);
  if (Number.isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export default function DueDiligencePage() {
  const router = useRouter();
  const params = useParams();

  const propertyId = params.id as string;

  const [data, setData] =
    useState<DueDiligenceResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [envPage, setEnvPage] = useState(1);
  const ENV_PAGE_SIZE = 20;

  useEffect(() => {
    async function loadDueDiligence() {
      const token = getToken();

      if (!token) {
        router.replace("/login");
        return;
      }

      if (!propertyId) {
        setError("Property ID is missing.");
        setLoading(false);
        return;
      }

      try {
        const response =
          await apiRequest<DueDiligenceResponse>(
            `/api/properties/${propertyId}/due-diligence`,
            {
              token,
            },
          );

        setData(response);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Unable to load due diligence data.",
        );
      } finally {
        setLoading(false);
      }
    }

    loadDueDiligence();
  }, [propertyId, router]);

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-gray-100">
        <p className="text-gray-600">
          Loading due diligence data...
        </p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="min-h-screen bg-gray-100">
        <div className="mx-auto max-w-4xl px-6 py-12">
          <div className="rounded-xl bg-red-50 p-6 text-red-700">
            <h1 className="text-lg font-semibold">
              Unable to load due diligence
            </h1>

            <p className="mt-2">{error}</p>

            <button
              onClick={() =>
                router.push(`/properties/${propertyId}`)
              }
              className="mt-4 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
            >
              Back to Property
            </button>
          </div>
        </div>
      </main>
    );
  }

  if (!data) {
    return null;
  }

  const ownershipAvailable =
    data.ownership?.status === "SUCCESS" &&
    data.ownership?.data;

  const taxAvailable =
    data.taxHistory?.status === "SUCCESS" &&
    data.taxHistory?.data;

  const zoningAvailable =
    data.zoning?.status === "SUCCESS" &&
    data.zoning?.data;

  const floodAvailable =
    data.floodZone?.status === "SUCCESS" &&
    data.floodZone?.data;

  const permitsAvailable =
    data.permits?.status === "SUCCESS" &&
    data.permits?.data;

  const environmentalAvailable =
    data.environmental?.status === "SUCCESS";

  const utilitiesAvailable =
    data.utilities?.status === "SUCCESS" &&
    data.utilities?.data;

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-100">
        <Navbar />

        {/* Header */}
        <header className="border-b bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              Due Diligence
            </h1>

            <p className="text-sm text-gray-500">
              Property ID: {propertyId}
            </p>
          </div>

          <button
            onClick={() =>
              router.push(`/properties/${propertyId}`)
            }
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Back to Property
          </button>
        </div>
      </header>

      {/* Content */}
      <section className="mx-auto max-w-7xl px-6 py-8">

        <div className="mb-8">
          <h2 className="text-3xl font-bold text-gray-900">
            Due Diligence Analysis
          </h2>

          <p className="mt-2 text-gray-600">
            Review available property information and risk data.
          </p>
        </div>

        {/* Ownership */}
        <section className="mb-6 rounded-xl bg-white p-6 shadow-sm">
          {ownershipAvailable ? (
            <>
              <SectionHeader
                title="Ownership"
                status={data.ownership.status}
              />

              {!data.ownership.data ||
              data.ownership.data.length === 0 ? (
                <p className="text-sm text-gray-500">
                  No ownership records available.
                </p>
              ) : (
                <div className="space-y-3">
                  {data.ownership.data?.map(
                    (record, index) => (
                      <div
                        key={record.id ?? index}
                        className="rounded-lg border p-4"
                      >
                        {record.ownerName && (
                          <p>
                            <span className="font-medium">
                              Owner:
                            </span>{" "}
                            {record.ownerName}
                          </p>
                        )}

                        {record.ownershipType && (
                          <p className="mt-1 text-sm text-gray-600">
                            Ownership Type:{" "}
                            {record.ownershipType}
                          </p>
                        )}

                        {record.deedType && (
                          <p className="mt-1 text-sm text-gray-600">
                            Deed Type: {record.deedType}
                          </p>
                        )}

                        {record.recordedDate && (
                          <p className="mt-1 text-sm text-gray-600">
                            Recorded:{" "}
                            {record.recordedDate}
                          </p>
                        )}
                      </div>
                    ),
                  )}
                </div>
              )}
            </>
          ) : (
            <SectionUnavailable
              title="Ownership"
              section={data.ownership}
            />
          )}
        </section>

        {/* Tax History */}
        <section className="mb-6 rounded-xl bg-white p-6 shadow-sm">
          {taxAvailable ? (
            <>
              <SectionHeader
                title="Tax History"
                status={data.taxHistory.status}
              />

              {!data.taxHistory.data ||
              data.taxHistory.data.length === 0 ? (
                <p className="text-sm text-gray-500">
                  No tax history available.
                </p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-sm">
                    <thead>
                      <tr className="border-b">
                        <th className="px-3 py-3">
                          Year
                        </th>

                        <th className="px-3 py-3">
                          Assessed Value
                        </th>

                        <th className="px-3 py-3">
                          Tax Amount
                        </th>

                        <th className="px-3 py-3">
                          Payment Status
                        </th>
                      </tr>
                    </thead>

                    <tbody>
                      {data.taxHistory.data?.map(
                        (tax, index) => (
                          <tr
                            key={tax.id ?? index}
                            className="border-b last:border-0"
                          >
                            <td className="px-3 py-3">
                              {tax.taxYear ?? "—"}
                            </td>

                            <td className="px-3 py-3">
                              {tax.assessedValue != null
                                ? tax.assessedValue.toLocaleString()
                                : "—"}
                            </td>

                            <td className="px-3 py-3">
                              {tax.taxAmount != null
                                ? tax.taxAmount.toLocaleString()
                                : "—"}
                            </td>

                            <td className="px-3 py-3">
                              {tax.paymentStatus ?? "—"}
                            </td>
                          </tr>
                        ),
                      )}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          ) : (
            <SectionUnavailable
              title="Tax History"
              section={data.taxHistory}
            />
          )}
        </section>

        {/* Zoning */}
        <section className="mb-6 rounded-xl bg-white p-6 shadow-sm">
          {zoningAvailable ? (
            <>
              <SectionHeader
                title="Zoning"
                status={data.zoning.status}
              />

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <p className="text-sm text-gray-500">
                    Zoning Code
                  </p>

                  <p className="mt-1 font-medium">
                    {data.zoning.data?.zoningCode?.trim() || "Not available"}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-gray-500">
                    Zoning Description
                  </p>

                  <p className="mt-1 font-medium">
                    {data.zoning.data?.zoningDescription?.trim() ||
                      "Not available"}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-gray-500">
                    Zoning Classification
                  </p>

                  <p className="mt-1 font-medium">
                    {data.zoning.data?.zoningClassification?.trim() ||
                      "Not available"}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-gray-500">
                    Land Use
                  </p>

                  <p className="mt-1 font-medium">
                    {data.zoning.data?.landUse?.trim() || "Not available"}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-gray-500">
                    Compliance
                  </p>

                  <p className="mt-1 font-medium">
                    {data.zoning.data?.zoningCompliance === true ? (
                      <span className="inline-flex items-center rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-semibold text-green-800">
                        Compliant
                      </span>
                    ) : data.zoning.data?.zoningCompliance === false ? (
                      <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-semibold text-red-800">
                        Non-Compliant
                      </span>
                    ) : typeof data.zoning.data?.zoningCompliance === "string" &&
                      data.zoning.data.zoningCompliance.trim() !== "" ? (
                      data.zoning.data.zoningCompliance.trim()
                    ) : (
                      "Not available"
                    )}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-gray-500">
                    Setback Requirements
                  </p>

                  <p className="mt-1 font-medium">
                    {data.zoning.data?.setbackRequirements?.trim() ||
                      "Not available"}
                  </p>
                </div>
              </div>
            </>
          ) : (
            <SectionUnavailable
              title="Zoning"
              section={data.zoning}
            />
          )}
        </section>

        {/* Flood Zone */}
<section className="mb-6 rounded-xl bg-white p-6 shadow-sm">
  {floodAvailable ? (
    <>
      <SectionHeader
        title="Flood Zone"
        status={data.floodZone.status}
      />

      <div className="grid gap-4 sm:grid-cols-3">
        <div>
          <p className="text-sm text-gray-500">
            Flood Zone
          </p>

          <p className="mt-1 font-medium">
            {data.floodZone.data?.floodZone ?? "—"}
          </p>
        </div>

        <div>
          <p className="text-sm text-gray-500">
            Risk Rating
          </p>

          <p className="mt-1 font-medium">
            {data.floodZone.data?.floodRiskRating ?? "—"}
          </p>
        </div>

        <div>
          <p className="text-sm text-gray-500">
            Source
          </p>

          <p className="mt-1 font-medium">
            {data.floodZone.data?.source ?? "—"}
          </p>
        </div>
      </div>
    </>
  ) : (
    <SectionUnavailable
      title="Flood Zone"
      section={data.floodZone}
    />
  )}
</section>
        {/* Permits */}
        <section className="mb-6 rounded-xl bg-white p-6 shadow-sm">
          {permitsAvailable ? (
            <>
              <SectionHeader
                title="Permits"
                status={data.permits.status}
              />

              {!data.permits.data ||
              data.permits.data.length === 0 ? (
                <p className="text-sm text-gray-500">
                  No permit records available.
                </p>
              ) : (
                <div className="space-y-3">
                  {data.permits.data?.map(
                    (permit, index) => (
                      <div
                        key={permit.id ?? index}
                        className="rounded-lg border p-4"
                      >
                        {permit.permitNumber && (
                          <p>
                            <span className="font-medium">
                              Permit:
                            </span>{" "}
                            {permit.permitNumber}
                          </p>
                        )}

                        {permit.permitType && (
                          <p className="mt-1 text-sm text-gray-600">
                            Type: {permit.permitType}
                          </p>
                        )}

                        {permit.status && (
                          <p className="mt-1 text-sm text-gray-600">
                            Status: {permit.status}
                          </p>
                        )}

                        {permit.issueDate && (
                          <p className="mt-1 text-sm text-gray-600">
                            Issue Date:{" "}
                            {permit.issueDate}
                          </p>
                        )}

                        {permit.description && (
                          <p className="mt-1 text-sm text-gray-600">
                            {permit.description}
                          </p>
                        )}
                      </div>
                    ),
                  )}
                </div>
              )}
            </>
          ) : (
            <SectionUnavailable
              title="Permits"
              section={data.permits}
            />
          )}
        </section>

        {/* Environmental */}
        <section className="mb-6 rounded-xl bg-white p-6 shadow-sm">
          {environmentalAvailable ? (
            <>
              <SectionHeader
                title="Environmental"
                status={data.environmental.status}
              />

              {!data.environmental.data ||
              data.environmental.data.length === 0 ? (
                <div className="rounded-lg border border-dashed border-gray-300 bg-gray-50/50 p-6 text-center">
                  <p className="text-sm font-medium text-gray-500">
                    No environmental records available.
                  </p>
                </div>
              ) : (
                <>
                  <div className="mb-4 flex flex-wrap items-center justify-between gap-2 text-sm text-gray-600">
                    <p>
                      Found <span className="font-semibold text-gray-900">{data.environmental.data.length}</span> environmental records
                    </p>
                    {data.environmental.data.length > ENV_PAGE_SIZE && (
                      <p className="text-xs text-gray-500">
                        Showing {(envPage - 1) * ENV_PAGE_SIZE + 1}–{Math.min(envPage * ENV_PAGE_SIZE, data.environmental.data.length)} of {data.environmental.data.length}
                      </p>
                    )}
                  </div>

                  <div className="space-y-3">
                    {data.environmental.data
                      .slice((envPage - 1) * ENV_PAGE_SIZE, envPage * ENV_PAGE_SIZE)
                      .map((record, index) => {
                        const parsed = parseEnvironmentalDescription(record.description);
                        const facilityTitle =
                          parsed?.facilityName ||
                          record.facilityName ||
                          (record.recordType ? `${record.recordType} Record` : `Environmental Record #${record.id ?? (envPage - 1) * ENV_PAGE_SIZE + index + 1}`);

                        const location = [
                          parsed?.address,
                          parsed?.city,
                          parsed?.state,
                          parsed?.zip,
                        ].filter(Boolean).join(", ");

                        return (
                          <div
                            key={record.id ?? index}
                            className="rounded-lg border border-gray-200 bg-white p-4 transition-all hover:border-gray-300 shadow-2xs"
                          >
                            <div className="flex flex-wrap items-start justify-between gap-2">
                              <div className="flex-1 min-w-[200px]">
                                <h4 className="font-semibold text-gray-900 text-base">
                                  {facilityTitle}
                                </h4>
                                {location && (
                                  <p className="mt-0.5 text-xs text-gray-500">
                                    {location}
                                  </p>
                                )}
                              </div>

                              <div className="flex items-center gap-1.5 flex-wrap">
                                {(record.recordType || parsed?.program) && (
                                  <span className="rounded-md bg-blue-50 px-2.5 py-0.5 text-xs font-semibold text-blue-700 border border-blue-200">
                                    {record.recordType || parsed?.program}
                                  </span>
                                )}
                                {record.severity && (
                                  <span className={`rounded-md px-2 py-0.5 text-xs font-bold border ${getSeverityBadge(record.severity)}`}>
                                    {record.severity}
                                  </span>
                                )}
                              </div>
                            </div>

                            {/* Key Record Details */}
                            <div className="mt-3 grid gap-2 sm:grid-cols-2 lg:grid-cols-3 text-xs">
                              {parsed?.registryId && (
                                <div>
                                  <span className="text-gray-500">EPA Registry ID:</span>{" "}
                                  <span className="font-mono text-gray-800 font-medium">{parsed.registryId}</span>
                                </div>
                              )}
                              {parsed?.programId && (
                                <div>
                                  <span className="text-gray-500">Program ID:</span>{" "}
                                  <span className="font-mono text-gray-800 font-medium">{parsed.programId}</span>
                                </div>
                              )}
                              {parsed?.programFacility && parsed.programFacility !== facilityTitle && (
                                <div>
                                  <span className="text-gray-500">Program Facility:</span>{" "}
                                  <span className="text-gray-800 font-medium">{parsed.programFacility}</span>
                                </div>
                              )}
                              {record.source && (
                                <div>
                                  <span className="text-gray-500">Source:</span>{" "}
                                  <span className="text-gray-800 font-medium">{record.source}</span>
                                </div>
                              )}
                              {record.retrievedAt && (
                                <div>
                                  <span className="text-gray-500">Retrieved:</span>{" "}
                                  <span className="text-gray-800 font-medium">{formatDate(record.retrievedAt)}</span>
                                </div>
                              )}
                              {record.distanceMiles != null && (
                                <div>
                                  <span className="text-gray-500">Distance:</span>{" "}
                                  <span className="text-gray-800 font-medium">{record.distanceMiles} miles</span>
                                </div>
                              )}
                              {record.facilityType && (
                                <div>
                                  <span className="text-gray-500">Type:</span>{" "}
                                  <span className="text-gray-800 font-medium">{record.facilityType}</span>
                                </div>
                              )}
                              {record.status && (
                                <div>
                                  <span className="text-gray-500">Status:</span>{" "}
                                  <span className="text-gray-800 font-medium">{record.status}</span>
                                </div>
                              )}
                            </div>

                            {/* Fallback raw text description if not structured */}
                            {parsed?.rawText && (
                              <p className="mt-2.5 text-xs text-gray-600 bg-gray-50 p-2 rounded border border-gray-100">
                                {parsed.rawText}
                              </p>
                            )}
                          </div>
                        );
                      })}
                  </div>

                  {/* Pagination Controls */}
                  {data.environmental.data.length > ENV_PAGE_SIZE && (
                    <div className="mt-5 flex items-center justify-between border-t border-gray-100 pt-4">
                      <button
                        type="button"
                        onClick={() => setEnvPage((prev) => Math.max(1, prev - 1))}
                        disabled={envPage === 1}
                        className="rounded-lg border border-gray-300 px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                      >
                        Previous
                      </button>

                      <span className="text-xs text-gray-600 font-medium">
                        Page {envPage} of {Math.ceil(data.environmental.data.length / ENV_PAGE_SIZE)}
                      </span>

                      <button
                        type="button"
                        onClick={() =>
                          setEnvPage((prev) =>
                            Math.min(
                              Math.ceil((data.environmental?.data?.length || 0) / ENV_PAGE_SIZE),
                              prev + 1,
                            )
                          )
                        }
                        disabled={envPage >= Math.ceil(data.environmental.data.length / ENV_PAGE_SIZE)}
                        className="rounded-lg border border-gray-300 px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                      >
                        Next
                      </button>
                    </div>
                  )}
                </>
              )}
            </>
          ) : (
            <SectionUnavailable
              title="Environmental"
              section={data.environmental}
            />
          )}
        </section>

        {/* Utilities */}
        <section className="mb-6 rounded-xl bg-white p-6 shadow-sm">
          {utilitiesAvailable ? (
            <>
              <SectionHeader
                title="Utilities"
                status={data.utilities.status}
              />

              {!data.utilities.data ||
              data.utilities.data.length === 0 ? (
                <p className="text-sm text-gray-500">
                  No utility information available.
                </p>
              ) : (
                <div className="grid gap-4 sm:grid-cols-2">
                  {data.utilities.data?.map(
                    (utility, index) => (
                      <div
                        key={utility.id ?? index}
                        className="rounded-lg border p-4"
                      >
                        {utility.utilityType && (
                          <p className="font-medium text-gray-900">
                            {utility.utilityType}
                          </p>
                        )}

                        {utility.provider && (
                          <p className="mt-1 text-sm text-gray-600">
                            Provider: {utility.provider}
                          </p>
                        )}

                        {utility.status && (
                          <p className="mt-1 text-sm text-gray-600">
                            Status: {utility.status}
                          </p>
                        )}
                      </div>
                    ),
                  )}
                </div>
              )}
            </>
          ) : (
            <SectionUnavailable
              title="Utilities"
              section={data.utilities}
            />
          )}
        </section>

      </section>
      </div>
    </AuthGuard>
  );
}