import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { apiGet, apiJson } from "../lib/api";
import { useAuth } from "../context/AuthContext";

type PerformanceViewDto = {
  id: number;
  festivalId: number;
  name: string;
  genre: string;
  description?: string | null;
  scheduledSlot?: string | null;
  preferredRehearsalTimes?: string | null;
  preferredTimeSlots?: string | null;
  finalSetlist?: string | null;
  finalRehearsalTimes?: string | null;
  finalTimeSlots?: string | null;
  lastReviewScore?: number | null;
  lastReviewComments?: string | null;
  state?: string | null;
};

type CreatePerformancePayload = {
  name: string;
  genre: string;
  description?: string | null;
  durationMinutes: number;
  bandMembers?: string | null;
  technicalRequirements?: string | null;
  setlist?: string | null;
  preferredRehearsalTimes?: string | null;
  preferredTimeSlots?: string | null;
};

function cn(...classes: Array<string | false | undefined | null>) {
  return classes.filter(Boolean).join(" ");
}

function stateLabel(state?: string | null) {
  switch ((state || "").toUpperCase()) {
    case "CREATED":
      return "Draft";
    case "SUBMITTED":
      return "Υποβλήθηκε";
    case "APPROVED":
      return "Εγκρίθηκε (για τελικό)";
    case "REJECTED":
      return "Απορρίφθηκε";
    case "FINAL_SUBMITTED":
      return "Τελική υποβολή";
    case "SCHEDULED":
      return "Προγραμματίστηκε";
    default:
      return state || "Άγνωστο";
  }
}

function stateBadgeClass(state?: string | null) {
  const s = (state || "").toUpperCase();
  if (s === "CREATED") return "bg-gray-100 text-gray-700 border-gray-200";
  if (s === "SUBMITTED") return "bg-blue-50 text-blue-700 border-blue-200";
  if (s === "APPROVED") return "bg-emerald-50 text-emerald-700 border-emerald-200";
  if (s === "REJECTED") return "bg-rose-50 text-rose-700 border-rose-200";
  if (s === "FINAL_SUBMITTED") return "bg-violet-50 text-violet-700 border-violet-200";
  if (s === "SCHEDULED") return "bg-amber-50 text-amber-800 border-amber-200";
  return "bg-gray-100 text-gray-700 border-gray-200";
}

function safeText(v?: string | null, fallback = "—") {
  const t = (v ?? "").trim();
  return t ? t : fallback;
}

function toErrMsg(e: unknown) {
  if (!e) return "Κάτι πήγε στραβά.";
  if (typeof e === "string") return e;
  if (typeof e === "object" && "message" in e && typeof (e as any).message === "string") {
    return (e as any).message;
  }
  if (typeof e === "object" && "status" in e && "message" in e) {
    const anyE = e as any;
    return `${anyE.status} - ${String(anyE.message || "")}`.trim();
  }
  return "Κάτι πήγε στραβά.";
}

