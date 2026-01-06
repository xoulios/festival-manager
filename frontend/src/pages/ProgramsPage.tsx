import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { apiGet } from "../lib/api";

type Festival = {
  id: number;
  title: string;
  state?: string;
  startDate?: string;
  endDate?: string;
};

function safeText(v: any) {
  return typeof v === "string" ? v : "";
}

export default function ProgramsPage() {
  const [items, setItems] = useState<Festival[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [q, setQ] = useState("");
  const [sort, setSort] = useState<"title" | "state">("title");

  useEffect(() => {
    (async () => {
      setLoading(true);
      setErr(null);
      try {
        const data = await apiGet<Festival[]>("/api/festivals");
        setItems(Array.isArray(data) ? data : []);
      } catch (e: any) {
        setItems([]);
        setErr(e?.message || "Αποτυχία φόρτωσης προγραμμάτων.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const filtered = useMemo(() => {
    const term = q.trim().toLowerCase();
    const base = term
      ? items.filter((x) => safeText(x.title).toLowerCase().includes(term))
      : items.slice();

    base.sort((a, b) => {
      if (sort === "state") {
        return safeText(a.state).localeCompare(safeText(b.state));
      }
      return safeText(a.title).localeCompare(safeText(b.title));
    });

    return base;
  }, [items, q, sort]);

  return (
    <div className="programs-screen">
      <div className="programs-container">
        <div className="programs-head">
          <div>
            <h1 className="programs-title">Προγράμματα</h1>
            <p className="programs-subtitle">
              Εδώ εμφανίζονται τα διαθέσιμα προγράμματα (festivals). Μπορείς να ανοίξεις λεπτομέρειες ή να κάνεις σύνδεση για διαχείριση.
            </p>
          </div>

          <div className="programs-tools">
            <input
              className="input"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Αναζήτηση τίτλου…"
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

            <Link to="/login" className="btn-primary">
              Σύνδεση
            </Link>
            <Link to="/" className="btn-ghost">
              Home
            </Link>
          </div>
        </div>

        {/* Content */}
        {loading && (
          <div style={{ marginTop: 14 }} className="card">
            <div className="muted">Φόρτωση προγραμμάτων...</div>
          </div>
        )}

        {!loading && err && (
          <div style={{ marginTop: 14 }} className="card">
            <div className="muted" style={{ color: "#b91c1c", fontWeight: 800 }}>
              {err}
            </div>
            <div style={{ marginTop: 10 }}>
              <button
                className="btn-secondary"
                onClick={() => window.location.reload()}
              >
                Επανάληψη
              </button>
            </div>
          </div>
        )}

        {!loading && !err && filtered.length === 0 && (
          <div className="empty-box">
            Δεν βρέθηκαν προγράμματα με βάση την αναζήτηση.
          </div>
        )}

        {!loading && !err && filtered.length > 0 && (
          <div className="programs-grid">
            {filtered.map((f) => (
              <div key={f.id} className="card">
                <h2 className="program-card-title">{f.title}</h2>

                <div className="program-meta">
                  <span className="pill">ID: {f.id}</span>
                  {f.state && <span className="pill">{f.state}</span>}
                  {(f.startDate || f.endDate) && (
                    <span className="pill pill-muted">
                      {f.startDate ? `Από: ${f.startDate}` : "Από: —"}{" "}
                      {f.endDate ? `• Έως: ${f.endDate}` : ""}
                    </span>
                  )}
                </div>

                <div className="program-actions">
                  <Link to={`/programs/${f.id}`} className="btn-secondary">
                    Λεπτομέρειες
                  </Link>

                  <Link to="/login" className="btn-ghost">
                    Διαχείριση (login)
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}

        <div style={{ marginTop: 18, textAlign: "center" }} className="muted">
          Festival Manager • Public Programs
        </div>
      </div>
    </div>
  );
}
