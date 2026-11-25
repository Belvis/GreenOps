import { useEffect, useState } from "react";
import { Bell, Mail, Plus, Trash2 } from "lucide-react";
import { Card } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";
import type { Subscription, TargetType } from "./monitoringApi";
import {
  createSubscription,
  deleteSubscription,
  listSubscriptions,
  sendTestEmail,
  updateSubscription,
} from "./monitoringApi";

const inputClass =
  "rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 " +
  "text-slate-900 dark:text-slate-100 px-3 py-2 text-sm focus:outline-none " +
  "focus:ring-2 focus:ring-emerald-500";

export default function AlertPreferencesPanel() {
  const [subs, setSubs] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  const [newType, setNewType] = useState<TargetType>("WEB");
  const [newTarget, setNewTarget] = useState("");
  const [newThreshold, setNewThreshold] = useState(10);

  const errorMessage = (e: unknown) =>
    e instanceof Error ? e.message : "Une erreur est survenue, réessayez.";

  const refresh = async () => {
    try {
      setSubs(await listSubscriptions());
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
  }, []);

  const replace = (updated: Subscription) =>
    setSubs((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));

  const handleToggle = async (s: Subscription) => {
    setError(null);
    try {
      replace(await updateSubscription(s.id, { active: !s.active }));
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  const handleThreshold = async (s: Subscription, value: string) => {
    const n = Number(value);
    if (!Number.isFinite(n) || n < 0.1 || n === s.thresholdDelta) return;
    setError(null);
    try {
      replace(await updateSubscription(s.id, { thresholdDelta: n }));
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  const handleDelete = async (s: Subscription) => {
    setError(null);
    try {
      await deleteSubscription(s.id);
      setSubs((prev) => prev.filter((x) => x.id !== s.id));
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  const handleAdd = async () => {
    if (!newTarget.trim()) return;
    setError(null);
    try {
      const created = await createSubscription({
        targetType: newType,
        targetId: newTarget.trim(),
        thresholdDelta: newThreshold,
      });
      setSubs((prev) => [...prev, created]);
      setNewTarget("");
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  const handleTestEmail = async () => {
    setError(null);
    setInfo(null);
    try {
      setInfo((await sendTestEmail()).message);
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  return (
    <Card className="p-6 space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-2">
          <Bell className="h-5 w-5 text-emerald-600 dark:text-emerald-500" />
          <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100">
            Alertes de dérive par email
          </h3>
        </div>
        <Button variant="secondary" onClick={handleTestEmail}>
          <Mail className="h-4 w-4 mr-2 inline" />
          Envoyer un email de test
        </Button>
      </div>

      {error && (
        <p className="text-sm text-red-600 dark:text-red-400" role="alert">
          {error}
        </p>
      )}
      {info && <p className="text-sm text-emerald-700 dark:text-emerald-400">{info}</p>}

      {loading ? (
        <p className="text-sm text-slate-500 dark:text-slate-400">Chargement…</p>
      ) : subs.length === 0 ? (
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Aucune ressource suivie pour le moment.
        </p>
      ) : (
        <ul className="divide-y divide-slate-200 dark:divide-slate-800">
          {subs.map((s) => (
            <li key={s.id} className="py-3 flex items-center gap-4 flex-wrap">
              <span className="text-xs font-semibold px-2 py-1 rounded bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                {s.targetType}
              </span>
              <span className="flex-1 min-w-[12rem] truncate text-sm text-slate-900 dark:text-slate-100">
                {s.targetId}
              </span>

              <label className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
                Seuil (%)
                <input
                  key={`${s.id}-${s.thresholdDelta}`}
                  type="number"
                  min={0.1}
                  step={0.5}
                  defaultValue={s.thresholdDelta}
                  onBlur={(e) => handleThreshold(s, e.target.value)}
                  className={`${inputClass} w-20`}
                />
              </label>

              <button
                role="switch"
                aria-checked={s.active}
                aria-label={s.active ? "Désactiver l'alerte" : "Activer l'alerte"}
                onClick={() => handleToggle(s)}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                  s.active ? "bg-emerald-500" : "bg-slate-300 dark:bg-slate-700"
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                    s.active ? "translate-x-6" : "translate-x-1"
                  }`}
                />
              </button>

              <Button variant="ghost" onClick={() => handleDelete(s)}>
                <Trash2 className="h-4 w-4" />
              </Button>
            </li>
          ))}
        </ul>
      )}

      <div className="pt-4 border-t border-slate-200 dark:border-slate-800 flex items-end gap-3 flex-wrap">
        <label className="flex flex-col gap-1 text-sm text-slate-600 dark:text-slate-400">
          Type
          <select
            value={newType}
            onChange={(e) => setNewType(e.target.value as TargetType)}
            className={inputClass}
          >
            <option value="WEB">Site web</option>
            <option value="GIT">Dépôt Git</option>
          </select>
        </label>
        <label className="flex flex-col gap-1 text-sm text-slate-600 dark:text-slate-400 flex-1 min-w-[14rem]">
          URL à suivre
          <input
            type="url"
            value={newTarget}
            onChange={(e) => setNewTarget(e.target.value)}
            placeholder="https://exemple.fr"
            className={inputClass}
          />
        </label>
        <label className="flex flex-col gap-1 text-sm text-slate-600 dark:text-slate-400">
          Seuil (%)
          <input
            type="number"
            min={0.1}
            step={0.5}
            value={newThreshold}
            onChange={(e) => setNewThreshold(Number(e.target.value))}
            className={`${inputClass} w-24`}
          />
        </label>
        <Button variant="primary" onClick={handleAdd}>
          <Plus className="h-4 w-4 mr-1 inline" />
          Suivre
        </Button>
      </div>
    </Card>
  );
}
