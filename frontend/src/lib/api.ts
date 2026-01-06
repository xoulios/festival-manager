export type ApiError = {
  status: number;
  message: string;
};

function buildHeaders(authHeader?: string) {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (authHeader) headers["Authorization"] = authHeader;
  return headers;
}

async function parseError(res: Response): Promise<ApiError> {
  let msg = `HTTP ${res.status}`;
  try {
    const text = await res.text();
    if (text) msg = text;
  } catch {
    // ignore
  }
  return { status: res.status, message: msg };
}

export async function apiGet<T>(path: string, authHeader?: string): Promise<T> {
  const res = await fetch(path, {
    headers: {
      ...(authHeader ? { Authorization: authHeader } : {}),
      Accept: "application/json",
    },
  });

  const contentType = res.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${res.statusText}${text ? ` - ${text}` : ""}`);
  }

  if (!isJson) {
    const text = await res.text();
    throw new Error(`Το backend δεν επέστρεψε JSON. Έστειλε: ${text.slice(0, 200)}`);
  }

  return (await res.json()) as T;
}


export async function apiJson<T>(
  url: string,
  method: "POST" | "PUT" | "PATCH" | "DELETE",
  body?: unknown,
  authHeader?: string
): Promise<T> {
  const res = await fetch(url, {
    method,
    headers: buildHeaders(authHeader),
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) throw await parseError(res);
  const text = await res.text();
  return (text ? JSON.parse(text) : null) as T;
}
