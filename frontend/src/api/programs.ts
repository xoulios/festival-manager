import api from "./api";

export type ProgramState =
  | "CREATED"
  | "SUBMISSION"
  | "ASSIGNMENT"
  | "REVIEW"
  | "SCHEDULING"
  | "FINAL_SUBMISSION"
  | "DECISION"
  | "ANNOUNCED"
  | "COMPLETE";

export type ProgramDto = {
  id: number;
  title: string;
  description: string | null;
  startDate: string;
  endDate: string;   
  state: ProgramState;
};

export type ProgramUpdatePayload = {
  title: string;
  description: string;
  startDate: string;
  endDate: string;
};

export async function getProgram(id: number) {
  const { data } = await api.get<ProgramDto>(`/api/festivals/${id}`);
  return data;
}

export async function updateProgram(id: number, payload: ProgramUpdatePayload) {
  const { data } = await api.put<ProgramDto>(`/api/festivals/${id}`, payload);
  return data;
}

export async function deleteProgram(id: number) {
  await api.delete(`/api/festivals/${id}`);
}

export async function updateProgramState(programId: number, nextState: ProgramState) {
  const { data } = await api.patch<ProgramDto>(
    `/api/festivals/${programId}/state`,
    null,
    { params: { state: nextState } }
  );
  return data;
}

export async function assignFestivalRole(params: {
  festivalId: number;
  targetUserId: number;
  roleId: 1 | 2 | 3;
}) {
  const { festivalId, targetUserId, roleId } = params;
  await api.post(
    `/api/festivals/${festivalId}/roles`,
    { userId: targetUserId, roleId },
    undefined
  );
}
