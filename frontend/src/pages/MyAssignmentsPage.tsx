import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiGet, apiJson } from "../lib/api";
import { cn } from "../lib/utils";

import { Button } from "../components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "../components/ui/card";

type Festival = {
  id: number;
  title: string;
  startDate?: string;
  endDate?: string;
  description?: string;
  state: string;
};

type PerformanceView = {
  id: number;
  festivalId: number;

  name?: string;
  genre?: string;
  description?: string;

  scheduledSlot?: string;

  preferredRehearsalTimes?: string;
  preferredTimeSlots?: string;

  finalSetlist?: string;
  finalRehearsalTimes?: string;
  finalTimeSlots?: string;

  lastReviewScore?: number | null;
  lastReviewComments?: string | null;

  state: string;
};

function Badge({
  children,
  variant = "neutral",
  className,
}: {
  children: React.ReactNode;
  variant?: "neutral" | "ok" | "warn" | "bad" | "info";
  className?: string;
}) {
  const base =
    "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium";
  const styles: Record<string, string> = {
    neutral: "border-border bg-background text-foreground",
    ok: "border-emerald-200 bg-emerald-50 text-emerald-700",
    warn: "border-amber-200 bg-amber-50 text-amber-700",
    bad: "border-red-200 bg-red-50 text-red-700",
    info: "border-blue-200 bg-blue-50 text-blue-700",
  };
  return <span className={cn(base, styles[variant], className)}>{children}</span>;
}

function Modal({
  open,
  title,
  description,
  children,
  onClose,
  footer,
}: {
  open: boolean;
  title: string;
  description?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  onClose: () => void;
}) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="absolute inset-0 flex items-center justify-center p-4">
        <div className="w-full max-w-lg rounded-2xl border bg-card shadow-xl">
          <div className="p-5 border-b">
            <div className="flex items-start justify-between gap-4">
              <div>
                <div className="text-base font-semibold">{title}</div>
                {description ? (
                  <div className="mt-1 text-sm text-muted-foreground">{description}</div>
                ) : null}
              </div>
              <Button variant="ghost" onClick={onClose}>
                Κλείσιμο
              </Button>
            </div>
          </div>
          <div className="p-5">{children}</div>
          {footer ? <div className="p-5 pt-0">{footer}</div> : null}
        </div>
      </div>
    </div>
  );
}

function stateBadgeVariant(state?: string): "neutral" | "ok" | "warn" | "bad" | "info" {
  switch ((state || "").toUpperCase()) {
    case "ASSIGNED":
    case "REVIEWED":
      return "info";
    case "APPROVED":
    case "FINAL_SUBMITTED":
    case "SCHEDULED":
      return "ok";
    case "REJECTED":
    case "WITHDRAWN":
      return "bad";
    default:
      return "neutral";
  }
}

function festivalStateVariant(state?: string): "neutral" | "ok" | "warn" | "bad" | "info" {
  switch ((state || "").toUpperCase()) {
    case "CREATED":
    case "SUBMISSION":
    case "ASSIGNMENT":
      return "neutral";
    case "REVIEW":
      return "info";
    case "SCHEDULING":
    case "DECISION":
    case "FINAL_PUBLICATION":
    case "FINAL_SUBMISSION":
      return "warn";
    case "ANNOUNCED":
    case "COMPLETE":
      return "ok";
    default:
      return "neutral";
  }
}

function fmtDate(s?: string) {
  if (!s) return "—";
  const d = new Date(s);
  if (Number.isNaN(d.getTime())) return s;
  return d.toLocaleDateString("el-GR");
}

function errMsg(e: any) {
  if (!e) return "Άγνωστο σφάλμα";
  if (typeof e === "string") return e;
  if (e.message) return e.message;
  if (e.status && e.message) return `${e.status} - ${e.message}`;
  try {
    return JSON.stringify(e);
  } catch {
    return "Σφάλμα";
  }
}

