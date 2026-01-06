import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiGet } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import type { UserRole } from "../context/AuthContext";


type MeResponse = {
  userId: number;
  username: string;
  effectiveRole: UserRole;
  festivalRoles: { festivalId: number; role: string }[];
};

function toBasicAuth(username: string, password: string) {
  const token = btoa(`${username}:${password}`);
  return `Basic ${token}`;
}

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const basicAuth = useMemo(
    () => toBasicAuth(username.trim(), password),
    [username, password]
  );

  useEffect(() => {
    if (user) navigate("/dashboard", { replace: true });
  }, [user, navigate]);

  const handleLogin = async () => {
    setErr(null);

    const u = username.trim();
    if (!u || !password) {
      setErr("Συμπλήρωσε username και password.");
      return;
    }

    setLoading(true);
    try {
      const me = await apiGet<MeResponse>("/api/auth/me", basicAuth);

      login({
        userId: me.userId,
        username: me.username,
        role: me.effectiveRole,
        festivalRoles: me.festivalRoles ?? [],
        basicAuth,
      });

      navigate("/dashboard");
    } catch (e: any) {
      const msg = e?.message || "Αποτυχία σύνδεσης.";
      setErr("Λάθος στοιχεία ή μη εξουσιοδοτημένη πρόσβαση. " + msg);
    } finally {
      setLoading(false);
    }
  };

  return (
  <div className="login-screen">
    <div className="login-card">
      <div className="login-header">
        <div>
          <h1 className="login-title">Σύνδεση</h1>
          <p className="login-subtitle">
            Δώσε credentials. Το σύστημα εντοπίζει αυτόματα userId και ρόλους.
          </p>
        </div>

        <div className="login-badge">FM</div>
      </div>

      {err && <div className="login-error">{err}</div>}

      <form
        className="login-form"
        onSubmit={(e) => {
          e.preventDefault();
          if (!loading) void handleLogin();
        }}
      >
        <div className="login-field">
          <label>Username</label>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="π.χ. programmer1"
            autoComplete="username"
          />
        </div>

        <div className="login-field">
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            autoComplete="current-password"
          />
        </div>

        <button className="login-button" type="submit" disabled={loading}>
          {loading ? "Σύνδεση..." : "Σύνδεση"}
        </button>

        <div className="login-footer">
          <Link to="/" className="login-link">
            Επιστροφή στο Home
          </Link>
          <span className="login-tip">Enter για σύνδεση</span>
        </div>
      </form>

      <div className="login-bottom">Festival Manager</div>
    </div>
  </div>
);

}
