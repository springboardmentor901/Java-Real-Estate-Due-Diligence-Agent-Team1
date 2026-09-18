"use client";

import { useEffect, useState, useMemo, useCallback } from "react";
import { useRouter } from "next/navigation";
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

export default function PropertiesPage() {
  const router = useRouter();

  const [initialProperties, setInitialProperties] = useState<Property[]>([]);
  const [searchResults, setSearchResults] = useState<Property[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState("");
  const [searchError, setSearchError] = useState("");

  const [searchQuery, setSearchQuery] = useState("");
  const [searchedQuery, setSearchedQuery] = useState("");
  const [selectedType, setSelectedType] = useState("ALL");
  const [sortBy, setSortBy] = useState<"address" | "year" | "sqft">("address");

  useEffect(() => {
    async function fetchProperties() {
      const token = getToken();

      if (!token) {
        router.replace("/login");
        return;
      }

      try {
        setLoading(true);
        setError("");

        const data = await apiRequest<Property[]>("/api/properties", {
          token,
        });

        const safeData = Array.isArray(data) ? data : [];
        const unique = Array.from(
          new Map(safeData.filter((p) => p && p.id != null).map((p) => [p.id, p])).values()
        );
        setInitialProperties(unique);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load properties. Please try again."
        );
      } finally {
        setLoading(false);
      }
    }

    fetchProperties();
  }, [router]);

  const executeSearchImmediate = useCallback(
    async (query: string) => {
      const token = getToken();
      if (!token) {
        router.replace("/login");
        return;
      }

      const trimmed = query.trim();
      if (!trimmed) {
        setSearchResults(null);
        setSearchedQuery("");
        setSearchError("");
        setIsSearching(false);
        return;
      }

      try {
        setIsSearching(true);
        setSearchError("");
        const data = await apiRequest<Property[]>(
          `/api/properties/search?address=${encodeURIComponent(trimmed)}`,
          { token }
        );
        const safeData = Array.isArray(data) ? data : [];
        const unique = Array.from(
          new Map(safeData.filter((p) => p && p.id != null).map((p) => [p.id, p])).values()
        );
        setSearchResults(unique);
        setSearchedQuery(trimmed);
      } catch (err) {
        setSearchError("Unable to search properties. Please try again.");
        setSearchResults([]);
        setSearchedQuery(trimmed);
      } finally {
        setIsSearching(false);
      }
    },
    [router]
  );

  useEffect(() => {
    const trimmed = searchQuery.trim();
    if (!trimmed) {
      setSearchResults(null);
      setSearchedQuery("");
      setSearchError("");
      setIsSearching(false);
      return;
    }

    if (trimmed === searchedQuery) {
      return;
    }

    let isCancelled = false;
    const timer = setTimeout(async () => {
      const token = getToken();
      if (!token) {
        router.replace("/login");
        return;
      }

      try {
        setIsSearching(true);
        setSearchError("");
        const data = await apiRequest<Property[]>(
          `/api/properties/search?address=${encodeURIComponent(trimmed)}`,
          { token }
        );
        if (!isCancelled) {
          const safeData = Array.isArray(data) ? data : [];
          const unique = Array.from(
            new Map(safeData.filter((p) => p && p.id != null).map((p) => [p.id, p])).values()
          );
          setSearchResults(unique);
          setSearchedQuery(trimmed);
        }
      } catch (err) {
        if (!isCancelled) {
          setSearchError("Unable to search properties. Please try again.");
          setSearchResults([]);
          setSearchedQuery(trimmed);
        }
      } finally {
        if (!isCancelled) {
          setIsSearching(false);
        }
      }
    }, 400);

    return () => {
      isCancelled = true;
      clearTimeout(timer);
    };
  }, [searchQuery, searchedQuery, router]);

  const handleClearSearch = () => {
    setSearchQuery("");
    setSearchResults(null);
    setSearchedQuery("");
    setSearchError("");
    setIsSearching(false);
  };

  const handleResetAllFilters = () => {
    handleClearSearch();
    setSelectedType("ALL");
  };

  const currentProperties = searchResults !== null ? searchResults : initialProperties;

  const propertyTypes = useMemo(() => {
    const types = new Set<string>();
    currentProperties.forEach((p) => {
      if (p.propertyType) types.add(p.propertyType);
    });
    return Array.from(types);
  }, [currentProperties]);

  const filteredProperties = useMemo(() => {
    const seenIds = new Set<number>();
    return currentProperties
      .filter((property) => {
        if (!property || property.id == null || seenIds.has(property.id)) {
          return false;
        }
        seenIds.add(property.id);

        const matchesType =
          selectedType === "ALL" ||
          property.propertyType?.toLowerCase() === selectedType.toLowerCase();

        return matchesType;
      })
      .sort((a, b) => {
        if (sortBy === "address") {
          return a.address.localeCompare(b.address);
        }
        if (sortBy === "year") {
          return (b.yearBuilt ?? 0) - (a.yearBuilt ?? 0);
        }
        if (sortBy === "sqft") {
          return (b.squareFeet ?? 0) - (a.squareFeet ?? 0);
        }
        return 0;
      });
  }, [currentProperties, selectedType, sortBy]);

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Navbar />

        <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">
              Property Search
            </h1>
            <p className="mt-2 text-gray-600">
              Browse and search registered properties or look up any live address to perform due diligence analysis.
            </p>
          </div>

          {/* Search and Filters */}
          <div className="mb-8 rounded-xl border border-gray-200 bg-white p-5 shadow-xs">
            <div className="grid gap-4 md:grid-cols-12">
              {/* Search Bar */}
              <div className="md:col-span-6">
                <label
                  htmlFor="property-search"
                  className="mb-1.5 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                >
                  Address Search
                </label>
                <div className="relative">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                    {isSearching ? (
                      <div className="h-4 w-4 animate-spin rounded-full border-2 border-blue-600 border-t-transparent" />
                    ) : (
                      <svg
                        className="h-4 w-4 text-gray-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="2"
                          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                        />
                      </svg>
                    )}
                  </div>
                  <input
                    id="property-search"
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault();
                        if (searchQuery.trim()) {
                          executeSearchImmediate(searchQuery.trim());
                        }
                      }
                    }}
                    placeholder="Search by street name, city, or zip code..."
                    className="w-full rounded-lg border border-gray-300 py-2.5 pl-10 pr-14 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                  />
                  {searchQuery && (
                    <button
                      type="button"
                      onClick={handleClearSearch}
                      className="absolute inset-y-0 right-0 flex items-center pr-3 text-xs font-medium text-gray-400 hover:text-gray-600"
                    >
                      Clear
                    </button>
                  )}
                </div>
              </div>

              {/* Property Type Filter */}
              <div className="md:col-span-3">
                <label
                  htmlFor="type-filter"
                  className="mb-1.5 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                >
                  Property Type
                </label>
                <select
                  id="type-filter"
                  value={selectedType}
                  onChange={(e) => setSelectedType(e.target.value)}
                  className="w-full rounded-lg border border-gray-300 py-2.5 px-3 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                >
                  <option value="ALL">All Property Types</option>
                  {propertyTypes.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </div>

              {/* Sort By */}
              <div className="md:col-span-3">
                <label
                  htmlFor="sort-filter"
                  className="mb-1.5 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                >
                  Sort By
                </label>
                <select
                  id="sort-filter"
                  value={sortBy}
                  onChange={(e) =>
                    setSortBy(e.target.value as "address" | "year" | "sqft")
                  }
                  className="w-full rounded-lg border border-gray-300 py-2.5 px-3 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                >
                  <option value="address">Address (A-Z)</option>
                  <option value="year">Year Built (Newest)</option>
                  <option value="sqft">Square Feet (Largest)</option>
                </select>
              </div>
            </div>

            {/* Active search query status banner */}
            {searchResults !== null && (
              <div className="mt-4 flex items-center justify-between border-t border-gray-100 pt-3 text-xs text-gray-500">
                <span>
                  Showing live search results for{" "}
                  <strong className="text-gray-800">&quot;{searchedQuery}&quot;</strong>{" "}
                  ({filteredProperties.length}{" "}
                  {filteredProperties.length === 1 ? "match" : "matches"})
                </span>
                <button
                  type="button"
                  onClick={handleClearSearch}
                  className="text-blue-600 hover:text-blue-800 font-medium"
                >
                  Reset to all properties
                </button>
              </div>
            )}
          </div>

          {/* Content States */}
          {loading ? (
            <div className="flex min-h-[300px] flex-col items-center justify-center rounded-xl border border-gray-200 bg-white p-12">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
              <p className="mt-4 text-sm text-gray-600">Loading properties...</p>
            </div>
          ) : isSearching ? (
            <div className="flex min-h-[300px] flex-col items-center justify-center rounded-xl border border-gray-200 bg-white p-12">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
              <p className="mt-4 text-sm font-medium text-gray-700">Searching properties...</p>
              <p className="mt-1 text-xs text-gray-500">Looking up matching addresses and properties...</p>
            </div>
          ) : searchError ? (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-700">
              <h3 className="text-lg font-semibold">Search Failed</h3>
              <p className="mt-1 text-sm">{searchError}</p>
              <div className="mt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => executeSearchImmediate(searchQuery)}
                  className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
                >
                  Retry Search
                </button>
                <button
                  type="button"
                  onClick={handleClearSearch}
                  className="rounded-lg border border-red-300 bg-white px-4 py-2 text-sm font-medium text-red-700 hover:bg-red-50"
                >
                  Clear Search
                </button>
              </div>
            </div>
          ) : error ? (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-700">
              <h3 className="text-lg font-semibold">Error Loading Properties</h3>
              <p className="mt-1 text-sm">{error}</p>
              <button
                type="button"
                onClick={() => window.location.reload()}
                className="mt-4 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
              >
                Retry
              </button>
            </div>
          ) : filteredProperties.length === 0 ? (
            <div className="flex min-h-[300px] flex-col items-center justify-center rounded-xl border border-dashed border-gray-300 bg-white p-12 text-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400 mb-4">
                <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                </svg>
              </div>
              <h3 className="text-base font-semibold text-gray-900">
                {searchedQuery
                  ? `No properties found for "${searchedQuery}"`
                  : "No properties found"}
              </h3>
              <p className="mt-1 text-sm text-gray-500 max-w-sm">
                {searchedQuery
                  ? "We couldn't locate any matching properties for this address query. Try checking the spelling or searching a city or street name."
                  : selectedType !== "ALL"
                  ? "No properties match your current property type filter."
                  : "There are currently no properties registered in the platform."}
              </p>
              {(searchQuery || selectedType !== "ALL") && (
                <button
                  type="button"
                  onClick={handleResetAllFilters}
                  className="mt-4 rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Clear Filters
                </button>
              )}
            </div>
          ) : (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {filteredProperties.map((property) => (
                <div
                  key={`property-${property.id}`}
                  className="flex flex-col justify-between rounded-xl border border-gray-200 bg-white p-6 shadow-xs hover:shadow-md transition-shadow"
                >
                  <div>
                    <div className="flex items-start justify-between gap-2">
                      <span className="inline-flex rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-700">
                        {property.propertyType || "N/A"}
                      </span>
                      <span className="text-xs text-gray-400">
                        ID: {property.id}
                      </span>
                    </div>

                    <h2 className="mt-3 text-lg font-bold text-gray-900 line-clamp-2">
                      {property.address}
                    </h2>

                    <div className="mt-4 grid grid-cols-2 gap-3 border-t border-gray-100 pt-4 text-xs text-gray-600">
                      <div>
                        <span className="text-gray-400">Bedrooms:</span>{" "}
                        <span className="font-semibold text-gray-800">
                          {property.bedrooms ?? "—"}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-400">Bathrooms:</span>{" "}
                        <span className="font-semibold text-gray-800">
                          {property.bathrooms ?? "—"}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-400">Square Feet:</span>{" "}
                        <span className="font-semibold text-gray-800">
                          {property.squareFeet != null
                            ? property.squareFeet.toLocaleString()
                            : "—"}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-400">Year Built:</span>{" "}
                        <span className="font-semibold text-gray-800">
                          {property.yearBuilt ?? "—"}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="mt-6 flex gap-2 border-t border-gray-100 pt-4">
                    <button
                      type="button"
                      onClick={() => router.push(`/properties/${property.id}`)}
                      className="flex-1 rounded-lg bg-blue-600 px-3 py-2 text-center text-xs font-semibold text-white hover:bg-blue-700 transition-colors"
                    >
                      View Details
                    </button>
                    <button
                      type="button"
                      onClick={() =>
                        router.push(`/properties/${property.id}/due-diligence`)
                      }
                      className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-center text-xs font-semibold text-gray-700 hover:bg-gray-50 transition-colors"
                    >
                      Due Diligence
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </main>
      </div>
    </AuthGuard>
  );
}