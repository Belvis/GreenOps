import React, { useEffect, useState } from 'react';
import { Shield, Users, Activity, Sliders, CheckCircle2, AlertCircle } from 'lucide-react';
import { apiClient } from '../../api/apiClient';
import { type UserRole } from '../../context/AppContext';

interface AdminUser {
  id: number;
  email: string;
  role: UserRole;
  active: boolean;
  status?: string;
  createdAt?: string;
}

interface QuotaItem {
  provider: string;
  usedToday: number;
  dailyLimit: number;
}

interface CoefficientItem {
  paramKey: string;
  paramValue: number;
  unit: string;
  description: string;
}

const ROLES: UserRole[] = ['ROLE_USER', 'ROLE_DEV', 'ROLE_INFRA', 'ROLE_RSE', 'ROLE_ADMIN'];

const ROLE_LABELS: Record<UserRole, string> = {
  ROLE_USER: 'Utilisateur (Découverte)',
  ROLE_DEV: 'Développeur CI/CD',
  ROLE_INFRA: 'Ingénieur Cloud / Infra',
  ROLE_RSE: 'Décideur RSE',
  ROLE_ADMIN: 'Super-Admin SaaS',
};

export function AdminConsolePage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [quotas, setQuotas] = useState<QuotaItem[]>([]);
  const [coefficients, setCoefficients] = useState<CoefficientItem[]>([]);
  const [editedValues, setEditedValues] = useState<Record<string, number>>({});
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const loadAdminData = async () => {
    setLoading(true);
    setError('');
    try {
      const [usersRes, quotasRes, coeffsRes] = await Promise.all([
        apiClient.get<AdminUser[]>('/admin/users').catch(() => []),
        apiClient.get<QuotaItem[]>('/admin/quotas').catch(() => [
          { provider: 'Google PageSpeed Insights', usedToday: 14, dailyLimit: 25000 },
          { provider: 'The Green Web Foundation', usedToday: 22, dailyLimit: 10000 },
          { provider: 'Electricity Maps API', usedToday: 38, dailyLimit: 5000 },
        ]),
        apiClient.get<CoefficientItem[]>('/admin/coefficients').catch(() => [
          { paramKey: 'DEFAULT_PUE', paramValue: 1.2, unit: 'ratio', description: 'PUE moyen des datacenters' },
          { paramKey: 'DEFAULT_CARBON_INTENSITY', paramValue: 300, unit: 'gCO2eq/kWh', description: 'Intensité carbone par défaut' },
        ]),
      ]);
      setUsers(Array.isArray(usersRes) ? usersRes : []);
      setQuotas(Array.isArray(quotasRes) ? quotasRes : []);
      const coeffList = Array.isArray(coeffsRes) ? coeffsRes : [];
      setCoefficients(coeffList);
      const mapVals: Record<string, number> = {};
      coeffList.forEach((c) => {
        mapVals[c.paramKey] = c.paramValue;
      });
      setEditedValues(mapVals);
    } catch (err: any) {
      setError(err.message || "Erreur lors du chargement des données d'administration.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  const notify = (msg: string) => {
    setMessage(msg);
    setTimeout(() => setMessage(''), 3500);
  };

  const handleRoleChange = async (userId: number, newRole: UserRole) => {
    try {
      await apiClient.patch(`/admin/users/${userId}/role`, { role: newRole });
      setUsers((prev) => prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u)));
      notify('Rôle mis à jour avec succès.');
    } catch (err: any) {
      setError(err.message || 'Impossible de modifier le rôle.');
    }
  };

  const handleToggleStatus = async (user: AdminUser) => {
    const isCurrentlyActive = user.active ?? user.status === 'ACTIVE';
    const nextActive = !isCurrentlyActive;
    try {
      await apiClient.patch(`/admin/users/${user.id}/status`, { active: nextActive });
      setUsers((prev) =>
        prev.map((u) =>
          u.id === user.id
            ? { ...u, active: nextActive, status: nextActive ? 'ACTIVE' : 'SUSPENDED' }
            : u
        )
      );
      notify(nextActive ? 'Compte activé.' : 'Compte suspendu.');
    } catch (err: any) {
      setError(err.message || 'Impossible de mettre à jour le statut.');
    }
  };

  const handleSaveCoefficient = async (paramKey: string) => {
    const val = editedValues[paramKey];
    try {
      await apiClient.put(`/admin/coefficients/${paramKey}`, { paramValue: val });
      setCoefficients((prev) =>
        prev.map((c) => (c.paramKey === paramKey ? { ...c, paramValue: val } : c))
      );
      notify(`Paramètre ${paramKey} enregistré.`);
    } catch (err: any) {
      setError(err.message || 'Erreur lors de la mise à jour du coefficient.');
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Shield className="w-6 h-6 text-emerald-600" />
            Console Super-Administrateur
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Attribution des rôles applicatifs, supervision des quotas d&apos;API tierces et calibration des facteurs carbone.
          </p>
        </div>
      </div>

      {message && (
        <div className="flex items-center gap-2 p-3.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 text-sm">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {error && (
        <div className="flex items-center gap-2 p-3.5 rounded-xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 text-sm">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Quotas API */}
      <section className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 space-y-4">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-white flex items-center gap-2">
          <Activity className="w-5 h-5 text-emerald-600" />
          Supervision des Quotas d&apos;API Externes
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {quotas.map((q) => {
            const pct = q.dailyLimit > 0 ? Math.min(100, Math.round((q.usedToday / q.dailyLimit) * 100)) : 0;
            return (
              <div key={q.provider} className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 space-y-2">
                <div className="flex items-center justify-between text-xs font-semibold uppercase text-slate-500">
                  <span>{q.provider}</span>
                  <span className="text-emerald-600">{pct} %</span>
                </div>
                <div className="text-xl font-bold text-slate-900 dark:text-white">
                  {q.usedToday} <span className="text-xs font-normal text-slate-400">/ {q.dailyLimit} appels</span>
                </div>
                <div className="w-full h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                  <div className="h-full bg-emerald-500 rounded-full" style={{ width: `${pct}%` }} />
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* Utilisateurs & Rôles */}
      <section className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 space-y-4">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-white flex items-center gap-2">
          <Users className="w-5 h-5 text-emerald-600" />
          Utilisateurs &amp; Attribution des Rôles SaaS
        </h2>
        {loading ? (
          <p className="text-sm text-slate-500">Chargement des comptes...</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-sm">
              <thead>
                <tr className="border-b border-slate-200 dark:border-slate-800 text-xs uppercase text-slate-500">
                  <th className="py-3 px-4">Email</th>
                  <th className="py-3 px-4">Statut</th>
                  <th className="py-3 px-4">Rôle Attribué</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                {users.map((u) => {
                  const isActive = u.active ?? u.status === 'ACTIVE';
                  return (
                    <tr key={u.id}>
                      <td className="py-3 px-4 font-medium text-slate-900 dark:text-white">{u.email}</td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            isActive
                              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300'
                              : 'bg-amber-50 text-amber-700 dark:bg-amber-950 dark:text-amber-300'
                          }`}
                        >
                          {isActive ? 'Actif' : 'En attente / Suspendu'}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <select
                          value={u.role}
                          onChange={(e) => handleRoleChange(u.id, e.target.value as UserRole)}
                          className="px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-950 text-slate-900 dark:text-white"
                        >
                          {ROLES.map((r) => (
                            <option key={r} value={r}>
                              {ROLE_LABELS[r]}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          type="button"
                          onClick={() => handleToggleStatus(u)}
                          className="px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs font-medium"
                        >
                          {isActive ? 'Suspendre' : 'Activer'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Coefficients Carbone */}
      <section className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 space-y-4">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-white flex items-center gap-2">
          <Sliders className="w-5 h-5 text-emerald-600" />
          Coefficients Système de Calcul Carbone
        </h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-sm">
            <thead>
              <tr className="border-b border-slate-200 dark:border-slate-800 text-xs uppercase text-slate-500">
                <th className="py-3 px-4">Clé Paramètre</th>
                <th className="py-3 px-4">Description</th>
                <th className="py-3 px-4">Valeur</th>
                <th className="py-3 px-4">Unité</th>
                <th className="py-3 px-4 text-right">Enregistrer</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {coefficients.map((c) => (
                <tr key={c.paramKey}>
                  <td className="py-3 px-4 font-mono text-xs font-semibold">{c.paramKey}</td>
                  <td className="py-3 px-4 text-slate-600 dark:text-slate-400">{c.description}</td>
                  <td className="py-3 px-4">
                    <input
                      type="number"
                      step="any"
                      value={editedValues[c.paramKey] ?? c.paramValue}
                      onChange={(e) =>
                        setEditedValues((prev) => ({ ...prev, [c.paramKey]: parseFloat(e.target.value) }))
                      }
                      className="w-28 px-2.5 py-1 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-950"
                    />
                  </td>
                  <td className="py-3 px-4 text-slate-500">{c.unit}</td>
                  <td className="py-3 px-4 text-right">
                    <button
                      type="button"
                      onClick={() => handleSaveCoefficient(c.paramKey)}
                      className="px-3.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold"
                    >
                      Enregistrer
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}