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
    case "CREATED":
      return "neutral";
    case "SUBMITTED":
    case "ASSIGNED":
    case "REVIEWED":
      return "info";
    case "APPROVED":
    case "PROVISIONALLY_SCHEDULED":
      return "warn";
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
      return "neutral";
    case "SUBMISSION":
    case "ASSIGNMENT":
    case "REVIEW":
      return "info";
    case "SCHEDULING":
    case "FINAL_SUBMISSION":
    case "DECISION":
      return "warn";
    case "ANNOUNCED":
    case "COMPLETE":
      return "ok";
    default:
      return "neutral";
  }
}

export default function ScreeningsPage() {
  const { user, authHeader } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  const [festivals, setFestivals] = useState<Festival[]>([]);
  const [festival, setFestival] = useState<Festival | null>(null);

  const [items, setItems] = useState<PerformanceView[]>([]);
  const [q, setQ] = useState<string>("");
  const [sort, setSort] = useState<"genreTitle" | "slot">("genreTitle");

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const festivalId = useMemo(() => {
    const raw = searchParams.get("festivalId");
    const n = raw ? Number(raw) : NaN;
    return Number.isFinite(n) && n > 0 ? n : null;
  }, [searchParams]);

  const festivalRole = useMemo(() => {
    if (!user) return "VISITOR";
    if (festivalId) {
      const fr = user.festivalRoles?.find((r) => Number(r.festivalId) === Number(festivalId));
      if (fr?.role) return fr.role;
    }
    return user.role;
  }, [user, festivalId]);

  const canProgrammer = festivalRole === "PROGRAMMER";
  const canStaff = festivalRole === "STAFF";
  const canSubmitter = festivalRole === "SUBMITTER";

  async function loadAll(nextFestivalId?: number, nextQ?: string) {
    if (!user?.userId) return;
    const fid = nextFestivalId ?? festivalId;
    if (!fid) return;

    setLoading(true);
    setError(null);

    try {
      const [f, list] = await Promise.all([
        apiGet<Festival>(`/api/festivals/${fid}`, authHeader),
        apiGet<PerformanceView>(
          `/api/performances/search-view?festivalId=${fid}&userId=${user.userId}`,
          authHeader
        ).then(async () => {
          const res = await apiGet<PerformanceView[]>(
            `/api/performances/search-view?festivalId=${fid}&userId=${user.userId}${
              (nextQ ?? q).trim() ? `&q=${encodeURIComponent((nextQ ?? q).trim())}` : ""
            }`,
            authHeader
          );
          return res as unknown as PerformanceView;
        }),
      ]);

      setFestival(f as Festival);

      const arr = (list as unknown as PerformanceView[]) ?? [];
      const sorted = [...arr].sort((a, b) => {
        if (sort === "slot") {
          const as = (a.scheduledSlot || "").toLowerCase();
          const bs = (b.scheduledSlot || "").toLowerCase();
          return as.localeCompare(bs);
        }
        const ag = (a.genre || "").toLowerCase();
        const bg = (b.genre || "").toLowerCase();
        if (ag !== bg) return ag.localeCompare(bg);
        const at = (a.name || "").toLowerCase();
        const bt = (b.name || "").toLowerCase();
        return at.localeCompare(bt);
      });

      setItems(sorted);
    } catch (e: any) {
      setError(e?.message || "Σφάλμα κατά τη φόρτωση.");
      setItems([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    (async () => {
      try {
        const list = await apiGet<Festival[]>("/api/festivals", authHeader);
        setFestivals(list || []);

        if (!festivalId && list?.length) {
          setSearchParams((prev) => {
            const p = new URLSearchParams(prev);
            p.set("festivalId", String(list[0].id));
            return p;
          });
        }
      } catch (e: any) {
        setFestivals([]);
      }
    })();
  }, []);

  useEffect(() => {
    if (festivalId) loadAll(festivalId);
  }, [festivalId, sort]);

  const [active, setActive] = useState<PerformanceView | null>(null);

  const [assignOpen, setAssignOpen] = useState(false);
  const [staffId, setStaffId] = useState<string>("");

  const [reviewOpen, setReviewOpen] = useState(false);
  const [reviewScore, setReviewScore] = useState<string>("");
  const [reviewComments, setReviewComments] = useState<string>("");

  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState<string>("");

  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [scheduledSlot, setScheduledSlot] = useState<string>("");

  const [finalSubmitOpen, setFinalSubmitOpen] = useState(false);
  const [finalSetlist, setFinalSetlist] = useState<string>("");
  const [finalRehearsalTimes, setFinalRehearsalTimes] = useState<string>("");
  const [finalTimeSlots, setFinalTimeSlots] = useState<string>("");

  async function doPost(path: string, body?: any) {
    try {
      setLoading(true);
      setError(null);
      await apiJson<any>(path, "POST", body, authHeader);
      await loadAll();
    } catch (e: any) {
      const msg =
        typeof e?.message === "string"
          ? e.message
          : typeof e?.status === "number"
          ? `${e.status} ${e?.message || ""}`
          : "Απέτυχε η ενέργεια.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  const headerFestivalText = festival ? `${festival.title} (#${festival.id})` : festivalId ? `Festival #${festivalId}` : "";

  return (
    <div className="mx-auto w-full max-w-5xl px-4 py-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="text-3xl font-semibold tracking-tight">Προβολές</div>
          <div className="mt-1 text-sm text-muted-foreground">
            Διαχείριση προβολών/παραστάσεων για επιλεγμένο πρόγραμμα.
          </div>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <Badge variant="info">Ρόλος: {festivalRole}</Badge>
            {festival?.state ? (
              <Badge variant={festivalStateVariant(festival.state)}>
                Πρόγραμμα: {festival.state}
              </Badge>
            ) : null}
            {festival ? <Badge>{headerFestivalText}</Badge> : null}
          </div>
        </div>

        <div className="flex flex-col gap-2 sm:items-end">
          <div className="flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
            <select
              className="h-9 w-full rounded-md border bg-background px-3 text-sm sm:w-64"
              value={festivalId ?? ""}
              onChange={(e) => {
                const next = e.target.value ? Number(e.target.value) : "";
                setSearchParams((prev) => {
                  const p = new URLSearchParams(prev);
                  if (next) p.set("festivalId", String(next));
                  else p.delete("festivalId");
                  return p;
                });
              }}
            >
              {(festivals || []).map((f) => (
                <option key={f.id} value={f.id}>
                  {f.title} (#{f.id})
                </option>
              ))}
              {!festivals.length ? <option value="">(Δεν βρέθηκαν festivals)</option> : null}
            </select>

            <Link to={festivalId ? `/programs/manage?festivalId=${festivalId}` : "/programs/manage"}>
              <Button variant="outline" className="w-full sm:w-auto">
                Διαχείριση Προγραμμάτων
              </Button>
            </Link>
          </div>

          <div className="text-xs text-muted-foreground">
            Tip: Χρησιμοποίησε το search για γρήγορο φιλτράρισμα (backend q).
          </div>
        </div>
      </div>

      <Card className="mt-5 rounded-2xl shadow-sm">
        <CardHeader className="pb-3">
          <CardTitle className="text-base">Αναζήτηση & Ταξινόμηση</CardTitle>
          <CardDescription>
            Αναζήτηση σε τίτλο/περιγραφή/genre (ανάλογα με το backend q).
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <input
            className="h-10 w-full rounded-md border bg-background px-3 text-sm sm:flex-1"
            placeholder="Αναζήτηση (q)..."
            value={q}
            onChange={(e) => setQ(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") loadAll(undefined, q);
            }}
          />
          <select
            className="h-10 w-full rounded-md border bg-background px-3 text-sm sm:w-56"
            value={sort}
            onChange={(e) => setSort(e.target.value as any)}
          >
            <option value="genreTitle">Ταξινόμηση: Genre → Τίτλος</option>
            <option value="slot">Ταξινόμηση: Scheduled Slot</option>
          </select>
          <div className="flex gap-2">
            <Button variant="default" onClick={() => loadAll(undefined, q)} disabled={!festivalId || loading}>
              Αναζήτηση
            </Button>
            <Button variant="outline" onClick={() => loadAll()} disabled={!festivalId || loading}>
              Ανανέωση
            </Button>
          </div>
        </CardContent>
        {error ? (
          <CardFooter className="pt-0">
            <div className="w-full rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              {error}
            </div>
          </CardFooter>
        ) : null}
      </Card>

      <div className="mt-5 grid gap-4">
        <Card className="rounded-2xl shadow-sm">
          <CardHeader className="pb-2">
            <div className="flex items-center justify-between gap-3">
              <div>
                <CardTitle className="text-base">Λίστα</CardTitle>
                <CardDescription>
                  Σύνολο: <span className="font-medium">{items.length}</span>
                </CardDescription>
              </div>
              {loading ? <Badge variant="info">Φόρτωση...</Badge> : <Badge>Έτοιμο</Badge>}
            </div>
          </CardHeader>

          <CardContent className="pt-0">
            {!items.length ? (
              <div className="rounded-2xl border bg-muted/30 p-4 text-sm text-muted-foreground">
                Δεν βρέθηκαν προβολές/performances για τα τρέχοντα κριτήρια.
              </div>
            ) : (
              <div className="grid gap-3">
                {items.map((p) => (
                  <div
                    key={p.id}
                    className="rounded-2xl border bg-card p-4 shadow-sm"
                  >
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <div className="truncate text-base font-semibold">
                            {p.name || `Performance #${p.id}`}
                          </div>
                          {p.genre ? <Badge variant="info">{p.genre}</Badge> : null}
                          <Badge variant={stateBadgeVariant(p.state)}>{p.state}</Badge>
                        </div>

                        {p.description ? (
                          <div className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                            {p.description}
                          </div>
                        ) : (
                          <div className="mt-1 text-sm text-muted-foreground">
                            (Χωρίς περιγραφή)
                          </div>
                        )}

                        <div className="mt-3 flex flex-wrap items-center gap-2 text-sm">
                          <Badge>
                            Slot: {p.scheduledSlot ? p.scheduledSlot : "—"}
                          </Badge>

                          {typeof p.lastReviewScore === "number" ? (
                            <Badge variant="warn">
                              Review: {p.lastReviewScore}/10
                            </Badge>
                          ) : (
                            <Badge variant="neutral">Review: —</Badge>
                          )}
                        </div>

                        {p.lastReviewComments ? (
                          <div className="mt-2 rounded-xl border bg-muted/30 p-3 text-sm">
                            <div className="text-xs font-medium text-muted-foreground">
                              Τελευταίο σχόλιο review
                            </div>
                            <div className="mt-1">{p.lastReviewComments}</div>
                          </div>
                        ) : null}
                      </div>

                      <div className="flex w-full flex-col gap-2 sm:w-60">
                        {/* PROGRAMMER actions */}
                        {canProgrammer ? (
                          <>
                            <Button
                              variant="outline"
                              onClick={() => {
                                setActive(p);
                                setAssignOpen(true);
                                setStaffId("");
                              }}
                              disabled={loading}
                            >
                              Ανάθεση Handler
                            </Button>

                            <Button
                              variant="default"
                              onClick={() => doPost(`/api/performances/${p.id}/approve?userId=${user!.userId}`)}
                              disabled={loading}
                            >
                              Approve
                            </Button>

                            <Button
                              variant="outline"
                              onClick={() => {
                                setActive(p);
                                setScheduleOpen(true);
                                setScheduledSlot(p.scheduledSlot || "");
                              }}
                              disabled={loading}
                            >
                              Schedule
                            </Button>

                            <Button
                              variant="destructive"
                              onClick={() => {
                                setActive(p);
                                setRejectOpen(true);
                                setRejectReason("");
                              }}
                              disabled={loading}
                            >
                              Reject
                            </Button>

                            <div className="h-px bg-border my-1" />

                            <Button
                              variant="default"
                              onClick={() => doPost(`/api/performances/${p.id}/final-accept?userId=${user!.userId}`)}
                              disabled={loading}
                            >
                              Final Accept
                            </Button>

                            <Button
                              variant="destructive"
                              onClick={() => {
                                setActive(p);
                                setRejectOpen(true);
                                setRejectReason("");
                              }}
                              disabled={loading}
                            >
                              Final Reject
                            </Button>
                          </>
                        ) : null}

                        {/* STAFF actions */}
                        {canStaff ? (
                          <Button
                            variant="default"
                            onClick={() => {
                              setActive(p);
                              setReviewOpen(true);
                              setReviewScore(p.lastReviewScore?.toString?.() || "");
                              setReviewComments(p.lastReviewComments || "");
                            }}
                            disabled={loading}
                          >
                            Review
                          </Button>
                        ) : null}

                        {/* SUBMITTER actions */}
                        {canSubmitter ? (
                          <>
                            <Button
                              variant="default"
                              onClick={() => doPost(`/api/performances/${p.id}/submit`)}
                              disabled={loading}
                            >
                              Submit
                            </Button>
                            <Button
                              variant="destructive"
                              onClick={() => doPost(`/api/performances/${p.id}/withdraw`)}
                              disabled={loading}
                            >
                              Withdraw
                            </Button>

                            <Button
                              variant="outline"
                              onClick={() => {
                                setActive(p);
                                setFinalSubmitOpen(true);
                                setFinalSetlist(p.finalSetlist || "");
                                setFinalRehearsalTimes(p.finalRehearsalTimes || "");
                                setFinalTimeSlots(p.finalTimeSlots || "");
                              }}
                              disabled={loading}
                            >
                              Final Submit
                            </Button>
                          </>
                        ) : null}

                        {!canProgrammer && !canStaff && !canSubmitter ? (
                          <div className="rounded-xl border bg-muted/30 p-3 text-xs text-muted-foreground">
                            Δεν έχεις διαθέσιμες ενέργειες για αυτό το πρόγραμμα.
                          </div>
                        ) : null}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>

          <CardFooter className="pt-0">
            <div className="w-full text-center text-xs text-muted-foreground">
              Festival Manager • Screenings (mapped to Performances)
            </div>
          </CardFooter>
        </Card>
      </div>

      {/* Assign Handler */}
      <Modal
        open={assignOpen}
        title="Ανάθεση Handler (STAFF)"
        description={active ? `Performance #${active.id}` : undefined}
        onClose={() => setAssignOpen(false)}
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setAssignOpen(false)}>
              Άκυρο
            </Button>
            <Button
              onClick={async () => {
                if (!active) return;
                const sid = Number(staffId);
                if (!Number.isFinite(sid) || sid <= 0) {
                  setError("Δώσε έγκυρο staffId (αριθμός).");
                  return;
                }
                await doPost(
                  `/api/performances/${active.id}/assign-handler?userId=${user!.userId}&staffId=${sid}`
                );
                setAssignOpen(false);
              }}
              disabled={loading}
            >
              Ανάθεση
            </Button>
          </div>
        }
      >
        <div className="grid gap-2">
          <label className="text-sm font-medium">staffId</label>
          <input
            className="h-10 rounded-md border bg-background px-3 text-sm"
            placeholder="π.χ. 2"
            value={staffId}
            onChange={(e) => setStaffId(e.target.value)}
          />
          <div className="text-xs text-muted-foreground">
            (Στο current backend δεν υπάρχει users list endpoint, οπότε δίνουμε id χειροκίνητα.)
          </div>
        </div>
      </Modal>

      {/* Review */}
      <Modal
        open={reviewOpen}
        title="Review"
        description={active ? `Performance #${active.id}` : undefined}
        onClose={() => setReviewOpen(false)}
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setReviewOpen(false)}>
              Άκυρο
            </Button>
            <Button
              onClick={async () => {
                if (!active) return;
                const score = Number(reviewScore);
                if (!Number.isFinite(score) || score < 0 || score > 10) {
                  setError("Το score πρέπει να είναι αριθμός 0–10.");
                  return;
                }
                await doPost(`/api/performances/${active.id}/review?userId=${user!.userId}`, {
                  score,
                  comments: reviewComments,
                });
                setReviewOpen(false);
              }}
              disabled={loading}
            >
              Αποθήκευση
            </Button>
          </div>
        }
      >
        <div className="grid gap-3">
          <div className="grid gap-2">
            <label className="text-sm font-medium">Score (0–10)</label>
            <input
              className="h-10 rounded-md border bg-background px-3 text-sm"
              value={reviewScore}
              onChange={(e) => setReviewScore(e.target.value)}
              placeholder="π.χ. 8"
            />
          </div>

          <div className="grid gap-2">
            <label className="text-sm font-medium">Σχόλια</label>
            <textarea
              className="min-h-[120px] rounded-md border bg-background p-3 text-sm"
              value={reviewComments}
              onChange={(e) => setReviewComments(e.target.value)}
              placeholder="Γράψε σύντομη αλλά ουσιαστική αξιολόγηση..."
            />
          </div>
        </div>
      </Modal>

      {/* Reject / Final Reject (same modal) */}
      <Modal
        open={rejectOpen}
        title="Απόρριψη"
        description={active ? `Performance #${active.id}` : undefined}
        onClose={() => setRejectOpen(false)}
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setRejectOpen(false)}>
              Άκυρο
            </Button>
            <Button
              variant="destructive"
              onClick={async () => {
                if (!active) return;
                const reason = rejectReason.trim();
                if (!reason) {
                  setError("Η αιτιολογία είναι υποχρεωτική.");
                  return;
                }

                if (festival?.state?.toUpperCase() === "DECISION") {
                  await doPost(
                    `/api/performances/${active.id}/final-reject?userId=${user!.userId}&reason=${encodeURIComponent(
                      reason
                    )}`
                  );
                } else {
                  await doPost(`/api/performances/${active.id}/reject?userId=${user!.userId}`, {
                    reason,
                  });
                }

                setRejectOpen(false);
              }}
              disabled={loading}
            >
              Απόρριψη
            </Button>
          </div>
        }
      >
        <div className="grid gap-2">
          <label className="text-sm font-medium">Αιτιολογία</label>
          <textarea
            className="min-h-[110px] rounded-md border bg-background p-3 text-sm"
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            placeholder="π.χ. Δεν πληρούνται οι τεχνικές απαιτήσεις / αλλαγές δεν έγιναν..."
          />
        </div>
      </Modal>

      {/* Schedule */}
      <Modal
        open={scheduleOpen}
        title="Προσωρινός Προγραμματισμός (Schedule)"
        description={active ? `Performance #${active.id}` : undefined}
        onClose={() => setScheduleOpen(false)}
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setScheduleOpen(false)}>
              Άκυρο
            </Button>
            <Button
              onClick={async () => {
                if (!active) return;
                const slot = scheduledSlot.trim();
                if (!slot) {
                  setError("Το scheduled slot είναι υποχρεωτικό.");
                  return;
                }
                await doPost(`/api/performances/${active.id}/schedule?userId=${user!.userId}`, {
                  scheduledSlot: slot,
                });
                setScheduleOpen(false);
              }}
              disabled={loading}
            >
              Αποθήκευση
            </Button>
          </div>
        }
      >
        <div className="grid gap-2">
          <label className="text-sm font-medium">Scheduled Slot</label>
          <input
            className="h-10 rounded-md border bg-background px-3 text-sm"
            value={scheduledSlot}
            onChange={(e) => setScheduledSlot(e.target.value)}
            placeholder='π.χ. "2026-01-06 21:00–22:00 / Main Stage"'
          />
          <div className="text-xs text-muted-foreground">
            Tip: κράτα ένα απλό string format (όπως δουλεύει τώρα το backend).
          </div>
        </div>
      </Modal>

      {/* Final Submit */}
      <Modal
        open={finalSubmitOpen}
        title="Final Submit"
        description={active ? `Performance #${active.id}` : undefined}
        onClose={() => setFinalSubmitOpen(false)}
        footer={
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setFinalSubmitOpen(false)}>
              Άκυρο
            </Button>
            <Button
              onClick={async () => {
                if (!active) return;
                await doPost(`/api/performances/${active.id}/final-submit?userId=${user!.userId}`, {
                  finalSetlist: finalSetlist.trim(),
                  finalRehearsalTimes: finalRehearsalTimes.trim(),
                  finalTimeSlots: finalTimeSlots.trim(),
                });
                setFinalSubmitOpen(false);
              }}
              disabled={loading}
            >
              Υποβολή
            </Button>
          </div>
        }
      >
        <div className="grid gap-3">
          <div className="grid gap-2">
            <label className="text-sm font-medium">Final Setlist</label>
            <textarea
              className="min-h-[90px] rounded-md border bg-background p-3 text-sm"
              value={finalSetlist}
              onChange={(e) => setFinalSetlist(e.target.value)}
              placeholder="Τελική λίστα τραγουδιών / περιεχόμενο..."
            />
          </div>

          <div className="grid gap-2">
            <label className="text-sm font-medium">Final Rehearsal Times</label>
            <textarea
              className="min-h-[80px] rounded-md border bg-background p-3 text-sm"
              value={finalRehearsalTimes}
              onChange={(e) => setFinalRehearsalTimes(e.target.value)}
              placeholder="Τελικές ώρες πρόβας..."
            />
          </div>

          <div className="grid gap-2">
            <label className="text-sm font-medium">Final Time Slots</label>
            <textarea
              className="min-h-[80px] rounded-md border bg-background p-3 text-sm"
              value={finalTimeSlots}
              onChange={(e) => setFinalTimeSlots(e.target.value)}
              placeholder="Τελικές προτιμήσεις slot..."
            />
          </div>
        </div>
      </Modal>
    </div>
  );
}
