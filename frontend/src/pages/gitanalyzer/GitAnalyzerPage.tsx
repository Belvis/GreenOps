import React, { useEffect, useState } from 'react';
import { useUserActivity, triggerJsonDownload } from '../../context/UserActivityContext';
import {
  GitBranch,
  Play,
  Zap,
  CloudRain,
  AlertTriangle,
  CheckCircle2,
  Clock,
  Lightbulb,
  History,
  Server
} from 'lucide-react';

interface CiRunEnriched {
  externalRunId: string;
  workflowName: string;
  status: string;
  conclusion: string;
  durationSeconds: number;
  runnerOs: string;
  energyKwh: number;
  co2Grams: number;
  createdAt: string;
}

interface GitAnalysisResult {
  provider: 'GITHUB' | 'GITLAB';
  owner: string;
  repo: string;
  normalizedUrl: string;
  analyzedRunsCount: number;
  totalDurationSeconds: number;
  totalEnergyKwh: number;
  totalCo2Grams: number;
  failureRatePercent: number;
  runs: CiRunEnriched[];
  recommendations: string[];
}

interface GitHistoryItem {
  id: number;
  provider: 'GITHUB' | 'GITLAB';
  owner: string;
  repo: string;
  normalizedUrl: string;
  runsCount: number;
  totalEnergyKwh: number;
  totalCo2Grams: number;
  failureRatePercent: number;
  lastAnalyzedAt: string;
  recommendations: string[];
}

const API_BASE = 'http://localhost:8080/api/git';

