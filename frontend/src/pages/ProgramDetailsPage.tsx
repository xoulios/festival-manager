import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

import {
  type ProgramDto,
  type ProgramState,
  deleteProgram,
  getProgram,
  updateProgram,
  updateProgramState,
  assignFestivalRole,
} from "../api/programs";

import ProgramEditForm from "../components/programs/ProgramEditForm";
import ProgramRolesPanel from "../components/programs/ProgramRolesPanel";
import ProgramWorkflowPanel from "../components/programs/ProgramWorkflowPanel";

function formatDate(s?: string | null) {
  if (!s) return "—";
  const d = new Date(s);
  if (Number.isNaN(d.getTime())) return String(s);
  return d.toLocaleDateString("el-GR");
}

type Tab = "summary" | "edit" | "roles" | "workflow";

export default function ProgramDetailsPage() {
  const { id } = useParams();
  const nav = useNavigate();
  const { user } = useAuth();

  const [program, setProgram] = useState<ProgramDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  const [tab, setTab] = useState<Tab>("summary");

  const programId = useMemo(() => {
    const n = Number(id);
    return Number.isFinite(n) ? n : null;
  }, [id]);

  const isProgrammer = useMemo(() => {
    if (!user || programId == null) return false;

    const perFestival = (user.festivalRoles ?? []).some(
      (r) => r.festivalId === programId && String(r.role).toUpperCase().includes("PROGRAMMER")
    );

    return perFestival || String(user.role).toUpperCase() === "PROGRAMMER";
  }, [user, programId]);

  async function reload() {
    if (programId == null) return;
    setLoading(true);
    setErr(null);
    try {
      const p = await getProgram(programId);
      setProgram(p);
    } catch (e: any) {
      setProgram(null);
      setErr(e?.response?.data?.message ?? e?.message ?? "Αποτυχία φόρτωσης προγράμματος.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (programId == null) {
      setErr("Μη έγκυρο id προγράμματος.");
      setLoading(false);
      return;
    }
    reload();
  }, [programId]);

  async function onSave(payload: { title: string; description: string; startDate: string; endDate: string }) {
    if (!program) return;
    await updateProgram(program.id, payload);
    await reload();
    setTab("summary");
  }

  async function onChangeState(next: ProgramState) {
    if (!program) return;
    if (!user) throw new Error("Απαιτείται σύνδεση.");
    await updateProgramState(program.id, next);
    await reload();
  }

  async function onDelete() {
    if (!program) return;
    await deleteProgram(program.id);
    nav("/programs/manage");
  }

  async function onAssignRole(targetUserId: number, roleId: 1 | 2 | 3) {
    if (!program) return;
    if (!user) throw new Error("Απαιτείται σύνδεση.");
    await assignFestivalRole({
      festivalId: program.id,
      targetUserId,
      roleId,
    });
  }

  return (
    <div className="page">
      <div className="container">
        <div className="page-head">
          <div>
            <h1 className="page-title">Διαχείριση Προγράμματος</h1>
            <p className="page-subtitle">
              Προβολή/επεξεργασία/roles/workflow. Τα management actions εμφανίζονται μόνο σε PROGRAMMER.
            </p>
          </div>

          <div className="page-actions">
            <Link className="btn-secondary" to="/programs/manage">
              ← Πίσω
            </Link>
            {!user && (
              <Link className="btn-primary" to="/login">
                Σύνδεση
              </Link>
            )}
          </div>
        </div>

        {loading && (
          <section className="card">
            <div className="muted">Φόρτωση…</div>
          </section>
        )}

        {!loading && err && (
          <section className="card">
            <div style={{ color: "#b91c1c", fontWeight: 900 }}>{err}</div>
            <div style={{ marginTop: 12, display: "flex", gap: 10, flexWrap: "wrap" }}>
              <button className="btn-secondary" onClick={reload}>
                Επανάληψη
              </button>
              <Link className="btn-secondary" to="/programs/manage">
                Πίσω στη λίστα
              </Link>
            </div>
          </section>
        )}

        {!loading && !err && program && (
          <>
            {/* Tabs */}
            <section className="card">
              <div className="card-head" style={{ alignItems: "center" }}>
                <div>
                  <div className="card-title" style={{ marginBottom: 4 }}>
                    {program.title}
                  </div>
                  <div className="muted">
                    {formatDate(program.startDate)} – {formatDate(program.endDate)} • <b>{program.state}</b>
                  </div>
                </div>

                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                  <button className={tab === "summary" ? "btn-primary" : "btn-secondary"} onClick={() => setTab("summary")}>
                    Σύνοψη
                  </button>

                  <button
                    className={tab === "edit" ? "btn-primary" : "btn-secondary"}
                    onClick={() => setTab("edit")}
                    disabled={!isProgrammer}
                    title={!isProgrammer ? "Μόνο PROGRAMMER" : ""}
                  >
                    Επεξεργασία
                  </button>

                  <button
                    className={tab === "roles" ? "btn-primary" : "btn-secondary"}
                    onClick={() => setTab("roles")}
                    disabled={!isProgrammer}
                    title={!isProgrammer ? "Μόνο PROGRAMMER" : ""}
                  >
                    Ρόλοι
                  </button>

                  <button
                    className={tab === "workflow" ? "btn-primary" : "btn-secondary"}
                    onClick={() => setTab("workflow")}
                    disabled={!isProgrammer}
                    title={!isProgrammer ? "Μόνο PROGRAMMER" : ""}
                  >
                    Workflow
                  </button>
                </div>
              </div>

              {!isProgrammer && (
                <div className="muted" style={{ marginTop: 10 }}>
                  Βρίσκεσαι σε “public view”. Για πλήρη διαχείριση χρειάζεσαι ρόλο PROGRAMMER στο συγκεκριμένο πρόγραμμα.
                </div>
              )}
            </section>

            {/* Content */}
            {tab === "summary" && (
              <div className="dash-grid" style={{ marginTop: 14 }}>
                <section className="card">
                  <div className="card-head">
                    <h2 className="card-title">Βασικά στοιχεία</h2>
                  </div>

                  <div className="kv">
                    <div className="kv-row">
                      <span>ID</span>
                      <strong>{program.id}</strong>
                    </div>
                    <div className="kv-row">
                      <span>Κατάσταση</span>
                      <strong>{program.state}</strong>
                    </div>
                    <div className="kv-row">
                      <span>Έναρξη</span>
                      <strong>{formatDate(program.startDate)}</strong>
                    </div>
                    <div className="kv-row">
                      <span>Λήξη</span>
                      <strong>{formatDate(program.endDate)}</strong>
                    </div>
                  </div>

                  <div style={{ marginTop: 14 }}>
                    <h3 className="card-title">Περιγραφή</h3>
                    <div className="details-text" style={{ marginTop: 8 }}>
                      {program.description?.trim() ? program.description : "—"}
                    </div>
                  </div>
                </section>

                <aside className="card">
                  <div className="card-head">
                    <h2 className="card-title">Γρήγορες ενέργειες</h2>
                  </div>

                  <div style={{ display: "grid", gap: 10 }}>
                    <Link className="btn-secondary" to="/programs/manage">
                      Διαχείριση Προγραμμάτων
                    </Link>

                    <Link className="btn-secondary" to={`/screenings?programId=${program.id}`}>
                      Προβολές / Screenings
                    </Link>

                    <Link className="btn-ghost" to="/dashboard">
                      Dashboard
                    </Link>
                  </div>

                  <div className="muted" style={{ marginTop: 12, fontSize: 12 }}>
                    (Το screenings link είναι “safe” ακόμα κι αν δεν το έχεις τελειώσει, απλά σε βοηθάει να δέσει το flow.)
                  </div>
                </aside>
              </div>
            )}

            {tab === "edit" && (
              <div style={{ marginTop: 14 }}>
                <ProgramEditForm program={program} isProgrammer={isProgrammer} onSave={onSave} />
              </div>
            )}

            {tab === "roles" && (
              <div style={{ marginTop: 14 }}>
                <ProgramRolesPanel
                  program={program}
                  isProgrammer={isProgrammer}
                  actorUserId={user?.userId ?? 0}
                  onAssignRole={onAssignRole}
                />
              </div>
            )}

            {tab === "workflow" && (
              <div style={{ marginTop: 14 }}>
                <ProgramWorkflowPanel
                  program={program}
                  isProgrammer={isProgrammer}
                  onChangeState={onChangeState}
                  onDelete={onDelete}
                />
              </div>
            )}
          </>
        )}

        <div style={{ marginTop: 18, textAlign: "center" }} className="muted">
          Festival Manager • Program Details / Management
        </div>
      </div>
    </div>
  );
}
