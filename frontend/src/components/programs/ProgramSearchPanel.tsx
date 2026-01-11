import { useMemo, useState } from "react";
import type { User } from "../../context/AuthContext";

export type ProgramSort = "DATE_TITLE" | "TITLE" | "STATE";

export type ProgramFilters = {
  q: string;
  state: string;
  roleFilter: "ALL" | "PROGRAMMER" | "STAFF";
  from: string;
  to: string;
  sort: ProgramSort;
};

type Props = {
  user: User | null;
  filters: ProgramFilters;
  onChange: (next: ProgramFilters) => void;
  onReset: () => void;
  totalCount: number;
  filteredCount: number;
  loading?: boolean;
};

export default function ProgramSearchPanel({
  user,
  filters,
  onChange,
  onReset,
  totalCount,
  filteredCount,
  loading,
}: Props) {
  const [open, setOpen] = useState(false);

  const canRoleFilter = useMemo(() => (user?.festivalRoles?.length ?? 0) > 0, [user?.festivalRoles]);

  const states = useMemo(
    () => ["CREATED", "SUBMISSION", "ASSIGNMENT", "REVIEW", "SCHEDULING", "FINAL_PUBLICATION", "DECISION", "ANNOUNCED"],
    []
  );

  return (
    <section className="card">
      <div className="card-head">
        <h2 className="card-title">Αναζήτηση & Φίλτρα</h2>

        <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
          <span className="tag">
            {loading ? "Φόρτωση..." : `${filteredCount} / ${totalCount}`}
          </span>

          <button className="btn-ghost" onClick={() => setOpen((v) => !v)}>
            {open ? "Κλείσιμο advanced" : "Advanced φίλτρα"}
          </button>

          <button className="btn-secondary" onClick={onReset}>
            Καθαρισμός
          </button>
        </div>
      </div>

      <div style={{ display: "grid", gap: 10 }}>
        <input
          className="input"
          value={filters.q}
          onChange={(e) => onChange({ ...filters, q: e.target.value })}
          placeholder="Αναζήτηση (AND): λέξεις σε τίτλο ή περιγραφή..."
        />

        {open && (
          <div className="dash-grid" style={{ marginTop: 0 }}>
            <div className="card" style={{ boxShadow: "none" }}>
              <div className="card-head">
                <h3 className="card-title">Περιορισμοί</h3>
              </div>

              <div className="form-grid">
                <div className="field-row">
                  <div className="field">
                    <label className="label">Από (startDate ≥)</label>
                    <input
                      type="date"
                      className="input"
                      value={filters.from}
                      onChange={(e) => onChange({ ...filters, from: e.target.value })}
                    />
                  </div>

                  <div className="field">
                    <label className="label">Έως (endDate ≤)</label>
                    <input
                      type="date"
                      className="input"
                      value={filters.to}
                      onChange={(e) => onChange({ ...filters, to: e.target.value })}
                    />
                  </div>
                </div>

                <div className="field-row">
                  <div className="field">
                    <label className="label">Κατάσταση</label>
                    <select
                      className="select"
                      value={filters.state}
                      onChange={(e) => onChange({ ...filters, state: e.target.value })}
                    >
                      <option value="">Όλες</option>
                      {states.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="field">
                    <label className="label">Φίλτρο ρόλου (ανά πρόγραμμα)</label>
                    <select
                      className="select"
                      value={filters.roleFilter}
                      onChange={(e) => onChange({ ...filters, roleFilter: e.target.value as any })}
                      disabled={!canRoleFilter}
                      title={!canRoleFilter ? "Δεν υπάρχουν festivalRoles στο user context." : ""}
                    >
                      <option value="ALL">Όλα</option>
                      <option value="PROGRAMMER">Όπου είμαι PROGRAMMER</option>
                      <option value="STAFF">Όπου είμαι STAFF</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            <div className="card" style={{ boxShadow: "none" }}>
              <div className="card-head">
                <h3 className="card-title">Ταξινόμηση</h3>
              </div>

              <div className="form-grid">
                <div className="field">
                  <label className="label">Sort</label>
                  <select
                    className="select"
                    value={filters.sort}
                    onChange={(e) => onChange({ ...filters, sort: e.target.value as any })}
                  >
                    <option value="DATE_TITLE">Ημερομηνία → Τίτλος (προτεινόμενο)</option>
                    <option value="TITLE">Τίτλος</option>
                    <option value="STATE">Κατάσταση → Τίτλος</option>
                  </select>
                </div>

                <div className="muted" style={{ marginTop: 6 }}>
                  AND semantics: αν γράψεις <b>2+ λέξεις</b>, πρέπει να εμφανίζονται όλες (σε τίτλο ή/και περιγραφή).
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
