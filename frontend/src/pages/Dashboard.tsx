import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiGet } from "../lib/api";

type Festival = { id: number; title: string; state: string };

export default function Dashboard() {
  const { user, authHeader } = useAuth();
  const [festivals, setFestivals] = useState<Festival[]>([]);
  const [loading, setLoading] = useState(true);

  const roleLabel = useMemo(() => user?.role ?? "VISITOR", [user?.role]);

  useEffect(() => {
    (async () => {
      try {
        const data = await apiGet<Festival[]>("/api/festivals", authHeader);
        setFestivals(Array.isArray(data) ? data : []);
      } catch {
        setFestivals([]);
      } finally {
        setLoading(false);
      }
    })();
  }, [authHeader]);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            Καλώς ήρθες{user?.username ? `, ${user.username}` : ""}!
          </h1>
        </div>

        <div className="role-badge">Ρόλος: {roleLabel}</div>
      </div>

      <div className="dash-grid">
        <section className="card">
          <div className="card-head">
            <h2 className="card-title">Σύνοψη</h2>
            <Link className="card-link" to="/programs">
              Προγράμματα →
            </Link>
          </div>

          {loading ? (
            <p className="muted">Φόρτωση...</p>
          ) : (
            <>
              <p className="muted">
                Συνολικά προγράμματα (festivals): <b>{festivals.length}</b>
              </p>

              {festivals.length > 0 && (
                <ul className="list">
                  {festivals.slice(0, 3).map((f) => (
                    <li key={f.id} className="list-item">
                      <strong>{f.title}</strong>
                      <span className="tag">{f.state}</span>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </section>

        <section className="card">
          <div className="card-head">
            <h2 className="card-title">Γρήγορες ενέργειες</h2>
          </div>

          <div style={{ display: "grid", gap: 10 }}>
            {(user?.role === "PROGRAMMER" || user?.role === "STAFF") && (
              <>
                <Link to="/programs/manage" className="btn-primary">
                  Διαχείριση Προγραμμάτων
                </Link>
                <Link to="/screenings" className="btn-secondary">
                  Προβολές / Πρόγραμμα
                </Link>
              </>
            )}

            {user?.role === "SUBMITTER" && (
              <Link to="/my-submissions" className="btn-primary">
                Οι Υποβολές μου
              </Link>
            )}
            {user?.role === "STAFF" && (
              <Link to="/my-assignments" className="btn-secondary">
                Οι Αναθέσεις μου
              </Link>
            )}

            <Link to="/profile" className="btn-ghost">
              Προφίλ
            </Link>
          </div>
        </section>
      </div>
    </div>
  );
}
