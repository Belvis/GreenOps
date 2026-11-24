import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Globe, GitBranch, Cloud, Bell, ShieldCheck,
  Sun, Moon, Lock, LogOut
} from 'lucide-react';
import { GreenOpsLogo } from '../ui/GreenOpsLogo';
import { StatusBadge } from '../ui/Badges';
import { useApp, type UserRole } from '../../context/AppContext';

const ROLE_LABELS: Record<UserRole, string> = {
  ROLE_USER: 'Profil Découverte',
  ROLE_DEV: 'Développeur / DevOps',
  ROLE_INFRA: 'Ingénieur Infra / Cloud',
  ROLE_RSE: 'Décideur RSE',
  ROLE_ADMIN: 'Super-Admin SaaS',
};

export function AppLayout() {
  const { darkMode, toggleDarkMode, role, userEmail, logout } = useApp();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { to: '/dashboard', label: 'Tableau de bord', icon: LayoutDashboard, allowed: true, req: '' },
    { to: '/audit-web', label: 'Audit Web & EcoIndex', icon: Globe, allowed: true, req: '' },
    { to: '/git-analyzer', label: 'Analyse Git & CI/CD', icon: GitBranch, allowed: ['ROLE_DEV', 'ROLE_ADMIN'].includes(role), req: 'Développeur' },
    { to: '/cloud-simulator', label: 'Simulateur Cloud & RGPD', icon: Cloud, allowed: ['ROLE_INFRA', 'ROLE_RSE', 'ROLE_ADMIN'].includes(role), req: 'Ingénieur Infra' },
    { to: '/notifications', label: 'Alertes Email (Cron)', icon: Bell, allowed: role !== 'ROLE_USER', req: 'Compte Avancé' },
    { to: '/admin', label: 'Console Admin SaaS', icon: ShieldCheck, allowed: role === 'ROLE_ADMIN', req: 'Super-Admin' },
  ];

  return (
    <div className="min-h-screen flex bg-[#F8FAFC] dark:bg-slate-950 text-slate-900 dark:text-slate-100">
      <aside className="w-64 shrink-0 border-r border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 flex flex-col justify-between">
        <div>
          <div className="h-16 px-6 flex items-center border-b border-slate-200 dark:border-slate-800">
            <Link to="/">
              <GreenOpsLogo />
            </Link>
          </div>

          <nav className="p-4 space-y-1.5">
            {navItems.map((item) => {
              const Icon = item.icon;
              const active = location.pathname === item.to;
              if (!item.allowed) {
                return (
                  <div
                    key={item.to}
                    title={'Rôle requis : ' + item.req}
                    className="flex items-center justify-between px-3 py-2.5 rounded-lg text-sm text-slate-400 dark:text-slate-600 cursor-not-allowed select-none"
                  >
                    <span className="flex items-center gap-3">
                      <Icon size={18} />
                      {item.label}
                    </span>
                    <Lock size={14} />
                  </div>
                );
              }
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={
                    active
                      ? 'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400'
                      : 'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
                  }
                >
                  <Icon size={18} />
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </div>

        <div className="p-4 border-t border-slate-200 dark:border-slate-800 space-y-3">
          <div className="text-xs text-slate-500 dark:text-slate-400 truncate">
            Connecté : <strong className="text-slate-700 dark:text-slate-200">{userEmail}</strong>
          </div>
          <button
            type="button"
            onClick={handleLogout}
            className="w-full flex items-center gap-2 text-xs font-medium text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40 px-3 py-2 rounded-lg transition-colors cursor-pointer"
          >
            <LogOut size={14} /> Se déconnecter
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-16 px-8 border-b border-slate-200 dark:border-slate-800 bg-white/80 dark:bg-slate-900/80 backdrop-blur-xs flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-sm text-slate-500 dark:text-slate-400">Espace de travail</span>
            <StatusBadge type="green" label={ROLE_LABELS[role]} />
          </div>

          <div className="flex items-center gap-4">
            <button
              onClick={toggleDarkMode}
              className="p-2 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 cursor-pointer"
              title="Basculer thème clair / sombre"
            >
              {darkMode ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="text-sm font-medium text-slate-700 dark:text-slate-200">
              {userEmail}
            </div>
          </div>
        </header>

        <main className="flex-1 p-8 max-w-7xl w-full mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}