import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { LogIn, AlertCircle, Sun, Moon } from 'lucide-react';
import { GreenOpsLogo } from '../../components/ui/GreenOpsLogo';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useApp, UserRole } from '../../context/AppContext';
import { apiRequest } from '../../api/apiClient';

interface LoginResponse {
  token: string;
  email: string;
  role: UserRole;
}

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const { login, darkMode, toggleDarkMode } = useApp();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const data = await apiRequest<LoginResponse>('/auth/login', {
        method: 'POST',
        skipAuth: true,
        body: JSON.stringify({ email, password }),
      });
      login(data.token, data.email || email, data.role);
      navigate('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Identifiants invalides.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-[#F8FAFC] dark:bg-slate-950 text-slate-900 dark:text-slate-100 p-6">
      <div className="max-w-6xl w-full mx-auto flex items-center justify-between">
        <Link to="/">
          <GreenOpsLogo />
        </Link>
        <button
          onClick={toggleDarkMode}
          className="p-2 rounded-lg border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-300 cursor-pointer"
        >
          {darkMode ? <Sun size={18} /> : <Moon size={18} />}
        </button>
      </div>

      <div className="max-w-md w-full mx-auto my-auto">
        <Card className="p-8 space-y-6">
          <div className="text-center space-y-1">
            <h1 className="text-2xl font-bold">Connexion à GreenOps</h1>
            <p className="text-sm text-slate-500 dark:text-slate-400">
              Accédez à votre espace de pilotage carbone logiciel
            </p>
          </div>

          {error && (
            <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle size={16} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-700 dark:text-slate-300 mb-1">
                Adresse email
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="prenom.nom@exemple.fr"
                className="w-full px-3.5 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-xs font-medium text-slate-700 dark:text-slate-300">
                  Mot de passe
                </label>
                <Link to="/forgot-password" className="text-xs text-emerald-600 hover:underline">
                  Mot de passe oublié ?
                </Link>
              </div>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-3.5 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>

            <Button type="submit" disabled={loading} className="w-full">
              <LogIn size={16} />
              {loading ? 'Connexion en cours...' : 'Se connecter'}
            </Button>
          </form>

          <p className="text-center text-xs text-slate-500 dark:text-slate-400">
            Pas encore de compte ?{' '}
            <Link to="/register" className="text-emerald-600 font-semibold hover:underline">
              Créer un compte
            </Link>
          </p>
        </Card>
      </div>

      <div className="text-center text-xs text-slate-400">
        GreenOps SaaS • Authentification sécurisée JWT
      </div>
    </div>
  );
}