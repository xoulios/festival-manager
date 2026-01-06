import { Link, useNavigate } from "react-router-dom";
import { useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext";

export default function TopBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const initials = useMemo(() => {
    const u = user?.username?.trim();
    return u ? u.charAt(0).toUpperCase() : "?";
  }, [user?.username]);

  const handleLogout = () => {
    setOpen(false);
    logout();
    navigate("/", { replace: true });
  };

  return (
    <header className="app-topbar">
      <div className="app-topbar__inner">
        <Link to="/dashboard" className="app-topbar__brand">
          Festival Manager <span className="app-topbar__muted">Frontend</span>
        </Link>

        <div className="app-topbar__actions">
          {!user && (
            <Link to="/login" className="btn-primary">
              Σύνδεση
            </Link>
          )}

          {user && (
            <div style={{ position: "relative" }}>
              <button type="button" className="user-btn" onClick={() => setOpen((v) => !v)}>
                <div className="user-pill">{initials}</div>
                <span className="user-name">
                  {user.username} ({user.role})
                </span>
                <span className="user-caret">▾</span>
              </button>

              {open && (
                <div className="user-menu">
                  <button onClick={() => { setOpen(false); navigate("/profile"); }}>
                    Προφίλ
                  </button>
                  <button onClick={handleLogout}>Αποσύνδεση</button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
