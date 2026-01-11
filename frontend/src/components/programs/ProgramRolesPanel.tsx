import { useMemo, useState } from "react";
import type { ProgramDto } from "../../api/programs";
import { rolesAssignable } from "./programRules";

type Props = {
  program: ProgramDto;
  isProgrammer: boolean;
  actorUserId: number;

  onAssignRole: (targetUserId: number, roleId: 1 | 2 | 3) => Promise<void>;
};

export default function ProgramRolesPanel({ program, isProgrammer, actorUserId, onAssignRole }: Props) {
  const [target, setTarget] = useState("");
  const [roleId, setRoleId] = useState<1 | 2 | 3>(2);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);

  const enabled = useMemo(() => isProgrammer && rolesAssignable(program.state), [isProgrammer, program.state]);

  async function assign() {
    setMsg(null);

    const n = Number(target);
    if (!Number.isFinite(n) || n <= 0) return setMsg("Βάλε έγκυρο userId (αριθμός).");

    setBusy(true);
    try {
      await onAssignRole(n, roleId);
      setTarget("");
      setMsg("OK: Ο ρόλος ανατέθηκε.");
    } catch (e: any) {
      setMsg(e?.response?.data?.message ?? "Αποτυχία ανάθεσης ρόλου.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="rounded-2xl border bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="text-sm text-slate-500">Ρόλοι</div>
          <div className="mt-1 text-lg font-semibold">Ανάθεση ρόλου σε χρήστη</div>

          <div className="mt-1 text-sm text-slate-500">
            Επιτρέπεται μόνο σε <b>CREATED</b> ή <b>SUBMISSION</b> και μόνο από PROGRAMMER.
          </div>

          <div className="mt-2 text-xs text-slate-500">
            Actor userId (εσύ): <b>{actorUserId}</b> • roleId: 1=PROGRAMMER, 2=STAFF, 3=SUBMITTER
          </div>
        </div>
      </div>

      <div className="mt-4 grid gap-3">
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          <div>
            <label className="text-sm font-medium text-slate-700">Target userId</label>
            <input
              value={target}
              onChange={(e) => setTarget(e.target.value)}
              disabled={!enabled || busy}
              className="mt-1 w-full rounded-xl border px-3 py-2 text-sm"
              placeholder="π.χ. 2"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-slate-700">Ρόλος</label>
            <select
              value={roleId}
              onChange={(e) => setRoleId(Number(e.target.value) as any)}
              disabled={!enabled || busy}
              className="mt-1 w-full rounded-xl border px-3 py-2 text-sm"
            >
              <option value={1}>PROGRAMMER</option>
              <option value={2}>STAFF</option>
              <option value={3}>SUBMITTER</option>
            </select>
          </div>
        </div>

        {msg && <div className="rounded-xl border bg-slate-50 p-3 text-sm text-slate-700">{msg}</div>}

        <div className="flex justify-end">
          <button
            disabled={!enabled || busy}
            onClick={assign}
            className={[
              "rounded-xl px-5 py-2 text-sm font-semibold",
              enabled ? "bg-slate-900 text-white hover:bg-slate-800" : "bg-slate-200 text-slate-500",
            ].join(" ")}
          >
            Ανάθεση ρόλου
          </button>
        </div>
      </div>
    </div>
  );
}
