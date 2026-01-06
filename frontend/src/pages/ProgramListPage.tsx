import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiGet } from "../lib/api";

interface Festival {
  id: number;
  title: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  state?: string;
}

function formatDate(s?: string) {
  if (!s) return "—";
  const d = new Date(s);
  if (Number.isNaN(d.getTime())) return s;
  return d.toLocaleDateString("el-GR");
}

function safeText(v: any) {
  return typeof v === "string" ? v : "";
}

const ProgramListPage: React.FC = () => {
  const { user, authHeader } = useAuth();
  const userRole = user?.role ?? "VISITOR";

  const [programs, setPrograms] = useState<Festival[]>([]);
  const [search, setSearch] = useState("");
  const [sort, setSort] = useState<"title" | "state">("title");
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      setLoading(true);
      setErr(null);

      try {
        const data = await apiGet<Festival[]>(
          "/api/festivals",
          userRole === "VISITOR" ? undefined : authHeader
        );

        const all = Array.isArray(data) ? data : [];

        const visible =
          userRole === "VISITOR"
            ? all.filter((p) => p.state === "ANNOUNCED")
            : all;

        setPrograms(visible);
      } catch (e: any) {
        setPrograms([]);
        setErr(e?.message || "Αποτυχία φόρτωσης προγραμμάτων.");
      } finally {
        setLoading(false);
      }
    })();
  }, [userRole, authHeader]);

  const filteredPrograms = useMemo(() => {
    const term = search.trim().toLowerCase();

    const base = term
      ? programs.filter((p) => safeText(p.title).toLowerCase().includes(term))
      : programs.slice();

    base.sort((a, b) => {
      if (sort === "state") {
        return safeText(a.state).localeCompare(safeText(b.state));
      }
      return safeText(a.title).localeCompare(safeText(b.title));
    });

    return base;
  }, [programs, search, sort]);

  return (
    <div className="programs-screen">
      <div className="programs-container">
        <div className="programs-head">
          <div>
            <h1 className="programs-title">Προγράμματα</h1>
            <p className="programs-subtitle">
              {userRole === "VISITOR"
                ? "Ως επισκέπτης βλέπεις μόνο τα ανακοινωμένα προγράμματα."
                : "Ως συνδεδεμένος χρήστης βλέπεις όλα τα προγράμματα και έχεις πρόσβαση στη διαχείριση."}
            </p>
          </div>

          <div className="programs-tools">
            <input
              type="text"
              className="input"
              placeholder="Αναζήτηση με όνομα…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

            <select
              className="select"
              value={sort}
              onChange={(e) => setSort(e.target.value as any)}
              aria-label="Ταξινόμηση"
            >
              <option value="title">Ταξινόμηση: Τίτλος</option>
              <option value="state">Ταξινόμηση: Κατάσταση</option>
            </select>

            {userRole !== "VISITOR" && (
              <Link to="/programs/manage" className="btn-primary">
                Διαχείριση Προγραμμάτων
              </Link>
            )}

            <Link to="/" className="btn-ghost">
              Home
            </Link>
          </div>
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
              <button className="btn-secondary" onClick={() => window.location.reload()}>
                Επανάληψη
              </button>
              <Link to="/login" className="btn-secondary">
                Σύνδεση
              </Link>
            </div>
          </div>
        )}

        {!loading && !err && filteredPrograms.length === 0 && (
          <div className="empty-box">Δεν βρέθηκαν προγράμματα.</div>
        )}

        {!loading && !err && filteredPrograms.length > 0 && (
          <div className="programs-grid">
            {filteredPrograms.map((p) => (
              <div key={p.id} className="card">
                <h2 className="program-card-title">{p.title}</h2>

                <div className="program-meta">
                  <span className="pill">ID: {p.id}</span>
                  {p.state && <span className="pill">{p.state}</span>}
                  <span className="pill pill-muted">
                    {formatDate(p.startDate)} – {formatDate(p.endDate)}
                  </span>
                </div>

                <div style={{ marginTop: 10 }} className="muted">
                  {p.description?.trim()
                    ? p.description.length > 160
                      ? p.description.slice(0, 160) + "…"
                      : p.description
                    : "Δεν υπάρχει περιγραφή."}
                </div>

                <div className="program-actions">
                  <Link to={`/programs/${p.id}`} className="btn-secondary">
                    Λεπτομέρειες
                  </Link>

                  {userRole === "VISITOR" ? (
                    <Link to="/login" className="btn-ghost">
                      Σύνδεση για διαχείριση
                    </Link>
                  ) : (
                    <Link to="/programs/manage" className="btn-ghost">
                      Άνοιγμα διαχείρισης
                    </Link>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        <div style={{ marginTop: 18, textAlign: "center" }} className="muted">
          Festival Manager • Program List
        </div>
      </div>
    </div>
  );
};

export default ProgramListPage;
