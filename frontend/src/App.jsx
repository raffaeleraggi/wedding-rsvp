import { Routes, Route, Navigate } from "react-router-dom";
import DashboardPage from "./pages/DashboardPage.jsx";
import InvitePage from "./pages/InvitePage.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/admin" replace />} />
      <Route path="/admin" element={<DashboardPage />} />
      <Route path="/i/:token" element={<InvitePage />} />
    </Routes>
  );
}