export default function SubmissionsPage() {
  const { user, authHeader } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const festivalIdParam = searchParams.get("festivalId");
  const initialFestivalId = festivalIdParam ? Number(festivalIdParam) : undefined;

  const festivalOptions = useMemo(() => {
    const ids = (user?.festivalRoles || [])
      .filter((fr) => String(fr.role || "").toUpperCase() === "SUBMITTER" || String(fr.role || "").toUpperCase() === "ARTIST")
      .map((fr) => fr.festivalId);
    const uniq = Array.from(new Set(ids));
    uniq.sort((a, b) => a - b);
    return uniq;
  }, [user]);

  const [festivalId, setFestivalId] = useState<number | undefined>(
    initialFestivalId ?? festivalOptions[0]
  );

  const [q, setQ] = useState("");
  const [sortBy, setSortBy] = useState<"state" | "name" | "genre">("state");

  const [items, setItems] = useState<PerformanceViewDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [creating, setCreating] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [createErr, setCreateErr] = useState<string | null>(null);
  const [createForm, setCreateForm] = useState<CreatePerformancePayload>({
    name: "",
    genre: "",
    description: "",
    durationMinutes: 60,
    bandMembers: "",
    technicalRequirements: "",
    setlist: "",
    preferredRehearsalTimes: "",
    preferredTimeSlots: "",
  });

  useEffect(() => {
    if (!festivalId && festivalOptions.length) {
      setFestivalId(festivalOptions[0]);
    }
  }, [festivalId, festivalOptions]);

  useEffect(() => {
    if (festivalId) {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        next.set("festivalId", String(festivalId));
        return next;
      });
    }
  }, [festivalId, setSearchParams]);

  async function load() {
    if (!festivalId || !user) return;
    setLoading(true);
    setError(null);
    try {
      const url = `/api/performances/search-view?festivalId=${festivalId}...`
      const data = await apiGet<PerformanceViewDto[]>(url, authHeader);
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(toErrMsg(e));
      setItems([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [festivalId]);

  const filteredSorted = useMemo(() => {
    const needle = q.trim().toLowerCase();
    const filtered = needle
      ? items.filter((p) => {
          const hay = `${p.name || ""} ${p.genre || ""} ${p.description || ""} ${p.state || ""} ${p.scheduledSlot || ""}`.toLowerCase();
          return hay.includes(needle);
        })
      : items.slice();

    const orderState = (s?: string | null) => {
      const t = (s || "").toUpperCase();
      if (t === "CREATED") return 10;
      if (t === "SUBMITTED") return 20;
      if (t === "APPROVED") return 30;
      if (t === "FINAL_SUBMITTED") return 40;
      if (t === "SCHEDULED") return 50;
      if (t === "REJECTED") return 90;
      return 60;
    };

    filtered.sort((a, b) => {
      if (sortBy === "state") {
        const da = orderState(a.state);
        const db = orderState(b.state);
        if (da !== db) return da - db;
        return (a.name || "").localeCompare(b.name || "");
      }
      if (sortBy === "genre") {
        const ga = (a.genre || "").toLowerCase();
        const gb = (b.genre || "").toLowerCase();
        if (ga !== gb) return ga.localeCompare(gb);
        return (a.name || "").localeCompare(b.name || "");
      }
      return (a.name || "").localeCompare(b.name || "");
    });

    return filtered;
  }, [items, q, sortBy]);

  const stats = useMemo(() => {
    const total = items.length;
    const byState = new Map<string, number>();
    for (const it of items) {
      const k = (it.state || "UNKNOWN").toUpperCase();
      byState.set(k, (byState.get(k) || 0) + 1);
    }
    const get = (k: string) => byState.get(k) || 0;
    return {
      total,
      created: get("CREATED"),
      submitted: get("SUBMITTED"),
      approved: get("APPROVED"),
      rejected: get("REJECTED"),
      finalSubmitted: get("FINAL_SUBMITTED"),
      scheduled: get("SCHEDULED"),
    };
  }, [items]);

  async function doSubmit(id: number) {
    if (!user) return;
    setError(null);
    try {
      await apiJson(`/api/performances/${id}/submit`, "POST", undefined, authHeader);
      await load();
    } catch (e) {
      setError(toErrMsg(e));
    }
  }

  async function doWithdraw(id: number) {
    if (!user) return;
    setError(null);
    try {
      await apiJson(`/api/performances/${id}/withdraw`, "POST", undefined, authHeader);
      await load();
    } catch (e) {
      setError(toErrMsg(e));
    }
  }

  async function doCreate() {
    if (!user || !festivalId) return;

    setCreateErr(null);

    const payload: CreatePerformancePayload = {
      name: createForm.name.trim(),
      genre: createForm.genre.trim(),
      durationMinutes: Number(createForm.durationMinutes || 0),
      description: createForm.description?.trim() || "",
      bandMembers: createForm.bandMembers?.trim() || "",
      technicalRequirements: createForm.technicalRequirements?.trim() || "",
      setlist: createForm.setlist?.trim() || "",
      preferredRehearsalTimes: createForm.preferredRehearsalTimes?.trim() || "",
      preferredTimeSlots: createForm.preferredTimeSlots?.trim() || "",
    };

    if (!payload.name) return setCreateErr("Το 'Όνομα' είναι υποχρεωτικό.");
    if (!payload.genre) return setCreateErr("Το 'Είδος' είναι υποχρεωτικό.");
    if (!payload.durationMinutes || payload.durationMinutes <= 0)
      return setCreateErr("Η 'Διάρκεια (λεπτά)' πρέπει να είναι > 0.");

    setCreating(true);
    try {
      await apiJson(
        `/api/performances?festivalId=${festivalId}`,
        "POST",
        payload,
        authHeader
      );
      setCreateOpen(false);
      setCreateForm({
        name: "",
        genre: "",
        description: "",
        durationMinutes: 60,
        bandMembers: "",
        technicalRequirements: "",
        setlist: "",
        preferredRehearsalTimes: "",
        preferredTimeSlots: "",
      });
      await load();
    } catch (e) {
      setCreateErr(toErrMsg(e));
    } finally {
      setCreating(false);
    }
  }

  const canCreate = !!festivalId && !!user;

  return (
    <div className="min-h-[calc(100vh-64px)] bg-gray-50">
      <div className="max-w-6xl mx-auto px-6 py-8">
        {/* Header */}
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="space-y-1">
            <h1 className="text-3xl font-semibold tracking-tight">Οι Υποβολές μου</h1>
            <p className="text-sm text-gray-600">
              Δημιούργησε draft υποβολές, υπέβαλε τες όταν το festival είναι σε κατάσταση{" "}
              <span className="font-medium">SUBMISSION</span> και παρακολούθησε την εξέλιξή τους.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-2 sm:items-center">
            {/* Festival picker */}
            <div className="bg-white border rounded-xl px-3 py-2 shadow-sm flex items-center gap-2">
              <span className="text-xs text-gray-500">Festival</span>
              <select
                className="text-sm outline-none bg-transparent"
                value={festivalId ?? ""}
                onChange={(e) => setFestivalId(Number(e.target.value))}
                disabled={!festivalOptions.length}
                aria-label="Festival επιλογή"
              >
                {!festivalOptions.length ? (
                  <option value="">—</option>
                ) : (
                  festivalOptions.map((id) => (
                    <option key={id} value={id}>
                      #{id}
                    </option>
                  ))
                )}
              </select>
            </div>

            <button
              className={cn(
                "inline-flex items-center justify-center rounded-xl px-4 py-2 text-sm font-medium shadow-sm border",
                "bg-gray-900 text-white border-gray-900 hover:bg-gray-800",
                !canCreate && "opacity-60 cursor-not-allowed"
              )}
              onClick={() => setCreateOpen((v) => !v)}
              disabled={!canCreate}
            >
              Νέα Υποβολή
            </button>
          </div>
        </div>

        {/* Summary */}
        <div className="mt-6 grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="bg-white border rounded-2xl shadow-sm p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-500">Σύνολο</p>
                <p className="text-2xl font-semibold">{stats.total}</p>
              </div>
              <div className="text-xs text-gray-500 text-right">
                <div>Draft: {stats.created}</div>
                <div>Υποβλήθηκαν: {stats.submitted}</div>
              </div>
            </div>
          </div>

          <div className="bg-white border rounded-2xl shadow-sm p-4">
            <p className="text-xs text-gray-500">Αποτελέσματα</p>
            <div className="mt-2 grid grid-cols-3 gap-2 text-sm">
              <div className="rounded-xl border bg-emerald-50 text-emerald-800 px-3 py-2">
                <div className="text-xs opacity-80">Εγκρίθηκαν</div>
                <div className="font-semibold">{stats.approved}</div>
              </div>
              <div className="rounded-xl border bg-violet-50 text-violet-800 px-3 py-2">
                <div className="text-xs opacity-80">Τελικές</div>
                <div className="font-semibold">{stats.finalSubmitted}</div>
              </div>
              <div className="rounded-xl border bg-amber-50 text-amber-900 px-3 py-2">
                <div className="text-xs opacity-80">Scheduled</div>
                <div className="font-semibold">{stats.scheduled}</div>
              </div>
            </div>
          </div>

          <div className="bg-white border rounded-2xl shadow-sm p-4">
            <p className="text-xs text-gray-500">Συμβουλή</p>
            <p className="mt-2 text-sm text-gray-700">
              Αν βλέπεις error στο submit, συνήθως σημαίνει ότι το festival δεν είναι σε{" "}
              <span className="font-medium">SUBMISSION</span> ή ότι η υποβολή δεν είναι σε{" "}
              <span className="font-medium">CREATED</span>.
            </p>
          </div>
        </div>

        {/* Create form */}
        {createOpen && (
          <div className="mt-6 bg-white border rounded-2xl shadow-sm p-5">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 className="text-lg font-semibold">Νέα υποβολή (draft)</h2>
                <p className="text-sm text-gray-600">
                  Θα δημιουργηθεί σε κατάσταση <span className="font-medium">CREATED</span>. Μετά μπορείς να πατήσεις{" "}
                  <span className="font-medium">Υποβολή</span>.
                </p>
              </div>
              <button
                className="rounded-xl border px-3 py-2 text-sm hover:bg-gray-50"
                onClick={() => setCreateOpen(false)}
              >
                Κλείσιμο
              </button>
            </div>

            {createErr && (
              <div className="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
                {createErr}
              </div>
            )}

            <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
              <label className="block">
                <span className="text-xs text-gray-600">Όνομα *</span>
                <input
                  className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200"
                  value={createForm.name}
                  onChange={(e) => setCreateForm((p) => ({ ...p, name: e.target.value }))}
                  placeholder="π.χ. The Midnight Set"
                />
              </label>

              <label className="block">
                <span className="text-xs text-gray-600">Είδος *</span>
                <input
                  className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200"
                  value={createForm.genre}
                  onChange={(e) => setCreateForm((p) => ({ ...p, genre: e.target.value }))}
                  placeholder="π.χ. Rock / Jazz / Theater"
                />
              </label>

              <label className="block">
                <span className="text-xs text-gray-600">Διάρκεια (λεπτά) *</span>
                <input
                  type="number"
                  min={1}
                  className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200"
                  value={createForm.durationMinutes}
                  onChange={(e) =>
                    setCreateForm((p) => ({ ...p, durationMinutes: Number(e.target.value) }))
                  }
                />
              </label>

              <label className="block md:col-span-2">
                <span className="text-xs text-gray-600">Περιγραφή</span>
                <textarea
                  className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200 min-h-[90px]"
                  value={createForm.description || ""}
                  onChange={(e) => setCreateForm((p) => ({ ...p, description: e.target.value }))}
                  placeholder="Σύντομη περιγραφή της παράστασης..."
                />
              </label>

              <label className="block md:col-span-2">
                <span className="text-xs text-gray-600">Τεχνικές απαιτήσεις</span>
                <textarea
                  className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200 min-h-[80px]"
                  value={createForm.technicalRequirements || ""}
                  onChange={(e) =>
                    setCreateForm((p) => ({ ...p, technicalRequirements: e.target.value }))
                  }
                  placeholder="π.χ. μικρόφωνα, κονσόλα, monitors..."
                />
              </label>

              <label className="block">
                <span className="text-xs text-gray-600">Προτιμώμενοι χρόνοι πρόβας</span>
                <input
                  className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200"
                  value={createForm.preferredRehearsalTimes || ""}
                  onChange={(e) =>
                    setCreateForm((p) => ({ ...p, preferredRehearsalTimes: e.target.value }))
                  }
                  placeholder="π.χ. 16:00-18:00"
                />
              </label>

              <label className="block">
                <span className="text-xs text-gray-600">Προτιμώμενα time slots</span>
                <input
                  className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200"
                  value={createForm.preferredTimeSlots || ""}
                  onChange={(e) =>
                    setCreateForm((p) => ({ ...p, preferredTimeSlots: e.target.value }))
                  }
                  placeholder="π.χ. 20:00-22:00"
                />
              </label>
            </div>

            <div className="mt-4 flex flex-col sm:flex-row gap-2 sm:justify-end">
              <button
                className="rounded-xl border px-4 py-2 text-sm hover:bg-gray-50"
                onClick={() => setCreateOpen(false)}
                disabled={creating}
              >
                Άκυρο
              </button>
              <button
                className={cn(
                  "rounded-xl px-4 py-2 text-sm font-medium shadow-sm border",
                  "bg-gray-900 text-white border-gray-900 hover:bg-gray-800",
                  creating && "opacity-70 cursor-wait"
                )}
                onClick={doCreate}
                disabled={creating}
              >
                {creating ? "Δημιουργία..." : "Δημιουργία draft"}
              </button>
            </div>
          </div>
        )}

        {/* Controls */}
        <div className="mt-6 bg-white border rounded-2xl shadow-sm p-4">
          <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
            <div className="flex-1 flex flex-col sm:flex-row gap-2">
              <input
                className="w-full sm:max-w-md rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-gray-200"
                placeholder="Αναζήτηση (τίτλος/είδος/κατάσταση/slot)..."
                value={q}
                onChange={(e) => setQ(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") load();
                }}
              />
              <button
                className="rounded-xl border px-4 py-2 text-sm hover:bg-gray-50"
                onClick={load}
                disabled={loading}
              >
                {loading ? "Φόρτωση..." : "Ανανέωση"}
              </button>
            </div>

            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-500">Ταξινόμηση</span>
              <select
                className="rounded-xl border px-3 py-2 text-sm outline-none bg-white"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as any)}
              >
                <option value="state">Κατάσταση</option>
                <option value="name">Τίτλος</option>
                <option value="genre">Είδος</option>
              </select>
            </div>
          </div>

          {error && (
            <div className="mt-3 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
              {error}
            </div>
          )}
        </div>

        {/* List */}
        <div className="mt-6">
          {loading ? (
            <div className="bg-white border rounded-2xl shadow-sm p-6 text-sm text-gray-600">
              Φόρτωση υποβολών...
            </div>
          ) : filteredSorted.length === 0 ? (
            <div className="bg-white border rounded-2xl shadow-sm p-6">
              <h3 className="text-lg font-semibold">Δεν βρέθηκαν υποβολές</h3>
              <p className="mt-1 text-sm text-gray-600">
                Δοκίμασε να αλλάξεις festival ή να δημιουργήσεις νέα υποβολή.
              </p>
              <div className="mt-4 flex gap-2">
                <button
                  className="rounded-xl border px-4 py-2 text-sm hover:bg-gray-50"
                  onClick={load}
                >
                  Ανανέωση
                </button>
                <button
                  className={cn(
                    "rounded-xl px-4 py-2 text-sm font-medium shadow-sm border",
                    "bg-gray-900 text-white border-gray-900 hover:bg-gray-800",
                    !canCreate && "opacity-60 cursor-not-allowed"
                  )}
                  onClick={() => setCreateOpen(true)}
                  disabled={!canCreate}
                >
                  Νέα Υποβολή
                </button>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              {filteredSorted.map((p) => {
                const st = (p.state || "").toUpperCase();
                const canSubmit = st === "CREATED";
                const canWithdraw = st === "CREATED";

                return (
                  <div key={p.id} className="bg-white border rounded-2xl shadow-sm p-5">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <h3 className="text-lg font-semibold truncate">{safeText(p.name)}</h3>
                          <span
                            className={cn(
                              "inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-medium",
                              stateBadgeClass(p.state)
                            )}
                            title={p.state || ""}
                          >
                            {stateLabel(p.state)}
                          </span>
                        </div>

                        <p className="mt-1 text-sm text-gray-600">
                          <span className="font-medium">Είδος:</span> {safeText(p.genre)}
                        </p>

                        <p className="mt-2 text-sm text-gray-700">
                          {safeText(p.description, "Χωρίς περιγραφή.")}
                        </p>
                      </div>

                      <div className="text-right shrink-0">
                        <div className="text-xs text-gray-500">ID</div>
                        <div className="text-sm font-semibold">#{p.id}</div>
                      </div>
                    </div>

                    <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div className="rounded-xl border bg-gray-50 px-3 py-2">
                        <div className="text-xs text-gray-500">Scheduled slot</div>
                        <div className="text-sm font-medium">{safeText(p.scheduledSlot)}</div>
                      </div>
                      <div className="rounded-xl border bg-gray-50 px-3 py-2">
                        <div className="text-xs text-gray-500">Προτιμήσεις slot</div>
                        <div className="text-sm font-medium">{safeText(p.preferredTimeSlots)}</div>
                      </div>
                    </div>

                    <div className="mt-4 flex flex-col sm:flex-row gap-2 sm:items-center sm:justify-between">
                      <div className="text-xs text-gray-500">
                        Tip: Draft = “CREATED”. Κάνε υποβολή μόνο όταν το festival είναι σε “SUBMISSION”.
                      </div>

                      <div className="flex flex-wrap gap-2 justify-start sm:justify-end">
                        <button
                          className={cn(
                            "rounded-xl border px-4 py-2 text-sm hover:bg-gray-50",
                            !canSubmit && "opacity-50 cursor-not-allowed"
                          )}
                          onClick={() => doSubmit(p.id)}
                          disabled={!canSubmit}
                          title={!canSubmit ? "Υποβολή επιτρέπεται μόνο σε CREATED" : "Υποβολή"}
                        >
                          Υποβολή
                        </button>

                        <button
                          className={cn(
                            "rounded-xl border px-4 py-2 text-sm hover:bg-gray-50",
                            !canWithdraw && "opacity-50 cursor-not-allowed"
                          )}
                          onClick={() => doWithdraw(p.id)}
                          disabled={!canWithdraw}
                          title={!canWithdraw ? "Απόσυρση επιτρέπεται μόνο σε CREATED" : "Απόσυρση"}
                        >
                          Απόσυρση
                        </button>

                        <button
                          className="rounded-xl px-4 py-2 text-sm font-medium shadow-sm border bg-gray-900 text-white border-gray-900 hover:bg-gray-800"
                          onClick={() => navigate(`/screenings?festivalId=${p.festivalId}`)}
                          title="Πήγαινε στις προβολές/παραστάσεις του festival"
                        >
                          Προβολές
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="mt-10 text-center text-xs text-gray-500">
          Festival Manager • My Submissions{" "}
          {festivalId ? (
            <span>
              • Festival <span className="font-medium">#{festivalId}</span>
            </span>
          ) : null}
          <div className="mt-2">
            <Link className="underline hover:text-gray-700" to="/dashboard">
              Επιστροφή στο Dashboard
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
