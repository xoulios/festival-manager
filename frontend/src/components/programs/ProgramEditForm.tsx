import { useEffect, useState } from "react";
import type { ProgramDto } from "../../api/programs";
import { canEditProgram } from "./programRules";

type Props = {
  program: ProgramDto;
  isProgrammer: boolean;
  onSave: (payload: { title: string; description: string; startDate: string; endDate: string }) => Promise<void>;
};

export default function ProgramEditForm({ program, isProgrammer, onSave }: Props) {
  const [title, setTitle] = useState(program.title ?? "");
  const [description, setDescription] = useState(program.description ?? "");
  const [startDate, setStartDate] = useState(program.startDate?.slice(0, 10) ?? "");
  const [endDate, setEndDate] = useState(program.endDate?.slice(0, 10) ?? "");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setTitle(program.title ?? "");
    setDescription(program.description ?? "");
    setStartDate(program.startDate?.slice(0, 10) ?? "");
    setEndDate(program.endDate?.slice(0, 10) ?? "");
  }, [program]);

  const editable = isProgrammer && canEditProgram(program.state);

  async function submit() {
    setError(null);

    if (!title.trim()) return setError("Ο τίτλος είναι υποχρεωτικός.");
    if (!description.trim()) return setError("Η περιγραφή είναι υποχρεωτική.");
    if (!startDate) return setError("Η ημερομηνία έναρξης είναι υποχρεωτική.");
    if (!endDate) return setError("Η ημερομηνία λήξης είναι υποχρεωτική.");
    if (endDate < startDate) return setError("Η λήξη δεν μπορεί να είναι πριν την έναρξη.");

    setBusy(true);
    try {
      await onSave({ title: title.trim(), description: description.trim(), startDate, endDate });
    } catch (e: any) {
      setError(e?.response?.data?.message ?? "Αποτυχία αποθήκευσης.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="rounded-2xl border bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="text-sm text-slate-500">Επεξεργασία</div>
          <div className="mt-1 text-lg font-semibold">Στοιχεία προγράμματος</div>
          {!editable && (
            <div className="mt-1 text-sm text-slate-500">
              Δεν μπορείς να κάνεις αλλαγές (ή δεν είσαι PROGRAMMER, ή το πρόγραμμα είναι ANNOUNCED/COMPLETE).
            </div>
          )}
        </div>
      </div>

      <div className="mt-4 grid gap-3">
        <div>
          <label className="text-sm font-medium text-slate-700">Τίτλος</label>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            disabled={!editable || busy}
            className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2"
            placeholder="π.χ. Demo Festival"
          />
        </div>

        <div>
          <label className="text-sm font-medium text-slate-700">Περιγραφή</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={!editable || busy}
            className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2"
            rows={4}
            placeholder="Μια σύντομη περιγραφή..."
          />
        </div>

        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          <div>
            <label className="text-sm font-medium text-slate-700">Έναρξη</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              disabled={!editable || busy}
              className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-slate-700">Λήξη</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              disabled={!editable || busy}
              className="mt-1 w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2"
            />
          </div>
        </div>

        {error && <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}

        <div className="flex justify-end">
          <button
            disabled={!editable || busy}
            onClick={submit}
            className={[
              "rounded-xl px-5 py-2 text-sm font-semibold",
              editable ? "bg-slate-900 text-white hover:bg-slate-800" : "bg-slate-200 text-slate-500",
            ].join(" ")}
          >
            Αποθήκευση
          </button>
        </div>
      </div>
    </div>
  );
}
