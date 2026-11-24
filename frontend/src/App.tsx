import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppProvider } from './context/AppContext';
import { AppLayout } from './components/layout/AppLayout';
import { LandingPage } from './pages/public/LandingPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { WebAuditPage } from './pages/webaudit/WebAuditPage';

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
          <Route path="/login" element={<PlaceholderPage title="Connexion (JWT)" owner="RAMALINGAME Shiamamadanki" />} />
          <Route path="/register" element={<PlaceholderPage title="Inscription" owner="RAMALINGAME Shiamamadanki" />} />

          {/* Espace Connecté avec Sidebar fixe */}
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/audit-web" element={<WebAuditPage />} />
            <Route path="/git-analyzer" element={<PlaceholderPage title="Module 2 : Analyseur Git & CI/CD" owner="AGBOTON Belvis" />} />
            <Route path="/cloud-simulator" element={<PlaceholderPage title="Module 3 : Simulateur Cloud & RGPD" owner="PINEAU Faizi" />} />
            <Route path="/notifications" element={<PlaceholderPage title="Module 4 : Alertes Email & Cron" owner="RAMALINGAME Shiamamadanki" />} />
            <Route path="/admin" element={<PlaceholderPage title="Module 0 : Console Super-Admin SaaS" owner="RAMALINGAME Shiamamadanki" />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}

export default App;
