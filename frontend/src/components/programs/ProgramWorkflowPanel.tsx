import { useMemo, useState } from "react";
import type { ProgramDto, ProgramState } from "../../api/programs";
import { allowedNextStates, canDeleteProgram } from "./programRules";

type Props = {
  program: ProgramDto;
  isProgrammer: boolean;
  onChangeState: (next: ProgramState) => Promise<void>;
  onDelete: () => Promise<void>;
};

export default function ProgramWorkflowPanel({ program, isProgrammer, onChangeState, onDelete }: Props) {
  const [busy, setBusy] = useState(false);
  const nextStates = useMemo(() => allowedNextStates(program.state), [program.state]);
  const deletable = useMemo(() => isProgrammer && canDeleteProgram(program.state), [isProgrammer, program.state]);

  async function handleState(next: ProgramState) {
    setBusy(true);
    try {
      await onChangeState(next);
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete() {
    if (!confirm("Σίγουρα θέλεις να διαγράψεις το πρόγραμμα; (Επιτρέπεται μόνο σε CREATED)")) return;
    setBusy(true);
    try {
      await onDelete();
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="rounded-2xl border bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="text-sm text-slate-500">Workflow</div>
          <div className="mt-1 text-lg font-semibold">Κατάσταση: {program.state}</div>
          <div className="mt-1 text-sm text-slate-500">Οι μεταβάσεις είναι σειριακές, χωρίς rollback.</div>
        </div>

        <span className="rounded-full border px-3 py-1 text-xs font-medium text-slate-700">ID #{program.id}</span>
      </div>

      <div className="mt-4 flex flex-wrap gap-2">
        {nextStates.length === 0 && <div className="text-sm text-slate-500">Δεν υπάρχει επόμενη μετάβαση.</div>}

        {nextStates.map((st) => (
          <button
            key={st}
            disabled={!isProgrammer || busy}
            onClick={() => handleState(st)}
            className={[
              "rounded-xl px-4 py-2 text-sm font-semibold",
              isProgrammer ? "bg-slate-900 text-white hover:bg-slate-800" : "bg-slate-200 text-slate-500",
            ].join(" ")}
          >
            Μετάβαση → {st}
          </button>
        ))}
      </div>

      <div className="mt-5 border-t pt-4">
        <button
          disabled={!deletable || busy}
          onClick={handleDelete}
          className={[
            "rounded-xl px-4 py-2 text-sm font-semibold",
            deletable ? "bg-red-600 text-white hover:bg-red-700" : "bg-slate-200 text-slate-500",
          ].join(" ")}
        >
          Διαγραφή Program
        </button>

        {!deletable && (
          <div className="mt-2 text-xs text-slate-500">
            Η διαγραφή επιτρέπεται μόνο σε CREATED και μόνο από PROGRAMMER.
          </div>
        )}
      </div>
    </div>
  );
}
