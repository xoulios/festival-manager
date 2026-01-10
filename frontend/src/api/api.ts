type ApiResult<T> = { data: T };
type RequestConfig = { params?: Record<string, string | number | boolean | null | undefined> };

function getAuthHeader(): string | undefined {
  try {
    const raw = localStorage.getItem("fm_auth_user");
    if (!raw) return undefined;
    const parsed = JSON.parse(raw);
    return parsed?.basicAuth;
  } catch {
    return undefined;
  }
}

function withParams(url: string, config?: RequestConfig) {
  const params = config?.params;
  if (!params) return url;

  const qs = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v === null || v === undefined) continue;
    qs.set(k, String(v));
  }
  const s = qs.toString();
  if (!s) return url;
  return url.includes("?") ? `${url}&${s}` : `${url}?${s}`;
}

async function request<T>(method: string, url: string, body?: unknown, config?: RequestConfig): Promise<ApiResult<T>> {
  const headers: Record<string, string> = { Accept: "application/json" };

  const auth = getAuthHeader();
  if (auth) headers["Authorization"] = auth;

  if (body !== undefined && body !== null) headers["Content-Type"] = "application/json";

  const finalUrl = withParams(url, config);

  const res = await fetch(finalUrl, {
    method,
    headers,
    body: body === undefined || body === null ? undefined : JSON.stringify(body),
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`${res.status} ${res.statusText}${text ? ` - ${text}` : ""}`);
  }

  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) {
    const data = (await res.json()) as T;
    return { data };
  }

  const text = (await res.text().catch(() => "")) as unknown as T;
  return { data: text };
}

const api = {
  get: <T>(url: string, config?: RequestConfig) => request<T>("GET", url, undefined, config),
  post: <T = any>(url: string, body?: unknown, config?: RequestConfig) => request<T>("POST", url, body, config),
  put: <T = any>(url: string, body?: unknown, config?: RequestConfig) => request<T>("PUT", url, body, config),
  patch: <T = any>(url: string, body?: unknown, config?: RequestConfig) => request<T>("PATCH", url, body, config),
  delete: <T = any>(url: string, config?: RequestConfig) => request<T>("DELETE", url, undefined, config),
};

export default api;
