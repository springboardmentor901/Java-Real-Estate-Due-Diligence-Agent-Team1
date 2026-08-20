"use client";

import { useRouter } from "next/navigation";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import {
  AddressValidation,
  apiFetch,
  getAuthToken,
  Property,
} from "@/app/_lib/api";

type DashboardTab = "properties" | "profile" | "settings";

type AuthUser = {
  id?: number;
  fullName?: string;
  email?: string;
  role?: string;
};

type DashboardSettings = {
  emailUpdates: boolean;
  requireReview: boolean;
  defaultView: DashboardTab;
};

const defaultSettings: DashboardSettings = {
  emailUpdates: true,
  requireReview: true,
  defaultView: "properties",
};

const readStoredUser = (): AuthUser => {
  if (typeof window === "undefined") {
    return {};
  }

  const storedUser = localStorage.getItem("authUser");

  if (!storedUser) {
    return {};
  }

  try {
    return JSON.parse(storedUser) as AuthUser;
  } catch {
    localStorage.removeItem("authUser");
    return {};
  }
};

const readStoredSettings = (): DashboardSettings => {
  if (typeof window === "undefined") {
    return defaultSettings;
  }

  const storedSettings = localStorage.getItem("dashboardSettings");

  if (!storedSettings) {
    return defaultSettings;
  }

  try {
    return { ...defaultSettings, ...(JSON.parse(storedSettings) as Partial<DashboardSettings>) };
  } catch {
    localStorage.removeItem("dashboardSettings");
    return defaultSettings;
  }
};

const formatRoleLabel = (role?: string) =>
  role
    ? role
        .toLowerCase()
        .split("_")
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ")
    : "User";

