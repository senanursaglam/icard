import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import LoginPage from "../pages/LoginPage";
import DashboardPage from "../pages/DashboardPage";
import QrScanPage from "../pages/QrScanPage";
import ManagerTeamPage from "../pages/ManagerTeamPage";
import AdminPage from "../pages/AdminPage";
import ChangePasswordPage from "../pages/ChangePasswordPage";

const AppRouter = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
        path="/qr-scan"
        element={
            <ProtectedRoute>
            <QrScanPage />
            </ProtectedRoute>
        }
        />
        <Route
          path="/manager/team"
          element={
            <ProtectedRoute allowedRoles={["MANAGER"]}>
              <ManagerTeamPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/change-password"
          element={
            <ProtectedRoute>
              <ChangePasswordPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRouter;