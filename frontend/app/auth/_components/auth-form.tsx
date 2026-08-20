"use client";

import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import { loginUser, registerUser } from "../_lib/auth-api";

type AuthFormProps = {
  mode: "login" | "register";
};

const roleOptions = [
  "BUYER",
  "REAL_ESTATE_AGENT",
  "LEGAL_REVIEWER",
  "FINANCIAL_INSTITUTION",
];

const formatRoleLabel = (role: string) =>
  role
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

export function AuthForm({ mode }: AuthFormProps) {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    role: "BUYER",
  });

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSubmitting(true);
    setMessage("");

    try {
      if (mode === "login") {
        const response = await loginUser({
          email: formData.email,
          password: formData.password,
        });

        localStorage.setItem("authToken", response.token);
        localStorage.setItem("authUser", JSON.stringify(response));
        router.push("/");
        return;
      }

      await registerUser({
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        role: formData.role,
      });

      setMessage("Account created. You can sign in now.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Something went wrong.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <form className="space-y-5" onSubmit={handleSubmit}>
        {mode === "register" && (
          <Field label="Full name" htmlFor="fullName">
            <input
              id="fullName"
              type="text"
              value={formData.fullName}
              onChange={(event) => handleChange("fullName", event.target.value)}
              placeholder="John Smith"
              required
              className="auth-input"
            />
          </Field>
        )}

        <Field label="Email address" htmlFor="email">
          <input
            id="email"
            type="email"
            value={formData.email}
            onChange={(event) => handleChange("email", event.target.value)}
            placeholder="name@example.com"
            required
            className="auth-input"
          />
        </Field>

        <Field label="Password" htmlFor="password">
          <input
            id="password"
            type="password"
            value={formData.password}
            onChange={(event) => handleChange("password", event.target.value)}
            placeholder="Enter your password"
            minLength={mode === "register" ? 8 : undefined}
            required
            className="auth-input"
          />
        </Field>

        {mode === "register" && (
          <Field label="Role" htmlFor="role">
            <select
              id="role"
              value={formData.role}
              onChange={(event) => handleChange("role", event.target.value)}
              required
              className="auth-input"
            >
              {roleOptions.map((role) => (
                <option key={role} value={role}>
                  {formatRoleLabel(role)}
                </option>
              ))}
            </select>
          </Field>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-md bg-slate-950 px-4 py-3.5 text-sm font-semibold text-white transition hover:bg-slate-800 focus:outline-none focus:ring-4 focus:ring-slate-200 disabled:cursor-not-allowed disabled:bg-slate-400"
        >
          {isSubmitting
            ? "Please wait..."
            : mode === "login"
              ? "Sign in"
              : "Create account"}
        </button>
      </form>

      {message && (
        <p className="mt-5 rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
          {message}
        </p>
      )}
    </>
  );
}

function Field({
  children,
  htmlFor,
  label,
}: Readonly<{
  children: React.ReactNode;
  htmlFor: string;
  label: string;
}>) {
  return (
    <div>
      <label htmlFor={htmlFor} className="mb-2 block text-sm font-medium text-slate-700">
        {label}
      </label>
      {children}
    </div>
  );
}
