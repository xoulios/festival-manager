import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { apiGet } from "../lib/api";

type Festival = {
  id: number;
  title: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  state?: string;
  createdBy?: string;
};

function formatDate(s?: string) {
  if (!s) return "—";
  const d = new Date(s);
  if (Number.isNaN(d.getTime())) return s; 
  return d.toLocaleDateString("el-GR");
}

export default function ProgramDetailsPage() {
  const { id } = useParams();
  const [program, setProgram] = useState<Festival | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const numericId = useMemo(() => {
    const n = Number(id);
    return Number.isFinite(n) ? n : null;
  }, [id]);

  useEffect(() => {
    if (numericId == null) {
      setErr("Μη έγκυρο id προγράμματος.");
      setLoading(false);
      return;
    }

    (async () => {
      setLoading(true);
      setErr(null);
      try {
        const data = await apiGet<Festival>(`/api/festivals/${numericId}`);
        setProgram(data ?? null);
      } catch (e: any) {
        setProgram(null);
        setErr(e?.message || "Αποτυχία φόρτωσης προγράμματος.");
      } finally {
        setLoading(false);
      }
    })();
  }, [numericId]);

  return (
    <div className="program-details-screen">
      <div className="program-details-container">
        <div className="breadcrumbs">
          <Link to="/">Home</Link>
          <span>›</span>
          <Link to="/programs">Προγράμματα</Link>
          <span>›</span>
          <span>Λεπτομέρειες</span>
        </div>

        {loading && (
          <div style={{ marginTop: 14 }} className="card">
            <div className="muted">Φόρτωση…</div>
          </div>
        )}

        {!loading && err && (
          <div style={{ marginTop: 14 }} className="card">
            <div style={{ color: "#b91c1c", fontWeight: 900, fontSize: 13 }}>
              {err}
            </div>
            <div style={{ marginTop: 12, display: "flex", gap: 10, flexWrap: "wrap" }}>
              <Link to="/programs" className="btn-secondary">
                Πίσω στη λίστα
              </Link>
              <button className="btn-secondary" onClick={() => window.location.reload()}>
                Επανάληψη
              </button>
            </div>
          </div>
        )}

        {!loading && !err && program && (
          <div className="details-grid">
            {/* Main */}
            <section className="card">
              <h1 className="details-title">{program.title}</h1>
              <p className="details-subtitle">
                Διάρκεια: <b>{formatDate(program.startDate)}</b> –{" "}
                <b>{formatDate(program.endDate)}</b>
              </p>

              <div className="kv">
                <div className="kv-row">
                  <span>Κατάσταση</span>
                  <strong>{program.state ?? "—"}</strong>
                </div>
                <div className="kv-row">
                  <span>Πρόγραμμα ID</span>
                  <strong>{program.id}</strong>
                </div>
              </div>

              <div style={{ marginTop: 14 }}>
                <h2 className="card-title">Περιγραφή</h2>
                <div className="details-text">
                  {program.description?.trim()
                    ? program.description
                    : "Δεν υπάρχει διαθέσιμη περιγραφή για το πρόγραμμα."}
                </div>
              </div>
            </section>

            {/* Side */}
            <aside className="card">
              <div className="card-head">
                <h2 className="card-title">Ενέργειες</h2>
              </div>

              <div style={{ display: "grid", gap: 10 }}>
                <Link to="/programs" className="btn-secondary">
                  Πίσω στα Προγράμματα
                </Link>
                <Link to="/login" className="btn-primary">
                  Σύνδεση για Διαχείριση
                </Link>
                <Link to="/" className="btn-ghost">
                  Home
                </Link>
              </div>

              <div style={{ marginTop: 14 }}>
                <h3 className="card-title">Πληροφορίες</h3>
                <ul className="list" style={{ marginTop: 10 }}>
                  <li className="list-item">
                    <strong>Δημιουργήθηκε από</strong>
                    <span className="tag">{program.createdBy ?? "—"}</span>
                  </li>
                  <li className="list-item">
                    <strong>Ημερομηνία έναρξης</strong>
                    <span className="tag">{formatDate(program.startDate)}</span>
                  </li>
                  <li className="list-item">
                    <strong>Ημερομηνία λήξης</strong>
                    <span className="tag">{formatDate(program.endDate)}</span>
                  </li>
                </ul>
              </div>
            </aside>
          </div>
        )}

        <div style={{ marginTop: 18, textAlign: "center" }} className="muted">
          Festival Manager • Program Details
        </div>
      </div>
    </div>
  );
}