export const GitAnalyzerPage: React.FC = () => {
  const { recordSearch, toggleFavorite, downloadItem, items } = useUserActivity();
  const [lastGitId, setLastGitId] = useState<string | null>(null);
  const [repoUrl, setRepoUrl] = useState('https://github.com/spring-projects/spring-boot');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<GitAnalysisResult | null>(null);
  const [history, setHistory] = useState<GitHistoryItem[]>([]);

  const fetchHistory = async () => {
    try {
      const res = await fetch(`${API_BASE}/history`);
      if (res.ok) {
        const data: GitHistoryItem[] = await res.json();
        setHistory(data);
      }
    } catch {
      // Silencieux si le backend n'est pas encore démarré
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleAnalyze = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const token = localStorage.getItem('greenops_token');
      const headers: Record<string, string> = { 'Content-Type': 'application/json' };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await fetch(`${API_BASE}/analyze`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ url: repoUrl })
      });

      const rawText = await response.text();
      const payload = rawText ? JSON.parse(rawText) : null;

      if (!response.ok) {
        throw new Error(payload?.error || payload?.message || `Erreur HTTP ${response.status}`);
      }

      setResult(payload);
      const aid = recordSearch({ module: 'GIT_CI', title: (payload?.owner || '') + '/' + (payload?.repo || payload?.repoName || 'Dépôt Git'), subtitle: String(payload?.totalEnergyKwh ?? 0) + ' kWh • ' + String(payload?.totalCo2Grams ?? 0) + ' gCO2eq', route: '/git-analyzer', payload });
      setLastGitId(aid);
      await fetchHistory();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur réseau vers le serveur GreenOps.');
    } finally {
      setLoading(false);
    }
  };

  const maxRunCo2 = result && result.runs.length > 0
    ? Math.max(...result.runs.map((r) => r.co2Grams), 0.01)
    : 1;

  return (
    <div className="space-y-6">
      {/* En-tête du module */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300 mb-2">
            <GitBranch className="w-3.5 h-3.5" />
            Module 2 — Analyseur CI/CD & Dépôts Publics
          </div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
            Empreinte Carbone du Code & des Pipelines CI/CD
          </h1>
          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
            Analysez sans token les workflows publics GitHub Actions ou GitLab CI et identifiez votre dette écologique.
          </p>
        </div>
      </div>

      {/* Formulaire de saisie d'URL HTTPS */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm">
        <form onSubmit={handleAnalyze} className="space-y-4">
          <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
            URL publique HTTPS du dépôt (GitHub ou GitLab)
          </label>
          <div className="flex flex-col sm:flex-row gap-3">
            <input
              type="url"
              required
              value={repoUrl}
              onChange={(e) => setRepoUrl(e.target.value)}
              placeholder="https://github.com/owner/repository"
              className="flex-1 px-4 py-2.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 disabled:opacity-60 text-white font-medium text-sm transition-colors cursor-pointer"
            >
              <Play className="w-4 h-4" />
              {loading ? 'Analyse en cours...' : 'Lancer l’analyse CI/CD'}
            </button>
          </div>

          {/* Exemples rapides */}
          <div className="flex flex-wrap items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
            <span>Exemples rapides :</span>
            <button
              type="button"
              onClick={() => setRepoUrl('https://github.com/spring-projects/spring-boot')}
              className="underline hover:text-emerald-600 cursor-pointer"
            >
              spring-projects/spring-boot (GitHub)
            </button>
            <span>•</span>
            <button
              type="button"
              onClick={() => setRepoUrl('https://github.com/vitejs/vite')}
              className="underline hover:text-emerald-600 cursor-pointer"
            >
              vitejs/vite (GitHub)
            </button>
            <span>•</span>
            <button
              type="button"
              onClick={() => setRepoUrl('https://gitlab.com/gitlab-org/gitlab-runner')}
              className="underline hover:text-emerald-600 cursor-pointer"
            >
              gitlab-org/gitlab-runner (GitLab)
            </button>
          </div>
        </form>

        {error && (
          <div className="mt-4 p-3.5 rounded-lg bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 text-sm flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}
      </div>

      {/* Résultats de l'analyse */}
      {result && (
        <div className="space-y-6">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/70 p-4">
          <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
            Enregistrer cette analyse dans vos favoris ou télécharger le résultat JSON.
          </span>
          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => {
                if (lastGitId) {
                  toggleFavorite(lastGitId);
                } else {
                  const id = recordSearch({
                    module: 'GIT_CI',
                    title: repoUrl || 'Analyse Git & CI/CD',
                    subtitle: 'Analyse CI/CD',
                    route: '/git-analyzer',
                    payload: { repoUrl },
                    favorite: true,
                  });
                  setLastGitId(id);
                }
              }}
              className="px-3 py-1.5 rounded-lg text-xs font-medium border border-amber-300 bg-amber-50 text-amber-800 hover:bg-amber-100 cursor-pointer"
            >
              {items.find((i) => i.id === lastGitId)?.isFavorite ? 'Dans les favoris' : 'Ajouter aux favoris'}
            </button>
            <button
              type="button"
              onClick={() => {
                if (lastGitId) {
                  downloadItem(lastGitId);
                } else {
                  const id = recordSearch({
                    module: 'GIT_CI',
                    title: repoUrl || 'Analyse Git & CI/CD',
                    subtitle: 'Analyse CI/CD',
                    route: '/git-analyzer',
                    payload: { repoUrl },
                  });
                  setLastGitId(id);
                  triggerJsonDownload('greenops-git-ci.json', { repoUrl });
                }
              }}
              className="px-3 py-1.5 rounded-lg text-xs font-medium border border-emerald-300 bg-emerald-50 text-emerald-800 hover:bg-emerald-100 cursor-pointer"
            >
              Télécharger
            </button>
          </div>
        </div>
          {/* Cartes KPI */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5">
              <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 text-xs font-medium">
                <span>DÉPÔT & FOURNISSEUR</span>
                <Server className="w-4 h-4 text-emerald-600" />
              </div>
              <div className="mt-2 text-lg font-bold text-slate-900 dark:text-white truncate">
                {result.owner}/{result.repo}
              </div>
              <div className="mt-1 text-xs font-semibold text-emerald-600 dark:text-emerald-400">
                {result.provider} • {result.analyzedRunsCount} runs analysés
              </div>
            </div>

            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5">
              <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 text-xs font-medium">
                <span>ÉNERGIE CONSOMMÉE</span>
                <Zap className="w-4 h-4 text-amber-500" />
              </div>
              <div className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
                {result.totalEnergyKwh.toFixed(4)} <span className="text-sm font-normal">kWh</span>
              </div>
              <div className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                Durée cumulée : {result.totalDurationSeconds}s
              </div>
            </div>

            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5">
              <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 text-xs font-medium">
                <span>EMPREINTE CARBONE</span>
                <CloudRain className="w-4 h-4 text-sky-500" />
              </div>
              <div className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
                {result.totalCo2Grams.toFixed(2)} <span className="text-sm font-normal">gCO₂eq</span>
              </div>
              <div className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                PUE 1.20 • Intensité 300 g/kWh
              </div>
            </div>

            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5">
              <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 text-xs font-medium">
                <span>TAUX D’ÉCHEC CI/CD</span>
                <AlertTriangle className="w-4 h-4 text-rose-500" />
              </div>
              <div className="mt-2 text-2xl font-bold text-slate-900 dark:text-white">
                {result.failureRatePercent.toFixed(1)} %
              </div>
              <div className="mt-1 text-xs text-slate-500 dark:text-slate-400">
                {result.failureRatePercent > 20 ? 'Gaspillage CPU détecté' : 'Stabilité satisfaisante'}
              </div>
            </div>
          </div>

          {/* Graphique temporel des runs & Recommandations */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Graphique en barres des derniers runs */}
            <div className="lg:col-span-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6">
              <h2 className="text-base font-semibold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                <Clock className="w-4 h-4 text-emerald-600" />
                Consommation par exécution de pipeline (gCO₂eq)
              </h2>

              {result.runs.length === 0 ? (
                <p className="text-sm text-slate-500">Aucun pipeline public récent trouvé sur ce dépôt.</p>
              ) : (
                <div className="space-y-3">
                  {result.runs.map((run) => {
                    const widthPct = Math.max(6, Math.round((run.co2Grams / maxRunCo2) * 100));
                    const isFail = (run.conclusion || '').toLowerCase().includes('fail');
                    return (
                      <div key={run.externalRunId} className="space-y-1">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-medium text-slate-700 dark:text-slate-300 truncate max-w-[55%]">
                            {run.workflowName} ({run.runnerOs})
                          </span>
                          <span className="text-slate-500 dark:text-slate-400">
                            {run.durationSeconds}s • <strong>{run.co2Grams.toFixed(2)} gCO₂</strong>
                          </span>
                        </div>
                        <div className="w-full h-2.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                          <div
                            className={`h-full rounded-full ${isFail ? 'bg-rose-500' : 'bg-emerald-500'}`}
                            style={{ width: `${widthPct}%` }}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Panneau des recommandations d'optimisation */}
            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6">
              <h2 className="text-base font-semibold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
                <Lightbulb className="w-4 h-4 text-amber-500" />
                Dette Écologique & Pistes d’Optimisation
              </h2>
              <ul className="space-y-3">
                {result.recommendations.map((tip, idx) => (
                  <li
                    key={idx}
                    className="p-3 rounded-lg bg-emerald-50/70 dark:bg-emerald-950/30 border border-emerald-200/70 dark:border-emerald-800/50 text-xs text-slate-700 dark:text-slate-300 flex items-start gap-2.5"
                  >
                    <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                    <span>{tip}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      )}

      {/* Historique PostgreSQL des analyses */}
      {history.length > 0 && (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-6">
          <h2 className="text-base font-semibold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
            <History className="w-4 h-4 text-emerald-600" />
            Historique des dépôts analysés
          </h2>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 dark:border-slate-800 text-xs text-slate-500 uppercase">
                  <th className="py-2.5 px-3">Dépôt</th>
                  <th className="py-2.5 px-3">Fournisseur</th>
                  <th className="py-2.5 px-3">Runs</th>
                  <th className="py-2.5 px-3">Énergie (kWh)</th>
                  <th className="py-2.5 px-3">Carbone (gCO₂eq)</th>
                  <th className="py-2.5 px-3">Échecs</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                {history.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                    <td className="py-2.5 px-3 font-medium text-slate-900 dark:text-white">
                      {item.owner}/{item.repo}
                    </td>
                    <td className="py-2.5 px-3 text-xs">{item.provider}</td>
                    <td className="py-2.5 px-3">{item.runsCount}</td>
                    <td className="py-2.5 px-3">{item.totalEnergyKwh.toFixed(4)}</td>
                    <td className="py-2.5 px-3 font-semibold text-emerald-600">
                      {item.totalCo2Grams.toFixed(2)} g
                    </td>
                    <td className="py-2.5 px-3">{item.failureRatePercent.toFixed(1)} %</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};