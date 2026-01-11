import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import {
  LayoutDashboard,
  Film,
  CalendarDays,
  FileText,
  User,
  Menu,
  X,
} from "lucide-react";
import { useMemo, useState } from "react";

const links = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard, roles: ["ALL"] },
  { to: "/programs/manage", label: "Προγράμματα", icon: Film, roles: ["PROGRAMMER", "STAFF"] },
  { to: "/screenings", label: "Προβολές", icon: CalendarDays, roles: ["PROGRAMMER", "STAFF"] },
  { to: "/my-submissions", label: "Οι Υποβολές μου", icon: FileText, roles: ["SUBMITTER"] },
  { to: "/profile", label: "Προφίλ", icon: User, roles: ["ALL"] },
  { to: "/my-assignments", label: "Οι Αναθέσεις μου", icon: FileText, roles: ["STAFF"] },
];

export default function Sidebar() {
  const { user } = useAuth();
  const location = useLocation();
  const [open, setOpen] = useState(true);

  const role = user?.role;

  const visibleLinks = useMemo(() => {
    return links.filter(({ roles }) => roles.includes("ALL") || (role && roles.includes(role)));
  }, [role]);

  return (
    <aside className={`app-sidebar ${open ? "is-open" : "is-collapsed"}`}>
      <div className="app-sidebar__header">
        <Link to="/dashboard" className="app-sidebar__brand">
          <div className="app-sidebar__logo">FM</div>
          {open && (
            <div className="app-sidebar__brandtext">
              <div className="app-sidebar__title">Festival Manager</div>
              <div className="app-sidebar__subtitle">App</div>
            </div>
          )}
        </Link>

        <button className="icon-btn" onClick={() => setOpen((v) => !v)} aria-label="Toggle sidebar">
          {open ? <X size={18} /> : <Menu size={18} />}
        </button>
      </div>

      <nav className="app-nav">
        {visibleLinks.map(({ to, label, icon: Icon }) => {
          const isActive = location.pathname === to;
          return (
            <Link key={to} to={to} className={`app-nav-link ${isActive ? "is-active" : ""}`}>
              <Icon className="app-nav-icon" />
              {open && <span className="app-nav-text">{label}</span>}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
