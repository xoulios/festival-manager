import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { apiGet } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import ProgramCard from "../components/programs/ProgramCard";
import ProgramSearchPanel, { type ProgramFilters } from "../components/programs/ProgramSearchPanel";

type Festival = {
  id: number;
  title: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  state?: string;
};

function safeText(v: any) {
  return typeof v === "string" ? v : "";
}

function isoDateOnly(s?: string) {
  if (!s) return "";
  return s.slice(0, 10);
}

function cmpNullableDate(a?: string, b?: string) {
  const da = isoDateOnly(a);
  const db = isoDateOnly(b);
  if (!da && !db) return 0;
  if (!da) return 1;
  if (!db) return -1;
  return da.localeCompare(db);
}

export default function ProgramsPage() {
  const { user, authHeader } = useAuth();
  const canCreate = user?.role === "PROGRAMMER";

  const [items, setItems] = useState<Festival[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [filters, setFilters] = useState<ProgramFilters>({
    q: "",
    state: "",
    roleFilter: "ALL",
    from: "",
    to: "",
    sort: "DATE_TITLE",
  });

  useEffect(() => {
    (async () => {
      setLoading(true);
      setErr(null);
      try {
        const data = await apiGet<Festival[]>("/api/festivals", authHeader);
        setItems(Array.isArray(data) ? data : []);
      } catch (e: any) {
        setItems([]);
        setErr(e?.message || "Αποτυχία φόρτωσης προγραμμάτων.");
      } finally {
        setLoading(false);
      }
    })();
  }, [authHeader]);

  const filtered = useMemo(() => {
    const tokens = filters.q
      .trim()
      .toLowerCase()
      .split(/\s+/)
      .filter(Boolean);

    const roleMap = new Map<number, string>();
    (user?.festivalRoles ?? []).forEach((r) => roleMap.set(r.festivalId, String(r.role).toUpperCase()));

    let base = items.slice();

    if (filters.roleFilter !== "ALL") {
      base = base.filter((p) => roleMap.get(p.id) === filters.roleFilter);
    }

    if (filters.state) {
      base = base.filter((p) => safeText(p.state).toUpperCase() === filters.state);
    }

    if (filters.from) {
      base = base.filter((p) => {
        const sd = isoDateOnly(p.startDate);
        return sd ? sd >= filters.from : false;
      });
    }
    if (filters.to) {
      base = base.filter((p) => {
        const ed = isoDateOnly(p.endDate);
        return ed ? ed <= filters.to : false;
      });
    }

    if (tokens.length > 0) {
      base = base.filter((p) => {
        const hayTitle = safeText(p.title).toLowerCase();
        const hayDesc = safeText(p.description).toLowerCase();
        return tokens.every((t) => hayTitle.includes(t) || hayDesc.includes(t));
      });
    }

    base.sort((a, b) => {
      if (filters.sort === "STATE") {
        const c1 = safeText(a.state).localeCompare(safeText(b.state));
        if (c1 !== 0) return c1;
        return safeText(a.title).localeCompare(safeText(b.title));
      }

      if (filters.sort === "TITLE") {
        return safeText(a.title).localeCompare(safeText(b.title));
      }

      const cD = cmpNullableDate(a.startDate, b.startDate);
      if (cD !== 0) return cD;
      return safeText(a.title).localeCompare(safeText(b.title));
    });

    return base;
  }, [items, filters, user?.festivalRoles]);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Διαχείριση Προγραμμάτων</h1>
          <p className="page-subtitle">
            Προβολή/αναζήτηση προγραμμάτων με advanced φίλτρα (AND semantics) και ταξινόμηση.
          </p>
        </div>

        <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
          {canCreate && (
            <Link to="/programs/manage/create" className="btn-primary">
              + Δημιουργία Program
            </Link>
          )}
          <Link to="/dashboard" className="btn-ghost">
            Dashboard
          </Link>
        </div>
      </div>

      <div style={{ marginTop: 14 }}>
        <ProgramSearchPanel
          user={user}
          filters={filters}
          onChange={setFilters}
          onReset={() =>
            setFilters({
              q: "",
              state: "",
              roleFilter: "ALL",
              from: "",
              to: "",
              sort: "DATE_TITLE",
            })
          }
          totalCount={items.length}
          filteredCount={filtered.length}
          loading={loading}
        />
      </div>

      {loading && (
        <div style={{ marginTop: 14 }} className="card">
          <div className="muted">Φόρτωση προγραμμάτων...</div>
        </div>
      )}

      {!loading && err && (
        <div style={{ marginTop: 14 }} className="card">
          <div style={{ color: "#b91c1c", fontWeight: 900, fontSize: 13 }}>{err}</div>
          <div style={{ marginTop: 12, display: "flex", gap: 10, flexWrap: "wrap" }}>
            <button className="btn-secondary" onClick={() => window.location.reload()}>
              Επανάληψη
            </button>
          </div>
        </div>
      )}

      {!loading && !err && filtered.length === 0 && (
        <div style={{ marginTop: 14 }} className="empty-box">
          Δεν βρέθηκαν προγράμματα με βάση τα φίλτρα.
        </div>
      )}

      {!loading && !err && filtered.length > 0 && (
        <div style={{ marginTop: 14 }} className="programs-grid">
          {filtered.map((p) => (
            <ProgramCard key={p.id} program={p as any} manageMode />
          ))}
        </div>
      )}

      <div style={{ marginTop: 18, textAlign: "center" }} className="muted">
        Festival Manager • Programs Management
      </div>
    </div>
  );
}
