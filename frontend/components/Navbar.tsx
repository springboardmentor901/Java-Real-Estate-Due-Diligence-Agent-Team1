"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { removeToken, getRole, saveRole, getToken } from "@/lib/auth";
import { apiRequest } from "@/lib/api";

export default function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [role, setRole] = useState<string | null>(null);
  const [unreadCount, setUnreadCount] = useState<number>(0);

  useEffect(() => {
    const cachedRole = getRole();
    if (cachedRole) {
      setRole(cachedRole);
    } else {
      const token = getToken();
      if (token) {
        apiRequest<{ role: string }>("/api/users/profile", { token })
          .then((profile) => {
            if (profile?.role) {
              setRole(profile.role);
              saveRole(profile.role);
            }
          })
          .catch(() => {
            // Ignore if profile load fails
          });
      }
    }
  }, []);

  const fetchUnreadCount = useCallback(() => {
    const token = getToken();
    if (!token) {
      setUnreadCount(0);
      return;
    }

    apiRequest<{ unreadCount: number }>("/api/notifications/unread-count", {
      token,
    })
      .then((res) => {
        if (res && typeof res.unreadCount === "number") {
          setUnreadCount(res.unreadCount);
        }
      })
      .catch(() => {
        // Fallback to /api/notifications
        apiRequest<Array<{ isRead: boolean }>>("/api/notifications", {
          token,
        })
          .then((list) => {
            if (Array.isArray(list)) {
              setUnreadCount(list.filter((item) => !item.isRead).length);
            }
          })
          .catch(() => {});
      });
  }, []);

  useEffect(() => {
    fetchUnreadCount();

    // Periodic polling every 45s
    const interval = setInterval(fetchUnreadCount, 45000);

    const handleUpdate = () => fetchUnreadCount();
    window.addEventListener("notifications-updated", handleUpdate);
    window.addEventListener("focus", handleUpdate);

    return () => {
      clearInterval(interval);
      window.removeEventListener("notifications-updated", handleUpdate);
      window.removeEventListener("focus", handleUpdate);
    };
  }, [fetchUnreadCount, pathname]);

  function handleLogout() {
    removeToken();
    router.replace("/login");
  }

  const baseNavLinks = [
    { name: "Dashboard", href: "/dashboard" },
    { name: "Properties", href: "/properties" },
    { name: "Report History", href: "/reports" },
    { name: "Notifications", href: "/notifications" },
    { name: "Profile", href: "/profile" },
  ];

  const navLinks = role === "ADMINISTRATOR"
    ? [
        { name: "Admin Dashboard", href: "/admin/dashboard" },
        ...baseNavLinks,
      ]
    : baseNavLinks;

  function isActive(href: string) {
    if (href === "/dashboard" || href === "/admin/dashboard") {
      return pathname === href;
    }
    return pathname.startsWith(href);
  }

  return (
    <header className="sticky top-0 z-50 border-b border-gray-200 bg-white/95 backdrop-blur shadow-xs">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8 h-16">
        {/* Brand */}
        <div className="flex items-center gap-3">
          <Link href={role === "ADMINISTRATOR" ? "/admin/dashboard" : "/dashboard"} className="flex items-center gap-2 text-gray-900 font-bold text-lg hover:text-blue-600 transition-colors">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white font-bold text-sm shadow-xs">
              DD
            </div>
            <span>Due Diligence Agent</span>
          </Link>
          {role === "ADMINISTRATOR" && (
            <span className="hidden sm:inline-flex rounded-full bg-purple-100 px-2.5 py-0.5 text-xs font-bold text-purple-800 uppercase tracking-wider">
              Admin
            </span>
          )}
        </div>

        {/* Desktop Navigation Links */}
        <nav className="hidden md:flex items-center space-x-1 lg:space-x-2">
          {navLinks.map((link) => {
            const active = isActive(link.href);
            const isNotifications = link.name === "Notifications";

            return (
              <Link
                key={link.href}
                href={link.href}
                className={`flex items-center gap-1.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  active
                    ? "bg-blue-50 text-blue-700 font-semibold"
                    : "text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                }`}
              >
                {isNotifications && (
                  <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                )}
                <span>{link.name}</span>
                {isNotifications && unreadCount > 0 && (
                  <span className="inline-flex items-center justify-center rounded-full bg-blue-600 px-1.5 py-0.2 text-[11px] font-bold text-white leading-none min-w-[18px] h-[18px]">
                    {unreadCount > 99 ? "99+" : unreadCount}
                  </span>
                )}
              </Link>
            );
          })}
        </nav>

        {/* Right Actions */}
        <div className="hidden md:flex items-center gap-3">
          <button
            onClick={handleLogout}
            className="rounded-lg border border-gray-300 px-3.5 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-900 transition-colors"
          >
            Logout
          </button>
        </div>

        {/* Mobile Hamburger Button */}
        <div className="flex md:hidden">
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="rounded-lg p-2 text-gray-600 hover:bg-gray-100 hover:text-gray-900"
            aria-label="Toggle menu"
          >
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              {mobileMenuOpen ? (
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              ) : (
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              )}
            </svg>
          </button>
        </div>
      </div>

      {/* Mobile dropdown */}
      {mobileMenuOpen && (
        <div className="border-b border-gray-200 bg-white px-4 pt-2 pb-4 space-y-1 md:hidden">
          {navLinks.map((link) => {
            const isNotifications = link.name === "Notifications";
            return (
              <Link
                key={link.href}
                href={link.href}
                onClick={() => setMobileMenuOpen(false)}
                className={`flex items-center justify-between rounded-lg px-3 py-2 text-base font-medium ${
                  isActive(link.href)
                    ? "bg-blue-50 text-blue-700"
                    : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                }`}
              >
                <span className="flex items-center gap-2">
                  {isNotifications && (
                    <svg className="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                  )}
                  {link.name}
                </span>
                {isNotifications && unreadCount > 0 && (
                  <span className="inline-flex items-center justify-center rounded-full bg-blue-600 px-2 py-0.5 text-xs font-bold text-white leading-none">
                    {unreadCount > 99 ? "99+" : unreadCount}
                  </span>
                )}
              </Link>
            );
          })}
          <div className="pt-2">
            <button
              onClick={() => {
                setMobileMenuOpen(false);
                handleLogout();
              }}
              className="w-full text-left rounded-lg px-3 py-2 text-base font-medium text-red-600 hover:bg-red-50"
            >
              Logout
            </button>
          </div>
        </div>
      )}
    </header>
  );
}

