import type { ProgramState } from "../../api/programs";

export function allowedNextStates(state: ProgramState): ProgramState[] {
  const next: Record<ProgramState, ProgramState | null> = {
    CREATED: "SUBMISSION",
    SUBMISSION: "ASSIGNMENT",
    ASSIGNMENT: "REVIEW",
    REVIEW: "SCHEDULING",
    SCHEDULING: "FINAL_SUBMISSION",
    FINAL_SUBMISSION: "DECISION",
    DECISION: "ANNOUNCED",
    ANNOUNCED: "COMPLETE",
    COMPLETE: null,
  };

  const n = next[state];
  return n ? [n] : [];
}

export function canDeleteProgram(state: ProgramState) {
  return state === "CREATED";
}

export function canEditProgram(state: ProgramState) {
  return state !== "ANNOUNCED" && state !== "COMPLETE";
}

export function rolesAssignable(state: ProgramState) {
  return state === "CREATED" || state === "SUBMISSION";
}

export function staffFrozen(state: ProgramState) {
  return !rolesAssignable(state);
}
