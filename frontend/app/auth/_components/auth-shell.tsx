import Link from "next/link";
import { AuthForm } from "./auth-form";

type AuthShellProps = {
  mode: "login" | "register";
};

const copy = {
  login: {
    eyebrow: "Welcome back",
    title: "Sign in to your workspace",
    switchText: "Need an account?",
    switchHref: "/register",
    switchAction: "Create one",
  },
  register: {
    eyebrow: "Create account",
    title: "Start your due diligence workflow",
    switchText: "Already registered?",
    switchHref: "/login",
    switchAction: "Sign in",
  },
};

export function AuthShell({ mode }: AuthShellProps) {
  const activeCopy = copy[mode];

  return (
    <main className="min-h-screen bg-[#f4f7fb] px-4 py-6 text-slate-950 sm:px-6 lg:px-8">
      <div className="mx-auto grid min-h-[calc(100vh-3rem)] max-w-6xl overflow-hidden rounded-lg border border-slate-200 bg-white shadow-xl shadow-slate-200/70 lg:grid-cols-[0.95fr_1.05fr]">
        <section className="hidden bg-slate-950 p-10 text-white lg:flex lg:flex-col lg:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-emerald-300">
              Real Estate Due Diligence
            </p>
            <h1 className="mt-8 max-w-md text-4xl font-semibold leading-tight">
              Secure property checks before every major decision.
            </h1>
            <p className="mt-5 max-w-md text-base leading-7 text-slate-300">
              Bring buyers, agents, legal reviewers, and lenders into one
              reliable review flow as the platform grows.
            </p>
          </div>

          <div className="grid gap-4 text-sm text-slate-200">
            <div className="border-l-2 border-emerald-400 pl-4">
              Buyer and investor verification
            </div>
            <div className="border-l-2 border-cyan-400 pl-4">
              Agent and reviewer collaboration
            </div>
            <div className="border-l-2 border-amber-400 pl-4">
              Legal and financial screening
            </div>
          </div>
        </section>

        <section className="flex items-center px-5 py-8 sm:px-8 lg:px-12">
          <div className="mx-auto w-full max-w-md">
            <div className="mb-8 grid grid-cols-2 rounded-lg bg-slate-100 p-1">
              <Link
                href="/login"
                className={`rounded-md px-4 py-2.5 text-center text-sm font-semibold transition ${
                  mode === "login"
                    ? "bg-white text-slate-950 shadow-sm"
                    : "text-slate-600 hover:text-slate-950"
                }`}
              >
                Login
              </Link>
              <Link
                href="/register"
                className={`rounded-md px-4 py-2.5 text-center text-sm font-semibold transition ${
                  mode === "register"
                    ? "bg-white text-slate-950 shadow-sm"
                    : "text-slate-600 hover:text-slate-950"
                }`}
              >
                Register
              </Link>
            </div>

            <div className="mb-8">
              <p className="text-sm font-semibold uppercase tracking-[0.16em] text-emerald-700">
                {activeCopy.eyebrow}
              </p>
              <h2 className="mt-3 text-3xl font-semibold tracking-normal text-slate-950">
                {activeCopy.title}
              </h2>
            </div>

            <AuthForm mode={mode} />

            <p className="mt-6 text-center text-sm text-slate-600">
              {activeCopy.switchText}{" "}
              <Link
                href={activeCopy.switchHref}
                className="font-semibold text-emerald-700 hover:text-emerald-800"
              >
                {activeCopy.switchAction}
              </Link>
            </p>
          </div>
        </section>
      </div>
    </main>
  );
}
