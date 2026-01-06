import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import MainLayout from "./layouts/MainLayout";

import Home from "./pages/Home";
import About from "./pages/About";
import Dashboard from "./pages/Dashboard";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";
import ProgramListPage from "./pages/ProgramListPage";
import ProgramDetailsPage from "./pages/ProgramDetailsPage";
import ProgramsPage from "./pages/ProgramsPage";
import ScreeningsPage from "./pages/ScreeningsPage";
import SubmissionsPage from "./pages/SubmissionsPage";

function UnauthorizedPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
      <div className="max-w-md w-full bg-white border rounded-xl p-6 space-y-2 shadow-sm">
        <h1 className="text-xl font-semibold">Μη εξουσιοδοτημένη πρόσβαση</h1>
        <p className="text-sm text-gray-600">
          Δεν έχεις δικαιώματα για να δεις αυτή τη σελίδα.
        </p>
      </div>
    </div>
  );
}

function NotFoundPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
      <div className="max-w-md w-full bg-white border rounded-xl p-6 space-y-2 shadow-sm">
        <h1 className="text-xl font-semibold">404</h1>
        <p className="text-sm text-gray-600">Η σελίδα δεν βρέθηκε.</p>
      </div>
    </div>
  );
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/unauthorized" element={<UnauthorizedPage />} />

        {/* Public Programs (VISITOR allowed) */}
        <Route path="/programs" element={<ProgramListPage />} />
        <Route path="/programs/:id" element={<ProgramDetailsPage />} />

        {/* Protected (με layout) */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute requiredRoles={["PROGRAMMER", "STAFF", "SUBMITTER"]}>
              <MainLayout>
                <Dashboard />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/profile"
          element={
            <ProtectedRoute requiredRoles={["PROGRAMMER", "STAFF", "SUBMITTER"]}>
              <MainLayout>
                <ProfilePage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/programs/manage"
          element={
            <ProtectedRoute requiredRoles={["PROGRAMMER", "STAFF"]}>
              <MainLayout>
                <ProgramsPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/screenings"
          element={
            <ProtectedRoute requiredRoles={["PROGRAMMER", "STAFF"]}>
              <MainLayout>
                <ScreeningsPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/my-submissions"
          element={
            <ProtectedRoute requiredRoles={["SUBMITTER"]}>
              <MainLayout>
                <SubmissionsPage />
              </MainLayout>
            </ProtectedRoute>
          }
        />

        {/* Default redirects */}
        <Route path="/app" element={<Navigate to="/" replace />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