export default function Home() {
  const router = useRouter();
  const [settings, setSettings] = useState<DashboardSettings>(readStoredSettings);
  const [activeTab, setActiveTab] = useState<DashboardTab>(settings.defaultView);
  const [properties, setProperties] = useState<Property[]>([]);
  const [query, setQuery] = useState("");
  const [address, setAddress] = useState("");
  const [validation, setValidation] = useState<AddressValidation | null>(null);
  const [message, setMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [user] = useState<AuthUser>(readStoredUser);

  const userName = user.fullName ?? "there";
  const roleLabel = formatRoleLabel(user.role);
  const latestProperty = properties[0];

  const dashboardStats = useMemo(
    () => [
      { label: "Saved properties", value: properties.length.toString() },
      {
        label: "Validated address",
        value: validation?.valid ? "Confirmed" : "Pending",
      },
      { label: "Workspace role", value: roleLabel },
    ],
    [properties.length, roleLabel, validation?.valid],
  );

  const loadProperties = useCallback(async (searchTerm = "") => {
    setIsLoading(!searchTerm);
    setIsSearching(Boolean(searchTerm));
    setMessage("");

    try {
      const path = searchTerm
        ? `/api/properties/search?address=${encodeURIComponent(searchTerm)}`
        : "/api/properties";

      setProperties(await apiFetch<Property[]>(path));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load properties.");
    } finally {
      setIsLoading(false);
      setIsSearching(false);
    }
  }, []);

  useEffect(() => {
    if (!getAuthToken()) {
      router.replace("/login");
      return;
    }

    void loadProperties();
  }, [loadProperties, router]);

  const updateSettings = (updates: Partial<DashboardSettings>) => {
    setSettings((current) => {
      const nextSettings = { ...current, ...updates };
      localStorage.setItem("dashboardSettings", JSON.stringify(nextSettings));
      return nextSettings;
    });
  };

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    void loadProperties(query.trim());
  };

  const handleValidateAddress = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsValidating(true);
    setValidation(null);
    setMessage("");

    try {
      setValidation(
        await apiFetch<AddressValidation>("/api/properties/validate_address", {
          method: "POST",
          body: JSON.stringify({ address }),
        }),
      );
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to validate address.");
    } finally {
      setIsValidating(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("authUser");
    router.replace("/login");
  };

  return (
    <main className="min-h-screen bg-[#f4f7fb] text-slate-950">
      <div className="mx-auto flex min-h-screen w-full max-w-7xl flex-col lg:flex-row">
        <aside className="border-b border-slate-200 bg-white px-4 py-4 lg:w-72 lg:border-b-0 lg:border-r lg:px-6 lg:py-8">
          <div className="flex items-center justify-between gap-4 lg:block">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.16em] text-emerald-700">
                Real Estate
              </p>
              <h1 className="mt-2 text-xl font-semibold">User Dashboard</h1>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-800 transition hover:border-slate-400 hover:bg-slate-50"
            >
              Sign out
            </button>
          </div>

          <nav className="mt-5 grid grid-cols-3 gap-2 lg:grid-cols-1">
            <DashboardNavButton
              active={activeTab === "properties"}
              label="Properties"
              onClick={() => setActiveTab("properties")}
            />
            <DashboardNavButton
              active={activeTab === "profile"}
              label="Profile"
              onClick={() => setActiveTab("profile")}
            />
            <DashboardNavButton
              active={activeTab === "settings"}
              label="Settings"
              onClick={() => setActiveTab("settings")}
            />
          </nav>

          <div className="mt-6 hidden rounded-lg border border-slate-200 bg-slate-50 p-4 lg:block">
            <p className="text-sm font-semibold text-slate-950">{userName}</p>
            <p className="mt-1 text-sm text-slate-600">{roleLabel}</p>
          </div>
        </aside>

        <section className="flex-1 px-4 py-6 sm:px-6 lg:px-8">
          <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.16em] text-emerald-700">
                Due diligence workspace
              </p>
              <h2 className="mt-2 text-3xl font-semibold tracking-normal">
                Welcome, {userName}
              </h2>
            </div>
            <button
              type="button"
              onClick={() => setActiveTab("properties")}
              className="w-full rounded-md bg-slate-950 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 sm:w-auto"
            >
              Review properties
            </button>
          </header>

          <section className="mt-6 grid gap-4 md:grid-cols-3">
            {dashboardStats.map((stat) => (
              <div
                key={stat.label}
                className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
              >
                <p className="text-sm font-medium text-slate-500">{stat.label}</p>
                <p className="mt-2 text-2xl font-semibold text-slate-950">
                  {stat.value}
                </p>
              </div>
            ))}
          </section>

          {message && (
            <p className="mt-6 rounded-md border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
              {message}
            </p>
          )}

          {activeTab === "properties" && (
            <PropertiesPanel
              address={address}
              isLoading={isLoading}
              isSearching={isSearching}
              isValidating={isValidating}
              onAddressChange={setAddress}
              onSearch={handleSearch}
              onValidateAddress={handleValidateAddress}
              properties={properties}
              query={query}
              setQuery={setQuery}
              validation={validation}
            />
          )}

          {activeTab === "profile" && (
            <ProfilePanel
              latestProperty={latestProperty}
              roleLabel={roleLabel}
              user={user}
            />
          )}

          {activeTab === "settings" && (
            <SettingsPanel
              settings={settings}
              updateSettings={updateSettings}
            />
          )}
        </section>
      </div>
    </main>
  );
}

