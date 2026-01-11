import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { apiGet } from "../lib/api";

type Festival = {
  id: number;
  name: string;
  startDate?: string;
  endDate?: string;
  description?: string;
  state: string;
};

type PerformancePublic = {
  id: number;
  name?: string;
  genre?: string;
  scheduledSlot?: string;
  state?: string;
};

function formatDate(s?: string) {
  if (!s) return "—";
  const d = new Date(s);
  if (Number.isNaN(d.getTime())) return s;
  return d.toLocaleDateString("el-GR");
}

function andMatch(term: string, hay: string) {
  const words = term
    .trim()
    .toLowerCase()
    .split(/\s+/)
    .filter(Boolean);
  const h = hay.toLowerCase();
  return words.every((w) => h.includes(w));
}

export default function SchedulePage() {
  const [sp, setSp] = useSearchParams();
  const [festivals, setFestivals] = useState<Festival[]>([]);
  const [items, setItems] = useState<PerformancePublic[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const festivalId = Number(sp.get("festivalId") || "0");
  const q = sp.get("q") || "";

  useEffect(() => {
    (async () => {
      setLoading(true);
      setErr(null);
      try {
        const all = await apiGet<Festival[]>("/api/festivals");
        const announced = (Array.isArray(all) ? all : []).filter(
          (f) => (f.state || "").toUpperCase() === "ANNOUNCED"
        );
        setFestivals(announced);

        if (!festivalId && announced[0]?.id) {
          setSp((prev) => {
            prev.set("festivalId", String(announced[0].id));
            return prev;
          });
        }
      } catch (e: any) {
        setErr(e?.message || "Αποτυχία φόρτωσης προγραμμάτων.");
        setFestivals([]);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  useEffect(() => {
    if (!festivalId) return;
    (async () => {
      setLoading(true);
      setErr(null);
      try {
        const raw = await apiGet<any[]>(`/api/performances/search?festivalId=${festivalId}`);
        const list = Array.isArray(raw) ? raw : [];

        const mapped: PerformancePublic[] = list.map((p) => ({
          id: p.id,
          name: p.name,
          genre: p.genre,
          scheduledSlot: p.scheduledSlot,
          state: p.state,
        }));

        setItems(mapped);
      } catch (e: any) {
        setErr(e?.message || "Αποτυχία φόρτωσης timetable.");
        setItems([]);
      } finally {
        setLoading(false);
      }
    })();
  }, [festivalId]);

  const festival = useMemo(
    () => festivals.find((f) => f.id === festivalId),
    [festivals, festivalId]
  );

  const visible = useMemo(() => {
    const onlyScheduled = items.filter((p) => (p.state || "").toUpperCase() === "SCHEDULED");

    const filtered = q.trim()
      ? onlyScheduled.filter((p) =>
          andMatch(q, `${p.name || ""} ${p.genre || ""} ${p.scheduledSlot || ""}`)
        )
      : onlyScheduled;

    filtered.sort((a, b) => (a.scheduledSlot || "").localeCompare(b.scheduledSlot || ""));
    return filtered;
  }, [items, q]);

  return (
    <div className="programs-screen">
      <div className="programs-container">
        <div className="programs-head">
          <div>
            <h1 className="programs-title">Timetable (Δημόσιο)</h1>
            <p className="programs-subtitle">
              Εμφανίζονται μόνο τα <b>ANNOUNCED</b> προγράμματα και μόνο οι προβολές που είναι{" "}
              <b>SCHEDULED</b>.
            </p>
          </div>

          <div className="programs-tools">
            <select
              className="select"
              value={festivalId || ""}
              onChange={(e) =>
                setSp((prev) => {
                  prev.set("festivalId", e.target.value);
                  return prev;
                })
              }
            >
              {festivals.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>

            <input
              className="input"
              placeholder="Αναζήτηση (AND): λέξεις σε title/genre/slot…"
              value={q}
              onChange={(e) =>
                setSp((prev) => {
                  const v = e.target.value;
                  if (v) prev.set("q", v);
                  else prev.delete("q");
                  return prev;
                })
              }
            />

            <Link to="/programs" className="btn-secondary">
              Προγράμματα
            </Link>
            <Link to="/" className="btn-ghost">
              Home
            </Link>
          </div>
        </div>

        {festival && (
          <div className="card" style={{ marginTop: 14 }}>
            <div style={{ display: "flex", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
              <div>
                <div style={{ fontWeight: 800 }}>{festival.name}</div>
                <div className="muted">
                  {formatDate(festival.startDate)} – {formatDate(festival.endDate)}
                </div>
              </div>
              <div className="muted">{festival.description || ""}</div>
            </div>
          </div>
        )}

        {loading && (
          <div className="card" style={{ marginTop: 14 }}>
            <div className="muted">Φόρτωση…</div>
          </div>
        )}

        {!loading && err && (
          <div className="card" style={{ marginTop: 14 }}>
            <div style={{ color: "#b91c1c", fontWeight: 900, fontSize: 13 }}>{err}</div>
          </div>
        )}

        {!loading && !err && (
          <div style={{ marginTop: 14 }} className="grid gap-3">
            {visible.length === 0 && (
              <div className="card">
                <div className="muted">Δεν υπάρχουν scheduled προβολές για εμφάνιση.</div>
              </div>
            )}

            {visible.map((p) => (
              <div key={p.id} className="card">
                <div style={{ display: "flex", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
                  <div>
                    <div style={{ fontWeight: 900 }}>{p.name || `Performance #${p.id}`}</div>
                    <div className="muted">{p.genre || "—"}</div>
                  </div>
                  <div style={{ fontWeight: 800 }}>
                    {p.scheduledSlot || "—"}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="home-footer" style={{ marginTop: 22 }}>
          Festival Manager • Timetable
        </div>
      </div>
    </div>
  );
}
