import { useEffect, useState } from 'react';
import { ShieldCheck, Users, Activity, Sliders, Save, AlertCircle, CheckCircle2 } from 'lucide-react';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/Badges';
import { type UserRole } from '../../context/AppContext';
import { apiRequest } from '../../api/apiClient';

interface AdminUser {
  id: number;
  email: string;
  role: UserRole;
  active: boolean;
  createdAt?: string;
}

interface ApiQuota {
  provider: string;
  usedToday: number;
  dailyLimit: number;
}

interface SystemCoefficient {
  paramKey: string;
  paramValue: number;
  unit: string;
  description: string;
}

const ALL_ROLES: { value: UserRole; label: string }[] = [
  { value: 'ROLE_USER', label: 'Profil Découverte' },
  { value: 'ROLE_DEV', label: 'Développeur / DevOps' },
  { value: 'ROLE_INFRA', label: 'Ingénieur Infra / Cloud' },
  { value: 'ROLE_RSE', label: 'Décideur RSE' },
  { value: 'ROLE_ADMIN', label: 'Super-Admin SaaS' },
];

export function AdminConsolePage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [quotas, setQuotas] = useState<ApiQuota[]>([]);
  const [coefficients, setCoefficients] = useState<SystemCoefficient[]>([]);
  const [editedValues, setEditedValues] = useState<Record<string, string>>({});
  const [feedback, setFeedback] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);
  const [loading, setLoading] = useState(true);

  const loadAdminData = async () => {
    setLoading(true);
    try {
      const [uData, qData, cData] = await Promise.all([
        apiRequest<AdminUser[]>('/admin/users'),
        apiRequest<ApiQuota[]>('/admin/quotas'),
        apiRequest<SystemCoefficient[]>('/admin/coefficients'),
      ]);
      setUsers(uData || []);
      setQuotas(qData || []);
      setCoefficients(cData || []);

      const initialEdits: Record<string, string> = {};
      (cData || []).forEach((c) => {
        initialEdits[c.paramKey] = String(c.paramValue);
      });
      setEditedValues(initialEdits);
    } catch (err) {
      setFeedback({
        type: 'err',
        text: err instanceof Error ? err.message : 'Erreur lors du chargement des données Super-Admin.',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  const handleRoleChange = async (userId: number, newRole: UserRole) => {
    setFeedback(null);
    try {
      await apiRequest(`/admin/users/${userId}/role`, {
        method: 'PATCH',
        body: JSON.stringify({ role: newRole }),
      });
      setUsers((prev) => prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u)));
      setFeedback({ type: 'ok', text: 'Rôle utilisateur mis à jour avec succès.' });
    } catch (err) {
      setFeedback({ type: 'err', text: err instanceof Error ? err.message : 'Impossible de modifier le rôle.' });
    }
  };

  const handleToggleStatus = async (user: AdminUser) => {
    setFeedback(null);
    const nextStatus = !user.active;
    try {
      await apiRequest(`/admin/users/${user.id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ active: nextStatus }),
      });
      setUsers((prev) => prev.map((u) => (u.id === user.id ? { ...u, active: nextStatus } : u)));
      setFeedback({
        type: 'ok',
        text: nextStatus ? `Compte ${user.email} activé.` : `Compte ${user.email} suspendu.`,
      });
    } catch (err) {
      setFeedback({ type: 'err', text: err instanceof Error ? err.message : 'Erreur de mise à jour du statut.' });
    }
  };

  const handleSaveCoefficient = async (key: string) => {
    setFeedback(null);
    const numVal = parseFloat(editedValues[key]);
    if (Number.isNaN(numVal)) {
      setFeedback({ type: 'err', text: 'Veuillez saisir une valeur numérique valide.' });
      return;
    }

    try {
      await apiRequest(`/admin/coefficients/${key}`, {
        method: 'PUT',
        body: JSON.stringify({ paramValue: numVal }),
      });
      setFeedback({ type: 'ok', text: `Coefficient "${key}" mis à jour (${numVal}).` });
    } catch (err) {
      setFeedback({ type: 'err', text: err instanceof Error ? err.message : 'Erreur lors de la sauvegarde.' });
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300 mb-2">
          <ShieldCheck size={14} />
          Module 0 — Gouvernance SaaS & RBAC
        </div>
        <h1 className="text-2xl font-bold tracking-tight">Console Super-Administrateur</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          Attribution des rôles applicatifs, supervision des quotas d'API tierces et calibration des facteurs carbone.
        </p>
      </div>

      {feedback && (
        <div
          className={`p-3.5 rounded-lg border text-xs flex items-center gap-2 ${
            feedback.type === 'ok'
              ? 'bg-emerald-50 dark:bg-emerald-950/50 border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300'
              : 'bg-rose-50 dark:bg-rose-950/50 border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300'
          }`}
        >
          {feedback.type === 'ok' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
          <span>{feedback.text}</span>
        </div>
      )}

      {/* SECTION 1 : QUOTAS DES APIS EXTERNES */}
      <div className="space-y-3">
        <h2 className="text-base font-bold flex items-center gap-2">
          <Activity size={18} className="text-emerald-600" />
          Supervision des Quotas d'API Externes
        </h2>
        <div className="grid sm:grid-cols-3 gap-4">
          {quotas.length === 0 && !loading ? (
            <Card className="p-5 sm:col-span-3 text-xs text-slate-500">
              Aucune métrique de quota remontée par le serveur pour le moment.
            </Card>
          ) : (
            quotas.map((q) => {
              const pct = q.dailyLimit > 0 ? Math.min(100, Math.round((q.usedToday / q.dailyLimit) * 100)) : 0;
              return (
                <Card key={q.provider} className="p-5 space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase text-slate-500">{q.provider}</span>
                    <span className="text-xs font-semibold text-emerald-600">{pct} %</span>
                  </div>
                  <div className="text-lg font-bold">
                    {q.usedToday} <span className="text-xs font-normal text-slate-400">/ {q.dailyLimit} appels</span>
                  </div>
                  <div className="w-full h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full ${pct > 80 ? 'bg-rose-500' : 'bg-emerald-500'}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </Card>
              );
            })
          )}
        </div>
      </div>

      {/* SECTION 2 : GESTION DES UTILISATEURS & RÔLES RBAC */}
      <Card className="p-6 space-y-4">
        <h2 className="text-base font-bold flex items-center gap-2">
          <Users size={18} className="text-emerald-600" />
          Utilisateurs & Attribution des Rôles SaaS
        </h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 dark:border-slate-800 text-xs text-slate-500 uppercase">
                <th className="py-2.5 px-3">Email</th>
                <th className="py-2.5 px-3">Statut</th>
                <th className="py-2.5 px-3">Rôle attribué</th>
                <th className="py-2.5 px-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40">
                  <td className="py-3 px-3 font-medium">{u.email}</td>
                  <td className="py-3 px-3">
                    {u.active ? (
                      <StatusBadge type="green" label="Actif" />
                    ) : (
                      <StatusBadge type="warning" label="Suspendu" />
                    )}
                  </td>
                  <td className="py-3 px-3">
                    <select
                      value={u.role}
                      onChange={(e) => handleRoleChange(u.id, e.target.value as UserRole)}
                      className="text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 px-2.5 py-1.5"
                    >
                      {ALL_ROLES.map((r) => (
                        <option key={r.value} value={r.value}>
                          {r.label}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td className="py-3 px-3 text-right">
                    <Button
                      size="sm"
                      variant={u.active ? 'secondary' : 'primary'}
                      onClick={() => handleToggleStatus(u)}
                    >
                      {u.active ? 'Suspendre' : 'Activer'}
                    </Button>
                  </td>
                </tr>
              ))}
              {users.length === 0 && !loading && (
                <tr>
                  <td colSpan={4} className="py-6 text-center text-xs text-slate-500">
                    Aucun compte enregistré.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>

      {/* SECTION 3 : COEFFICIENTS DE CALCUL CARBONE */}
      <Card className="p-6 space-y-4">
        <h2 className="text-base font-bold flex items-center gap-2">
          <Sliders size={18} className="text-emerald-600" />
          Coefficients Système de Calcul Carbone
        </h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 dark:border-slate-800 text-xs text-slate-500 uppercase">
                <th className="py-2.5 px-3">Clé paramètre</th>
                <th className="py-2.5 px-3">Description</th>
                <th className="py-2.5 px-3">Valeur</th>
                <th className="py-2.5 px-3">Unité</th>
                <th className="py-2.5 px-3 text-right">Enregistrer</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {coefficients.map((coef) => (
                <tr key={coef.paramKey}>
                  <td className="py-3 px-3 font-mono text-xs font-semibold">{coef.paramKey}</td>
                  <td className="py-3 px-3 text-xs text-slate-600 dark:text-slate-400">{coef.description}</td>
                  <td className="py-3 px-3">
                    <input
                      type="number"
                      step="any"
                      value={editedValues[coef.paramKey] ?? ''}
                      onChange={(e) =>
                        setEditedValues((prev) => ({ ...prev, [coef.paramKey]: e.target.value }))
                      }
                      className="w-28 px-2.5 py-1 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800"
                    />
                  </td>
                  <td className="py-3 px-3 text-xs text-slate-500">{coef.unit}</td>
                  <td className="py-3 px-3 text-right">
                    <Button size="sm" onClick={() => handleSaveCoefficient(coef.paramKey)}>
                      <Save size={14} /> Sauvegarder
                    </Button>
                  </td>
                </tr>
              ))}
              {coefficients.length === 0 && !loading && (
                <tr>
                  <td colSpan={5} className="py-6 text-center text-xs text-slate-500">
                    Aucun coefficient configurable chargé depuis le back-end.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}