function DashboardNavButton({
  active,
  label,
  onClick,
}: Readonly<{
  active: boolean;
  label: string;
  onClick: () => void;
}>) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-md px-3 py-2.5 text-sm font-semibold transition ${
        active
          ? "bg-slate-950 text-white"
          : "bg-slate-100 text-slate-700 hover:bg-slate-200 hover:text-slate-950"
      }`}
    >
      {label}
    </button>
  );
}

function PropertiesPanel({
  address,
  isLoading,
  isSearching,
  isValidating,
  onAddressChange,
  onSearch,
  onValidateAddress,
  properties,
  query,
  setQuery,
  validation,
}: Readonly<{
  address: string;
  isLoading: boolean;
  isSearching: boolean;
  isValidating: boolean;
  onAddressChange: (value: string) => void;
  onSearch: (event: FormEvent<HTMLFormElement>) => void;
  onValidateAddress: (event: FormEvent<HTMLFormElement>) => void;
  properties: Property[];
  query: string;
  setQuery: (value: string) => void;
  validation: AddressValidation | null;
}>) {
  return (
    <section className="mt-8 grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
      <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h3 className="text-xl font-semibold">Properties</h3>
            <p className="mt-1 text-sm text-slate-600">
              Search backend records by address.
            </p>
          </div>
          <form className="flex flex-col gap-2 sm:flex-row" onSubmit={onSearch}>
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search address"
              className="auth-input min-w-0 sm:w-64"
            />
            <button
              type="submit"
              disabled={isSearching}
              className="rounded-md bg-slate-950 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:bg-slate-400"
            >
              {isSearching ? "Searching" : "Search"}
            </button>
          </form>
        </div>

        <div className="mt-5 overflow-hidden rounded-lg border border-slate-200">
          {isLoading ? (
            <p className="bg-slate-50 px-4 py-6 text-sm text-slate-600">
              Loading properties...
            </p>
          ) : properties.length === 0 ? (
            <p className="bg-slate-50 px-4 py-6 text-sm text-slate-600">
              No properties found.
            </p>
          ) : (
            <div className="divide-y divide-slate-200">
              {properties.map((property) => (
                <article key={property.id} className="bg-white p-4">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <h4 className="font-semibold text-slate-950">
                      {property.address}
                    </h4>
                    <span className="w-fit rounded-md bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-800">
                      {property.propertyType ?? "Property"}
                    </span>
                  </div>
                  <dl className="mt-3 grid gap-3 text-sm text-slate-600 sm:grid-cols-5">
                    <PropertyMetric label="Beds" value={property.bedrooms} />
                    <PropertyMetric label="Baths" value={property.bathrooms} />
                    <PropertyMetric label="Sq ft" value={property.squareFeet} />
                    <PropertyMetric label="Built" value={property.yearBuilt} />
                    <PropertyMetric
                      label="Added"
                      value={
                        property.createdAt
                          ? new Date(property.createdAt).toLocaleDateString()
                          : null
                      }
                    />
                  </dl>
                </article>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="text-xl font-semibold">Address Validation</h3>
        <p className="mt-1 text-sm text-slate-600">
          Call the geocoding-backed endpoint with your JWT.
        </p>

        <form className="mt-5 space-y-4" onSubmit={onValidateAddress}>
          <textarea
            value={address}
            onChange={(event) => onAddressChange(event.target.value)}
            placeholder="Enter a property address"
            required
            rows={4}
            className="auth-input resize-none"
          />
          <button
            type="submit"
            disabled={isValidating}
            className="w-full rounded-md bg-emerald-700 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-800 disabled:bg-slate-400"
          >
            {isValidating ? "Validating..." : "Validate address"}
          </button>
        </form>

        {validation && (
          <div className="mt-5 rounded-lg border border-slate-200 bg-slate-50 p-4 text-sm">
            <p className="font-semibold text-slate-950">
              {validation.valid ? "Valid address" : "Address not confirmed"}
            </p>
            <p className="mt-2 text-slate-700">
              {validation.formattedAddress ?? "No formatted address returned."}
            </p>
            {validation.latitude && validation.longitude && (
              <p className="mt-2 text-slate-600">
                {validation.latitude}, {validation.longitude}
              </p>
            )}
          </div>
        )}
      </div>
    </section>
  );
}

function ProfilePanel({
  latestProperty,
  roleLabel,
  user,
}: Readonly<{
  latestProperty?: Property;
  roleLabel: string;
  user: AuthUser;
}>) {
  return (
    <section className="mt-8 grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex h-16 w-16 items-center justify-center rounded-lg bg-emerald-700 text-2xl font-semibold text-white">
          {(user.fullName ?? "U").charAt(0).toUpperCase()}
        </div>
        <h3 className="mt-5 text-2xl font-semibold">{user.fullName ?? "User"}</h3>
        <p className="mt-1 text-sm text-slate-600">{roleLabel}</p>

        <dl className="mt-6 grid gap-4 text-sm">
          <ProfileRow label="Email" value={user.email ?? "Not available"} />
          <ProfileRow label="User ID" value={user.id?.toString() ?? "Not available"} />
          <ProfileRow label="Account status" value="Active" />
        </dl>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <h3 className="text-xl font-semibold">Profile Activity</h3>
        <div className="mt-5 grid gap-4">
          <ActivityItem
            title="Property access"
            description={
              latestProperty
                ? `Most recent record loaded: ${latestProperty.address}`
                : "No property records have been loaded yet."
            }
          />
          <ActivityItem
            title="Due diligence role"
            description={`Your dashboard is tailored for ${roleLabel.toLowerCase()} tasks.`}
          />
          <ActivityItem
            title="Security"
            description="Your session uses the stored JWT from the backend login response."
          />
        </div>
      </div>
    </section>
  );
}

function SettingsPanel({
  settings,
  updateSettings,
}: Readonly<{
  activeTab: DashboardTab;
  settings: DashboardSettings;
  updateSettings: (updates: Partial<DashboardSettings>) => void;
}>) {
  return (
    <section className="mt-8 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div className="max-w-3xl">
        <h3 className="text-xl font-semibold">Settings</h3>
        <p className="mt-1 text-sm text-slate-600">
          Manage your workspace preferences on this device.
        </p>

        <div className="mt-6 divide-y divide-slate-200 rounded-lg border border-slate-200">
          <SettingToggle
            checked={settings.emailUpdates}
            description="Receive status summaries and validation updates."
            label="Email updates"
            onChange={(checked) => updateSettings({ emailUpdates: checked })}
          />
          <SettingToggle
            checked={settings.requireReview}
            description="Keep legal and financial review prompts visible."
            label="Require review reminders"
            onChange={(checked) => updateSettings({ requireReview: checked })}
          />
          <div className="p-4">
            <label
              htmlFor="defaultView"
              className="block text-sm font-semibold text-slate-950"
            >
              Default dashboard view
            </label>
            <select
              id="defaultView"
              value={settings.defaultView}
              onChange={(event) =>
                updateSettings({ defaultView: event.target.value as DashboardTab })
              }
              className="auth-input mt-3 max-w-xs"
            >
              <option value="properties">Properties</option>
              <option value="profile">Profile</option>
              <option value="settings">Settings</option>
            </select>
          </div>
        </div>
      </div>
    </section>
  );
}

function SettingToggle({
  checked,
  description,
  label,
  onChange,
}: Readonly<{
  checked: boolean;
  description: string;
  label: string;
  onChange: (checked: boolean) => void;
}>) {
  return (
    <label className="flex cursor-pointer flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between">
      <span>
        <span className="block text-sm font-semibold text-slate-950">{label}</span>
        <span className="mt-1 block text-sm text-slate-600">{description}</span>
      </span>
      <input
        type="checkbox"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
        className="h-5 w-5 accent-emerald-700"
      />
    </label>
  );
}

function ProfileRow({
  label,
  value,
}: Readonly<{
  label: string;
  value: string;
}>) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-500">
        {label}
      </dt>
      <dd className="mt-1 text-slate-900">{value}</dd>
    </div>
  );
}

function ActivityItem({
  description,
  title,
}: Readonly<{
  description: string;
  title: string;
}>) {
  return (
    <article className="rounded-lg border border-slate-200 bg-slate-50 p-4">
      <h4 className="font-semibold text-slate-950">{title}</h4>
      <p className="mt-2 text-sm leading-6 text-slate-600">{description}</p>
    </article>
  );
}

function PropertyMetric({
  label,
  value,
}: Readonly<{
  label: string;
  value: string | number | null;
}>) {
  return (
    <div>
      <dt className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-500">
        {label}
      </dt>
      <dd className="mt-1 text-slate-900">{value ?? "N/A"}</dd>
    </div>
  );
}
