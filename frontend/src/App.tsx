import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppProvider } from './context/AppContext';
import { AppLayout } from './components/layout/AppLayout';
import { ProtectedRoute } from './components/layout/ProtectedRoute';
import { LandingPage } from './pages/public/LandingPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage';
import { AdminConsolePage } from './pages/admin/AdminConsolePage';

function PlaceholderPage({ title, owner }: { title: string; owner: string }) {
  return (
    <div className="p-8 rounded-xl border border-dashed border-slate-300 dark:border-slate-700 text-center space-y-2">
      <h2 className="text-xl font-bold">{title}</h2>
      <p className="text-sm text-slate-500">Espace réservé au développement de : <strong>{owner}</strong></p>
    </div>
  );
}

export function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          {/* Routes Publiques */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          {/* Espace Connecté protégé par JWT */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/audit-web" element={<PlaceholderPage title="Module 1 : Audit Web & EcoIndex" owner="HAFSAT Marwa" />} />
            <Route
              path="/git-analyzer"
              element={
                <ProtectedRoute allowedRoles={['ROLE_DEV', 'ROLE_ADMIN']}>
                  <PlaceholderPage title="Module 2 : Analyseur Git & CI/CD" owner="AGBOTON Belvis" />
                </ProtectedRoute>
              }
            />
            <Route
              path="/cloud-simulator"
              element={
                <ProtectedRoute allowedRoles={['ROLE_INFRA', 'ROLE_RSE', 'ROLE_ADMIN']}>
                  <PlaceholderPage title="Module 3 : Simulateur Cloud & RGPD" owner="PINEAU Faizi" />
                </ProtectedRoute>
              }
            />
            <Route path="/notifications" element={<PlaceholderPage title="Module 4 : Alertes Email & Cron" owner="RAMALINGAME Shiamamadanki" />} />
            <Route
              path="/admin"
              element={
                <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
                  <AdminConsolePage />
                </ProtectedRoute>
              }
            />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}

export default App;