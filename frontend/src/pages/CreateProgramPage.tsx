import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiJson } from "../lib/api";
import { useAuth } from "../context/AuthContext";

type FestivalResponse = {
  id: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  state: string;
};

export default function CreateProgramPage() {
  const navigate = useNavigate();
  const { user, authHeader } = useAuth();

  const canCreate = useMemo(() => user?.role === "PROGRAMMER", [user?.role]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function validate(): string | null {
    if (!title.trim()) return "Το όνομα (title) είναι υποχρεωτικό.";
    if (title.trim().length < 3) return "Το όνομα πρέπει να έχει τουλάχιστον 3 χαρακτήρες.";
    if (!description.trim()) return "Η περιγραφή είναι υποχρεωτική.";
    if (!startDate) return "Η ημερομηνία έναρξης είναι υποχρεωτική.";
    if (!endDate) return "Η ημερομηνία λήξης είναι υποχρεωτική.";
    if (endDate < startDate) return "Η λήξη δεν μπορεί να είναι πριν την έναρξη.";
    return null;
  }

  async function onSubmit() {
    setError(null);

    if (!canCreate) {
      setError("Μόνο PROGRAMMER μπορεί να δημιουργήσει πρόγραμμα.");
      return;
    }

    const msg = validate();
    if (msg) {
      setError(msg);
      return;
    }

    setBusy(true);
    try {
      const created = await apiJson<FestivalResponse>(
        "/api/festivals",
        "POST",
        {
          title: title.trim(),
          description: description.trim(),
          startDate,
          endDate,
        },
        authHeader
      );

      navigate(`/programs/${created.id}`);
    } catch (e: any) {
      const msg =
        e?.message ||
        e?.response?.data?.message ||
        "Αποτυχία δημιουργίας προγράμματος (πιθανόν διπλό όνομα ή μη έγκυρα πεδία).";
      setError(String(msg));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Δημιουργία Προγράμματος</h1>
          <p className="page-subtitle">
            Συμπλήρωσε τα απαιτούμενα πεδία. Το σύστημα δημιουργεί αυτόματα ID και κατάσταση <b>CREATED</b>.
          </p>
        </div>

        <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
          <Link className="btn-ghost" to="/programs/manage">
            Πίσω
          </Link>
        </div>
      </div>

      {!canCreate && (
        <div style={{ marginTop: 14 }} className="card">
          <div style={{ color: "#b91c1c", fontWeight: 900, fontSize: 13 }}>
            Δεν έχεις δικαίωμα δημιουργίας. (Απαιτείται ρόλος PROGRAMMER)
          </div>
        </div>
      )}

      <div style={{ marginTop: 14 }} className="card">
        <div className="card-head">
          <h2 className="card-title">Στοιχεία</h2>
        </div>

        <div className="form-grid">
          <div className="field">
            <label className="label">Όνομα (title)</label>
            <input
              className="input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="π.χ. Demo Festival"
              disabled={!canCreate || busy}
            />
          </div>

          <div className="field">
            <label className="label">Περιγραφή</label>
            <textarea
              className="textarea"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Σύντομη περιγραφή του φεστιβάλ..."
              rows={5}
              disabled={!canCreate || busy}
            />
          </div>

          <div className="field-row">
            <div className="field">
              <label className="label">Έναρξη</label>
              <input
                type="date"
                className="input"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                disabled={!canCreate || busy}
              />
            </div>

            <div className="field">
              <label className="label">Λήξη</label>
              <input
                type="date"
                className="input"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                disabled={!canCreate || busy}
              />
            </div>
          </div>
        </div>

        {error && (
          <div style={{ marginTop: 12 }} className="alert alert-danger">
            {error}
          </div>
        )}

        <div style={{ marginTop: 14, display: "flex", justifyContent: "flex-end", gap: 10, flexWrap: "wrap" }}>
          <button className="btn-secondary" onClick={() => navigate("/programs/manage")} disabled={busy}>
            Άκυρο
          </button>
          <button className="btn-primary" onClick={onSubmit} disabled={!canCreate || busy}>
            {busy ? "Δημιουργία..." : "Δημιουργία"}
          </button>
        </div>
      </div>
    </div>
  );
}