export default function MyAssignmentsPage() {
  const { user, authHeader } = useAuth();
  const userId = user?.userId;

  const [searchParams, setSearchParams] = useSearchParams();
  const festivalIdParam = searchParams.get("festivalId");

  const [festivals, setFestivals] = useState<Festival[]>([]);
  const [festivalId, setFestivalId] = useState<number | null>(
    festivalIdParam ? Number(festivalIdParam) : null
  );
  const festival = useMemo(
    () => festivals.find((f) => f.id === festivalId) || null,
    [festivals, festivalId]
  );

  const [q, setQ] = useState("");
  const [stateFilter, setStateFilter] = useState<string>("ALL");
  const [sort, setSort] = useState<"state" | "name">("state");

  const [items, setItems] = useState<PerformanceView[]>([]);
  const [loadingFestivals, setLoadingFestivals] = useState(true);
  const [loadingItems, setLoadingItems] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [reviewOpen, setReviewOpen] = useState(false);
  const [active, setActive] = useState<PerformanceView | null>(null);
  const [score, setScore] = useState<number>(7);
  const [comments, setComments] = useState<string>("");

  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState("");

  async function loadFestivals() {
    setLoadingFestivals(true);
    setError(null);
    try {
      const data = await apiGet<Festival[]>("/api/festivals", authHeader);
      setFestivals(Array.isArray(data) ? data : []);

      if (!festivalId && Array.isArray(data) && data.length > 0) {
        setFestivalId(data[0].id);
        setSearchParams({ festivalId: String(data[0].id) });
      }
    } catch (e: any) {
      setError(errMsg(e) || "Αποτυχία φόρτωσης festivals.");
    } finally {
      setLoadingFestivals(false);
    }
  }

  async function loadAssignments() {
    if (!festivalId || !userId) return;
    setLoadingItems(true);
    setError(null);
    try {
      const qs = new URLSearchParams();
      qs.set("festivalId", String(festivalId));
      qs.set("userId", String(userId));
      if (q.trim()) qs.set("q", q.trim());

      const data = await apiGet<PerformanceView[]>(
        `/api/performances/search-view?${qs.toString()}`,
        authHeader
      );
      setItems(Array.isArray(data) ? data : []);
    } catch (e: any) {
      setItems([]);
      setError(errMsg(e) || "Αποτυχία φόρτωσης αναθέσεων.");
    } finally {
      setLoadingItems(false);
    }
  }

  useEffect(() => {
    loadFestivals();
  }, []);

  useEffect(() => {
    if (festivalId) {
      setSearchParams({ festivalId: String(festivalId) });
      loadAssignments();
    }
  }, [festivalId]);

  const filtered = useMemo(() => {
    let arr = items.slice();

    if (stateFilter !== "ALL") {
      arr = arr.filter((x) => (x.state || "").toUpperCase() === stateFilter);
    }

    arr.sort((a, b) => {
      if (sort === "state") {
        return (a.state || "").localeCompare(b.state || "");
      }
      return (a.name || "").localeCompare(b.name || "");
    });

    return arr;
  }, [items, sort, stateFilter]);

  const canReviewFestival = (festival?.state || "").toUpperCase() === "REVIEW";

  async function doReview() {
    if (!active || !festivalId || !userId) return;
    const sc = Number(score);
    if (!Number.isFinite(sc) || sc < 0 || sc > 10) {
      setError("Το score πρέπει να είναι μεταξύ 0 και 10.");
      return;
    }
    if (!comments.trim()) {
      setError("Τα σχόλια είναι υποχρεωτικά.");
      return;
    }
    try {
      setError(null);
      await apiJson(
        `/api/performances/${active.id}/review?userId=${userId}`,
        "POST",
        { score: sc, comments: comments.trim() },
        authHeader
      );
      setReviewOpen(false);
      setActive(null);
      setComments("");
      await loadAssignments();
    } catch (e: any) {
      setError(errMsg(e));
    }
  }

  async function doApprove(p: PerformanceView) {
    if (!userId) return;
    try {
      setError(null);
      await apiJson(
        `/api/performances/${p.id}/approve?userId=${userId}`,
        "POST",
        undefined,
        authHeader
      );
      await loadAssignments();
    } catch (e: any) {
      setError(errMsg(e));
    }
  }

  async function doReject() {
    if (!active || !userId) return;
    if (!rejectReason.trim()) {
      setError("Η αιτιολογία απόρριψης είναι υποχρεωτική.");
      return;
    }
    try {
      setError(null);
      await apiJson(
        `/api/performances/${active.id}/reject?userId=${userId}`,
        "POST",
        { reason: rejectReason.trim() },
        authHeader
      );
      setRejectOpen(false);
      setActive(null);
      setRejectReason("");
      await loadAssignments();
    } catch (e: any) {
      setError(errMsg(e));
    }
  }

  const headerFestivalText = festival
    ? `${festival.title} (#${festival.id})`
    : festivalId
      ? `Festival #${festivalId}`
      : "";

  return (
    <div className="mx-auto max-w-6xl space-y-4">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Οι Αναθέσεις μου</h1>
          <p className="text-sm text-muted-foreground">
            Εδώ βλέπεις μόνο τις προβολές που σου έχουν ανατεθεί ως handler. Από εδώ κάνεις Review και μετά Approve/Reject (όπως επιτρέπει το backend).
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Link to="/screenings" className="text-sm font-medium underline underline-offset-4">
            Όλες οι Προβολές
          </Link>
          <Link to="/dashboard" className="text-sm font-medium underline underline-offset-4">
            Dashboard
          </Link>
        </div>
      </div>

      {/* Festival selector + controls */}
      <Card>
        <CardHeader>
          <CardTitle className="flex flex-wrap items-center gap-2">
            Επιλογή Προγράμματος
            {festival?.state ? (
              <Badge variant={festivalStateVariant(festival.state)}>
                {festival.state}
              </Badge>
            ) : null}
          </CardTitle>
          <CardDescription>
            {headerFestivalText
              ? `Τρέχον: ${headerFestivalText} • ${fmtDate(festival?.startDate)} – ${fmtDate(
                  festival?.endDate
                )}`
              : "Διάλεξε πρόγραμμα για να δεις τις αναθέσεις σου."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {error ? (
            <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {error}
            </div>
          ) : null}

          <div className="grid gap-3 md:grid-cols-4">
            <div className="md:col-span-2">
              <label className="text-xs font-medium text-muted-foreground">
                Πρόγραμμα (Festival)
              </label>
              <select
                className="mt-1 w-full rounded-xl border bg-background px-3 py-2 text-sm"
                value={festivalId ?? ""}
                onChange={(e) => setFestivalId(e.target.value ? Number(e.target.value) : null)}
                disabled={loadingFestivals}
              >
                <option value="" disabled>
                  {loadingFestivals ? "Φόρτωση..." : "Επίλεξε πρόγραμμα"}
                </option>
                {festivals.map((f) => (
                  <option key={f.id} value={f.id}>
                    #{f.id} • {f.title} ({f.state})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="text-xs font-medium text-muted-foreground">Κατάσταση</label>
              <select
                className="mt-1 w-full rounded-xl border bg-background px-3 py-2 text-sm"
                value={stateFilter}
                onChange={(e) => setStateFilter(e.target.value)}
              >
                <option value="ALL">Όλες</option>
                <option value="ASSIGNED">ASSIGNED</option>
                <option value="REVIEWED">REVIEWED</option>
                <option value="APPROVED">APPROVED</option>
                <option value="REJECTED">REJECTED</option>
                <option value="SCHEDULED">SCHEDULED</option>
              </select>
            </div>

            <div>
              <label className="text-xs font-medium text-muted-foreground">Ταξινόμηση</label>
              <select
                className="mt-1 w-full rounded-xl border bg-background px-3 py-2 text-sm"
                value={sort}
                onChange={(e) => setSort(e.target.value as any)}
              >
                <option value="state">State → Name</option>
                <option value="name">Name</option>
              </select>
            </div>
          </div>

          <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
            <input
              className="w-full rounded-xl border bg-background px-3 py-2 text-sm"
              placeholder="Αναζήτηση (backend search στο festival: name/genre/description)..."
              value={q}
              onChange={(e) => setQ(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") loadAssignments();
              }}
            />
            <Button onClick={loadAssignments} disabled={!festivalId || loadingItems}>
              Αναζήτηση
            </Button>
          </div>

          <div className="text-xs text-muted-foreground">
            <b>Κανόνες:</b> Review επιτρέπεται μόνο όταν το festival είναι <b>REVIEW</b> και η προβολή είναι <b>ASSIGNED</b>.
            Approve/Reject επιτρέπεται μόνο όταν η προβολή είναι <b>REVIEWED</b> (και πάντα από τον assigned handler).
          </div>
        </CardContent>
      </Card>

      {/* List */}
      <div className="space-y-3">
        {loadingItems ? (
          <Card>
            <CardContent className="py-6 text-sm text-muted-foreground">
              Φόρτωση αναθέσεων...
            </CardContent>
          </Card>
        ) : null}

        {!loadingItems && festivalId && filtered.length === 0 ? (
          <Card>
            <CardContent className="py-6 text-sm text-muted-foreground">
              Δεν υπάρχουν αναθέσεις για τα επιλεγμένα φίλτρα.
            </CardContent>
          </Card>
        ) : null}

        {!loadingItems &&
          filtered.map((p) => {
            const perfState = (p.state || "").toUpperCase();
            const festState = (festival?.state || "").toUpperCase();

            const canReview = canReviewFestival && perfState === "ASSIGNED";
            const canApproveReject = festState === "REVIEW" && perfState === "REVIEWED";

            return (
              <Card key={p.id} className="overflow-hidden">
                <CardHeader className="space-y-2">
                  <div className="flex flex-wrap items-start justify-between gap-2">
                    <div>
                      <CardTitle className="text-lg">
                        {p.name?.trim() || `Performance #${p.id}`}
                      </CardTitle>
                      <CardDescription>
                        {p.genre?.trim() ? p.genre : "—"} • ID: {p.id}
                      </CardDescription>
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                      <Badge variant={stateBadgeVariant(p.state)}>{p.state}</Badge>
                      {p.scheduledSlot ? (
                        <Badge variant="warn">Slot: {p.scheduledSlot}</Badge>
                      ) : null}
                      {typeof p.lastReviewScore === "number" ? (
                        <Badge variant="info">Last score: {p.lastReviewScore}</Badge>
                      ) : null}
                    </div>
                  </div>

                  {p.description?.trim() ? (
                    <div className="text-sm text-muted-foreground">
                      {p.description.length > 260
                        ? p.description.slice(0, 260) + "…"
                        : p.description}
                    </div>
                  ) : null}

                  {p.lastReviewComments?.trim() ? (
                    <div className="rounded-xl border bg-muted/30 p-3 text-xs text-muted-foreground">
                      <div className="font-medium text-foreground">Τελευταίο σχόλιο review:</div>
                      <div className="mt-1 whitespace-pre-wrap">{p.lastReviewComments}</div>
                    </div>
                  ) : null}
                </CardHeader>

                <CardFooter className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                  <div className="text-xs text-muted-foreground">
                    Πρόγραμμα: {festival?.title || `#${p.festivalId}`} • Κατάσταση προγράμματος:{" "}
                    <b>{festival?.state || "—"}</b>
                  </div>

                  <div className="flex flex-wrap gap-2">
                    <Button
                      variant="secondary"
                      disabled={!canReview}
                      onClick={() => {
                        setActive(p);
                        setScore(7);
                        setComments("");
                        setReviewOpen(true);
                      }}
                      title={
                        canReview
                          ? "Καταχώρηση review"
                          : "Review επιτρέπεται μόνο σε festival=REVIEW και performance=ASSIGNED"
                      }
                    >
                      Review
                    </Button>

                    <Button
                      disabled={!canApproveReject}
                      onClick={() => doApprove(p)}
                      title={
                        canApproveReject
                          ? "Approve μετά το review"
                          : "Approve επιτρέπεται μόνο σε festival=REVIEW και performance=REVIEWED"
                      }
                    >
                      Approve
                    </Button>

                    <Button
                      variant="destructive"
                      disabled={!canApproveReject}
                      onClick={() => {
                        setActive(p);
                        setRejectReason("");
                        setRejectOpen(true);
                      }}
                      title={
                        canApproveReject
                          ? "Reject μετά το review"
                          : "Reject επιτρέπεται μόνο σε festival=REVIEW και performance=REVIEWED"
                      }
                    >
                      Reject
                    </Button>
                  </div>
                </CardFooter>
              </Card>
            );
          })}
      </div>

      {/* Review modal */}
      <Modal
        open={reviewOpen}
        title="Review Προβολής"
        description={
          active
            ? `Performance #${active.id} • Θα αποθηκευτεί ως REVIEWED (μόνο στο festival=REVIEW).`
            : undefined
        }
        onClose={() => {
          setReviewOpen(false);
          setActive(null);
        }}
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setReviewOpen(false)}>
              Άκυρο
            </Button>
            <Button onClick={doReview}>Υποβολή Review</Button>
          </div>
        }
      >
        <div className="space-y-3">
          <div>
            <label className="text-sm font-medium">Score (0–10)</label>
            <input
              type="number"
              min={0}
              max={10}
              className="mt-1 w-full rounded-xl border bg-background px-3 py-2 text-sm"
              value={score}
              onChange={(e) => setScore(Number(e.target.value))}
            />
          </div>

          <div>
            <label className="text-sm font-medium">Σχόλια</label>
            <textarea
              className="mt-1 w-full rounded-xl border bg-background px-3 py-2 text-sm min-h-[110px]"
              value={comments}
              onChange={(e) => setComments(e.target.value)}
              placeholder="Γράψε τεκμηριωμένα σχόλια (required)."
            />
          </div>
        </div>
      </Modal>

      {/* Reject modal */}
      <Modal
        open={rejectOpen}
        title="Απόρριψη Προβολής"
        description={
          active
            ? `Performance #${active.id} • Θα γίνει REJECTED και θα αποθηκευτεί η αιτιολογία.`
            : undefined
        }
        onClose={() => {
          setRejectOpen(false);
          setActive(null);
        }}
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setRejectOpen(false)}>
              Άκυρο
            </Button>
            <Button variant="destructive" onClick={doReject}>
              Επιβεβαίωση Reject
            </Button>
          </div>
        }
      >
        <div className="space-y-3">
          <div>
            <label className="text-sm font-medium">Αιτιολογία (required)</label>
            <textarea
              className="mt-1 w-full rounded-xl border bg-background px-3 py-2 text-sm min-h-[110px]"
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Π.χ. σύγκρουση απαιτήσεων / ανεπαρκής τεκμηρίωση / μη εφικτό slot..."
            />
          </div>
          <div className="text-xs text-muted-foreground">
            Tip: στο backend η απόρριψη επιτρέπεται μόνο όταν είσαι ο assigned handler και το performance είναι REVIEWED.
          </div>
        </div>
      </Modal>

      <div className="pt-2 text-center text-xs text-muted-foreground">
        Festival Manager • My Assignments (STAFF)
      </div>
    </div>
  );
}
