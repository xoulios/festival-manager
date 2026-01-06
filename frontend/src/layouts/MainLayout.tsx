import { ReactNode } from "react";
import Sidebar from "../components/Sidebar";
import TopBar from "../components/TopBar";

export default function MainLayout({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-main">
        <TopBar />
        <main className="app-content">
          <div className="app-container">{children}</div>
        </main>
      </div>
    </div>
  );
}
