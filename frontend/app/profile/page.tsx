"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiRequest } from "@/lib/api";
import { getToken } from "@/lib/auth";
import Navbar from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";

interface UserProfile {
  id: number;
  fullName: string;
  email: string;
  role: string;
  createdAt: string;
}

export default function ProfilePage() {
  const router = useRouter();

  // User Profile Data
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [initialLoading, setInitialLoading] = useState(true);

  // Profile Edit Form State
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState("");
  const [profileError, setProfileError] = useState("");

  // Password Change Form State
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState("");
  const [passwordError, setPasswordError] = useState("");

  useEffect(() => {
    async function fetchProfile() {
      const token = getToken();

      if (!token) {
        router.replace("/login");
        return;
      }

      try {
        setInitialLoading(true);
        const data = await apiRequest<UserProfile>("/api/users/profile", {
          token,
        });

        setProfile(data);
        setFullName(data.fullName || "");
        setEmail(data.email || "");
      } catch (err) {
        setProfileError(
          err instanceof Error ? err.message : "Failed to load user profile."
        );
      } finally {
        setInitialLoading(false);
      }
    }

    fetchProfile();
  }, [router]);

  async function handleUpdateProfile(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setProfileError("");
    setProfileSuccess("");

    if (!fullName.trim()) {
      setProfileError("Full name cannot be blank.");
      return;
    }

    if (!email.trim()) {
      setProfileError("Email cannot be blank.");
      return;
    }

    const token = getToken();
    if (!token) {
      router.replace("/login");
      return;
    }

    try {
      setProfileLoading(true);
      const updated = await apiRequest<UserProfile>("/api/users/profile", {
        method: "PUT",
        token,
        body: JSON.stringify({
          fullName: fullName.trim(),
          email: email.trim(),
        }),
      });

      setProfile(updated);
      setFullName(updated.fullName);
      setEmail(updated.email);
      setProfileSuccess("Profile details updated successfully.");
    } catch (err) {
      setProfileError(
        err instanceof Error ? err.message : "Failed to update profile."
      );
    } finally {
      setProfileLoading(false);
    }
  }

  async function handleChangePassword(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setPasswordError("");
    setPasswordSuccess("");

    if (!currentPassword) {
      setPasswordError("Current password is required.");
      return;
    }

    if (newPassword.length < 8) {
      setPasswordError("New password must be at least 8 characters long.");
      return;
    }

    if (newPassword !== confirmPassword) {
      setPasswordError("New password and confirmation do not match.");
      return;
    }

    if (currentPassword === newPassword) {
      setPasswordError("New password must be different from current password.");
      return;
    }

    const token = getToken();
    if (!token) {
      router.replace("/login");
      return;
    }

    try {
      setPasswordLoading(true);
      await apiRequest<string>("/api/users/profile/password", {
        method: "PUT",
        token,
        body: JSON.stringify({
          currentPassword,
          newPassword,
        }),
      });

      setPasswordSuccess("Password changed successfully.");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setPasswordError(
        err instanceof Error ? err.message : "Failed to change password."
      );
    } finally {
      setPasswordLoading(false);
    }
  }

  const formatDate = (dateStr?: string | null) => {
    if (!dateStr) return "N/A";
    const date = new Date(dateStr);
    if (Number.isNaN(date.getTime())) return dateStr;
    return date.toLocaleDateString(undefined, {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Navbar />

        <main className="mx-auto max-w-5xl px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">
              User Profile
            </h1>
            <p className="mt-1 text-sm text-gray-600">
              Manage your personal information, role details, and security credentials.
            </p>
          </div>

          {initialLoading ? (
            <div className="flex min-h-[300px] flex-col items-center justify-center rounded-xl border border-gray-200 bg-white p-12">
              <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
              <p className="mt-4 text-sm text-gray-600">Loading profile data...</p>
            </div>
          ) : (
            <div className="grid gap-8 md:grid-cols-3">
              {/* Account Overview Card */}
              <div className="md:col-span-1 space-y-6">
                <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs text-center">
                  <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-blue-600 text-white font-bold text-2xl shadow-xs">
                    {profile?.fullName
                      ? profile.fullName
                          .split(" ")
                          .map((n) => n[0])
                          .join("")
                          .substring(0, 2)
                          .toUpperCase()
                      : "U"}
                  </div>

                  <h2 className="mt-4 text-lg font-bold text-gray-900">
                    {profile?.fullName || "User"}
                  </h2>
                  <p className="text-sm text-gray-500 break-all">
                    {profile?.email}
                  </p>

                  <div className="mt-4 inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
                    {profile?.role?.replace(/_/g, " ") || "USER"}
                  </div>

                  <div className="mt-6 border-t border-gray-100 pt-4 text-left text-xs text-gray-500 space-y-2">
                    <div>
                      <span className="font-semibold text-gray-700">User ID:</span> #{profile?.id}
                    </div>
                    <div>
                      <span className="font-semibold text-gray-700">Member Since:</span> {formatDate(profile?.createdAt)}
                    </div>
                  </div>
                </div>
              </div>

              {/* Edit Forms Column */}
              <div className="md:col-span-2 space-y-8">
                {/* Profile Information Form */}
                <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
                  <h2 className="text-lg font-bold text-gray-900 mb-1">
                    Profile Information
                  </h2>
                  <p className="text-xs text-gray-500 mb-5">
                    Update your account details and correspondence email.
                  </p>

                  {profileSuccess && (
                    <div className="mb-4 rounded-lg bg-green-50 p-3 text-sm text-green-800 border border-green-200">
                      {profileSuccess}
                    </div>
                  )}

                  {profileError && (
                    <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-800 border border-red-200">
                      {profileError}
                    </div>
                  )}

                  <form onSubmit={handleUpdateProfile} className="space-y-4">
                    <div>
                      <label
                        htmlFor="fullName"
                        className="mb-1 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                      >
                        Full Name
                      </label>
                      <input
                        id="fullName"
                        type="text"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        required
                        className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                      />
                    </div>

                    <div>
                      <label
                        htmlFor="email"
                        className="mb-1 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                      >
                        Email Address
                      </label>
                      <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                      />
                    </div>

                    <div className="pt-2">
                      <button
                        type="submit"
                        disabled={profileLoading}
                        className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60 transition-colors shadow-xs"
                      >
                        {profileLoading ? "Saving Changes..." : "Save Changes"}
                      </button>
                    </div>
                  </form>
                </div>

                {/* Change Password Form */}
                <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-xs">
                  <h2 className="text-lg font-bold text-gray-900 mb-1">
                    Change Password
                  </h2>
                  <p className="text-xs text-gray-500 mb-5">
                    Ensure your account is using a secure, long password.
                  </p>

                  {passwordSuccess && (
                    <div className="mb-4 rounded-lg bg-green-50 p-3 text-sm text-green-800 border border-green-200">
                      {passwordSuccess}
                    </div>
                  )}

                  {passwordError && (
                    <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-800 border border-red-200">
                      {passwordError}
                    </div>
                  )}

                  <form onSubmit={handleChangePassword} className="space-y-4">
                    <div>
                      <label
                        htmlFor="currentPassword"
                        className="mb-1 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                      >
                        Current Password
                      </label>
                      <input
                        id="currentPassword"
                        type="password"
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                        required
                        placeholder="Enter current password"
                        className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                      />
                    </div>

                    <div>
                      <label
                        htmlFor="newPassword"
                        className="mb-1 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                      >
                        New Password
                      </label>
                      <input
                        id="newPassword"
                        type="password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        required
                        minLength={8}
                        placeholder="At least 8 characters"
                        className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                      />
                    </div>

                    <div>
                      <label
                        htmlFor="confirmPassword"
                        className="mb-1 block text-xs font-semibold text-gray-700 uppercase tracking-wide"
                      >
                        Confirm New Password
                      </label>
                      <input
                        id="confirmPassword"
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                        minLength={8}
                        placeholder="Confirm new password"
                        className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                      />
                    </div>

                    <div className="pt-2">
                      <button
                        type="submit"
                        disabled={passwordLoading}
                        className="rounded-lg bg-gray-900 px-5 py-2.5 text-sm font-semibold text-white hover:bg-black disabled:opacity-60 transition-colors shadow-xs"
                      >
                        {passwordLoading ? "Updating Password..." : "Update Password"}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </AuthGuard>
  );
}
