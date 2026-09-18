"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import Navbar from "@/components/Navbar";
import AuthGuard from "@/components/AuthGuard";
import { apiRequest } from "@/lib/api";
import { getToken } from "@/lib/auth";

interface NotificationItem {
  id: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export default function NotificationsPage() {
  const router = useRouter();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState<"ALL" | "UNREAD">("ALL");
  const [markingId, setMarkingId] = useState<number | null>(null);
  const [markingAll, setMarkingAll] = useState(false);

  const fetchNotifications = useCallback(async () => {
    const token = getToken();
    if (!token) {
      router.replace("/login");
      return;
    }

    try {
      setLoading(true);
      setError("");
      const data = await apiRequest<NotificationItem[]>("/api/notifications", {
        token,
      });
      if (Array.isArray(data)) {
        setNotifications(data);
      } else {
        setNotifications([]);
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to load notifications. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }, [router]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const filteredNotifications =
    filter === "UNREAD"
      ? notifications.filter((n) => !n.isRead)
      : notifications;

  async function handleMarkAsRead(id: number) {
    const token = getToken();
    if (!token) return;

    try {
      setMarkingId(id);
      await apiRequest<NotificationItem>(`/api/notifications/${id}/read`, {
        method: "PUT",
        token,
      });

      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );

      // Notify Navbar badge to update
      if (typeof window !== "undefined") {
        window.dispatchEvent(new Event("notifications-updated"));
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to mark notification as read."
      );
    } finally {
      setMarkingId(null);
    }
  }

  async function handleMarkAllAsRead() {
    if (unreadCount === 0 || markingAll) return;

    const token = getToken();
    if (!token) return;

    try {
      setMarkingAll(true);
      await apiRequest<{ message: string }>("/api/notifications/read-all", {
        method: "PUT",
        token,
      });

      setNotifications((prev) =>
        prev.map((n) => ({ ...n, isRead: true }))
      );

      // Notify Navbar badge to update
      if (typeof window !== "undefined") {
        window.dispatchEvent(new Event("notifications-updated"));
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to mark all notifications as read."
      );
    } finally {
      setMarkingAll(false);
    }
  }

  function formatDate(dateStr: string) {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    if (Number.isNaN(date.getTime())) return dateStr;
    return date.toLocaleDateString(undefined, {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  return (
    <AuthGuard>
      <div className="min-h-screen bg-gray-50">
        <Navbar />

        <main className="mx-auto max-w-5xl px-4 sm:px-6 lg:px-8 py-8">
          {/* Header */}
          <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-3xl font-bold text-gray-900">
                  Notifications
                </h1>
                <span
                  className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                    unreadCount > 0
                      ? "bg-blue-100 text-blue-800"
                      : "bg-gray-100 text-gray-700"
                  }`}
                >
                  {unreadCount} Unread
                </span>
              </div>
              <p className="mt-1 text-sm text-gray-600">
                Real-time alerts, report completion updates, and property analysis notices.
              </p>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-2.5">
              <button
                type="button"
                onClick={fetchNotifications}
                disabled={loading}
                aria-label="Refresh notifications"
                className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 bg-white px-3.5 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 transition-colors shadow-2xs"
              >
                <svg
                  className={`h-4 w-4 ${loading ? "animate-spin text-blue-600" : "text-gray-500"}`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                  />
                </svg>
                <span>Refresh</span>
              </button>

              <button
                type="button"
                onClick={handleMarkAllAsRead}
                disabled={unreadCount === 0 || markingAll || loading}
                className="inline-flex items-center gap-1.5 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors shadow-2xs"
              >
                {markingAll ? (
                  <>
                    <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                    </svg>
                    <span>Marking All Read...</span>
                  </>
                ) : (
                  <>
                    <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    <span>Mark All Read</span>
                  </>
                )}
              </button>
            </div>
          </div>

          {/* Filter Tabs */}
          <div className="mb-6 flex border-b border-gray-200">
            <button
              onClick={() => setFilter("ALL")}
              className={`pb-3 px-4 text-sm font-medium border-b-2 transition-colors ${
                filter === "ALL"
                  ? "border-blue-600 text-blue-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              All ({notifications.length})
            </button>
            <button
              onClick={() => setFilter("UNREAD")}
              className={`pb-3 px-4 text-sm font-medium border-b-2 transition-colors ${
                filter === "UNREAD"
                  ? "border-blue-600 text-blue-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              Unread ({unreadCount})
            </button>
          </div>

          {/* Error Banner */}
          {error && (
            <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-800 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <svg className="h-5 w-5 text-red-600 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>{error}</span>
              </div>
              <button
                onClick={fetchNotifications}
                className="font-semibold text-red-800 underline hover:text-red-900 ml-4 shrink-0"
              >
                Retry
              </button>
            </div>
          )}

          {/* Content */}
          {loading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="animate-pulse rounded-xl border border-gray-200 bg-white p-5 shadow-2xs"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-2.5 flex-1">
                      <div className="h-4 w-48 rounded bg-gray-200" />
                      <div className="h-3 w-3/4 rounded bg-gray-100" />
                      <div className="h-3 w-28 rounded bg-gray-100" />
                    </div>
                    <div className="h-8 w-24 rounded bg-gray-200" />
                  </div>
                </div>
              ))}
            </div>
          ) : filteredNotifications.length === 0 ? (
            <div className="flex min-h-[340px] flex-col items-center justify-center rounded-xl border border-dashed border-gray-300 bg-white p-12 text-center shadow-2xs">
              <div className="flex h-14 w-14 items-center justify-center rounded-full bg-blue-50 text-blue-600 mb-4">
                <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="1.75"
                    d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                  />
                </svg>
              </div>
              <h3 className="text-base font-semibold text-gray-900">
                {filter === "UNREAD"
                  ? "All caught up!"
                  : "No notifications yet"}
              </h3>
              <p className="mt-1 text-sm text-gray-500 max-w-sm">
                {filter === "UNREAD"
                  ? "You have no unread notifications at this time."
                  : "Due diligence report completions, analysis notices, and system alerts will appear here."}
              </p>
              {filter === "UNREAD" && notifications.length > 0 && (
                <button
                  onClick={() => setFilter("ALL")}
                  className="mt-4 text-sm font-semibold text-blue-600 hover:text-blue-700"
                >
                  View All Notifications ({notifications.length})
                </button>
              )}
            </div>
          ) : (
            <div className="space-y-3">
              {filteredNotifications.map((notification) => {
                const isUnread = !notification.isRead;
                const isMarking = markingId === notification.id;

                return (
                  <div
                    key={notification.id}
                    className={`group relative rounded-xl border p-5 transition-all shadow-2xs ${
                      isUnread
                        ? "border-blue-200 bg-white ring-1 ring-blue-100/70"
                        : "border-gray-200 bg-gray-50/60 opacity-90"
                    }`}
                  >
                    <div className="flex items-start justify-between gap-4">
                      {/* Left Icon & Text */}
                      <div className="flex items-start gap-3.5 flex-1 min-w-0">
                        {/* Status Indicator Icon */}
                        <div
                          className={`mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg ${
                            isUnread
                              ? "bg-blue-100 text-blue-700"
                              : "bg-gray-200/80 text-gray-500"
                          }`}
                        >
                          {notification.title.toLowerCase().includes("ready") ||
                          notification.title.toLowerCase().includes("success") ? (
                            <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                          ) : notification.title.toLowerCase().includes("failed") ? (
                            <svg className="h-5 w-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                            </svg>
                          ) : (
                            <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                          )}
                        </div>

                        {/* Title and Message */}
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <h3
                              className={`text-base font-semibold ${
                                isUnread ? "text-gray-900" : "text-gray-700"
                              }`}
                            >
                              {notification.title}
                            </h3>
                            {isUnread && (
                              <span className="inline-flex items-center rounded-full bg-blue-100 px-2 py-0.5 text-[10px] font-bold text-blue-800 tracking-wide uppercase">
                                New
                              </span>
                            )}
                          </div>

                          <p className="mt-1 text-sm text-gray-600 leading-relaxed break-words">
                            {notification.message}
                          </p>

                          <div className="mt-2.5 flex items-center gap-1.5 text-xs text-gray-400">
                            <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <span>{formatDate(notification.createdAt)}</span>
                          </div>
                        </div>
                      </div>

                      {/* Right Action */}
                      <div className="shrink-0 flex items-center">
                        {isUnread ? (
                          <button
                            type="button"
                            onClick={() => handleMarkAsRead(notification.id)}
                            disabled={isMarking}
                            className="inline-flex items-center gap-1 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-900 disabled:opacity-50 transition-colors shadow-2xs"
                          >
                            {isMarking ? (
                              <svg className="h-3.5 w-3.5 animate-spin" viewBox="0 0 24 24" fill="none">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                              </svg>
                            ) : (
                              <svg className="h-3.5 w-3.5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                              </svg>
                            )}
                            <span>Mark as read</span>
                          </button>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-xs text-gray-400 font-medium px-2 py-1">
                            <svg className="h-3.5 w-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                            </svg>
                            Read
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </main>
      </div>
    </AuthGuard>
  );
}

