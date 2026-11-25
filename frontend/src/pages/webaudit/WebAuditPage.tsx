import { translateToFr } from '../../utils/translateToFr';

import { useState, type FormEvent } from 'react';

interface DomAnalysisResult {
  url: string;
  title: string;
  totalElements: number;
  totalImages: number;
  imagesWithoutAlt: number;
  cssFiles: number;
  scripts: number;
  htmlSizeBytes: number;
}

interface CategoryScore {
  id: string;
  title: string;
  score: number;
}

interface Recommendation {
  id: string;
  title: string;
  description: string;
  score: number;
  displayValue: string;
  savingsMs: number | null;
  savingsBytes: number | null;
}

interface WebAuditResult {
  dom: DomAnalysisResult;
  lighthouse: {
    analyzedUrl: string;
    scores: CategoryScore[];
    recommendations: Recommendation[];
    networkMetrics: {
      totalRequests: number | null;
      totalTransferBytes: number | null;
    };
  } | null;
  ecoIndex: {
    score: number;
    grade: string;
    co2Grams: number;
    waterCl: number;
  } | null;
  lighthouseStatus: string;
  lighthouseError: string | null;
}

export function WebAuditPage() {
  const [url, setUrl] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<WebAuditResult | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError('');
    setResult(null);

    let parsedUrl: URL;

    try {
      parsedUrl = new URL(url.trim());
    } catch {
      setError('Veuillez saisir une adresse URL valide.');
      return;
    }

    if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
      setError('Seules les adresses HTTP et HTTPS sont acceptées.');
      return;
    }

    setLoading(true);

    try {
      const token = localStorage.getItem('greenops_token');
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await fetch(
        'http://localhost:8080/api/web-audit/analyze',
        {
          method: 'POST',
          headers,
          body: JSON.stringify({
            url: parsedUrl.toString(),
          }),
        }
      );

      const rawText = await response.text();
      const data = rawText ? JSON.parse(rawText) : {};

      if (!response.ok) {
        throw new Error(
          data.message || `Erreur du serveur (${response.status}).`
        );
      }

      setResult(data as WebAuditResult);
    } catch (err) {
      if (err instanceof TypeError) {
        setError(
          'Impossible de contacter le backend. Vérifiez que Spring Boot est démarré sur le port 8080.'
        );
      } else {
        setError(
          err instanceof Error
            ? err.message
            : "Une erreur est survenue pendant l'audit."
        );
      }
    } finally {
      setLoading(false);
    }
  }

  function formatBytes(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} octets`;
    }

    return `${(bytes / 1024).toFixed(2)} Ko`;
  }

  return (
    <div className="space-y-8">
      <div>
        <p className="text-sm font-medium text-emerald-600">
          GREENOPS / MODULE 1
        </p>

        <h1 className="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
          Audit Web & EcoIndex
        </h1>

        <p className="mt-2 text-slate-500">
          Analysez les performances et l'impact environnemental de votre site.
        </p>
      </div>

      {/* Formulaire */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-900">
        <h2 className="text-xl font-semibold text-slate-900 dark:text-white">
          Lancer un nouvel audit
        </h2>

        <p className="mt-2 text-sm text-slate-500">
          Entrez l'adresse du site Web que vous souhaitez analyser.
        </p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label
              htmlFor="website-url"
              className="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300"
            >
              Adresse du site Web
            </label>

            <input
              id="website-url"
              type="url"
              value={url}
              onChange={(event) => setUrl(event.target.value)}
              placeholder="https://www.exemple.com"
              required
              className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
            />
          </div>

          {error && (
            <p role="alert" className="text-sm text-red-600">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={loading || !url.trim()}
            className="rounded-lg bg-emerald-600 px-6 py-3 font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {loading ? "Analyse en cours..." : "Lancer l'audit"}
          </button>
        </form>
      </div>

      {/* Résultats */}
      {result && (
        <div className="space-y-6">
          <div>
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
              Résultats de l'audit
            </h2>
            <p className="mt-1 break-all text-sm text-slate-500">
              {result.dom.url}
            </p>
          </div>

          {/* EcoIndex */}
          <div className="grid gap-4 md:grid-cols-3">
            <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-900">
              <h3 className="font-semibold text-slate-500">EcoIndex</h3>

              {result.ecoIndex ? (
                <>
                  <p className="mt-2 text-4xl font-bold text-emerald-600">
                    {result.ecoIndex.score}
                    <span className="text-lg"> / 100</span>
                  </p>
                  <p className="mt-2 text-slate-700 dark:text-slate-300">
                    Grade : <strong>{result.ecoIndex.grade}</strong>
                  </p>
                </>
              ) : (
                <p className="mt-3 text-sm text-amber-600">
                  Données insuffisantes pour calculer l'EcoIndex.
                </p>
              )}
            </div>

            <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-900">
              <h3 className="font-semibold text-slate-500">
                Émissions estimées
              </h3>

              {result.ecoIndex ? (
                <p className="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
                  {result.ecoIndex.co2Grams} gCO₂eq
                </p>
              ) : (
                <p className="mt-3 text-sm text-slate-500">Non disponible</p>
              )}
            </div>

            <div className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-900">
              <h3 className="font-semibold text-slate-500">
                Consommation d'eau estimée
              </h3>

              {result.ecoIndex ? (
                <p className="mt-2 text-3xl font-bold text-slate-900 dark:text-white">
                  {result.ecoIndex.waterCl} cl
                </p>
              ) : (
                <p className="mt-3 text-sm text-slate-500">Non disponible</p>
              )}
            </div>
          </div>

          {/* Analyse DOM */}
          <div className="rounded-xl border border-slate-200 bg-white p-6 dark:border-slate-700 dark:bg-slate-900">
            <h3 className="text-lg font-semibold text-slate-900 dark:text-white">
              Analyse technique du site
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              {result.dom.title || 'Titre non disponible'}
            </p>

            <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Metric label="Éléments HTML" value={result.dom.totalElements} />
              <Metric label="Images" value={result.dom.totalImages} />
              <Metric
                label="Images sans attribut alt"
                value={result.dom.imagesWithoutAlt}
              />
              <Metric label="Fichiers CSS" value={result.dom.cssFiles} />
              <Metric label="Scripts JavaScript" value={result.dom.scripts} />
              <Metric
                label="Taille HTML"
                value={formatBytes(result.dom.htmlSizeBytes)}
              />
            </div>
          </div>

          {/* Lighthouse */}
          <div className="rounded-xl border border-slate-200 bg-white p-6 dark:border-slate-700 dark:bg-slate-900">
            <h3 className="text-lg font-semibold text-slate-900 dark:text-white">
              Analyse Google Lighthouse
            </h3>

            {result.lighthouseStatus === 'AVAILABLE' && result.lighthouse ? (
              <>
                <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                  {result.lighthouse.scores.map((category) => (
                    <div
                      key={category.id}
                      className="rounded-lg bg-slate-50 p-4 dark:bg-slate-800"
                    >
                      <p className="text-sm text-slate-500">
                        {translateToFr(category.title)}
                      </p>
                      <p className="mt-1 text-2xl font-bold text-emerald-600">
                        {category.score} / 100
                      </p>
                    </div>
                  ))}
                </div>

                <div className="mt-5 grid gap-4 sm:grid-cols-2">
                  <Metric
                    label="Requêtes réseau"
                    value={
                      result.lighthouse.networkMetrics.totalRequests ?? 'N/D'
                    }
                  />
                  <Metric
                    label="Données transférées"
                    value={
                      result.lighthouse.networkMetrics.totalTransferBytes !==
                      null
                        ? formatBytes(
                            result.lighthouse.networkMetrics.totalTransferBytes
                          )
                        : 'N/D'
                    }
                  />
                </div>

                <h4 className="mt-6 font-semibold text-slate-900 dark:text-white">
                  Recommandations Lighthouse
                </h4>

                {result.lighthouse.recommendations.length > 0 ? (
                  <div className="mt-3 space-y-3">
                    {result.lighthouse.recommendations.map((recommendation) => (
                      <div
                        key={recommendation.id}
                        className="rounded-lg border border-slate-200 p-4 dark:border-slate-700"
                      >
                        <p className="font-medium text-slate-900 dark:text-white">
                          {translateToFr(recommendation.title)}
                        </p>
                        <p className="mt-1 text-sm text-slate-500">
                          {translateToFr(recommendation.description)}
                        </p>
                        {recommendation.displayValue && (
                          <p className="mt-2 text-sm text-amber-600">
                            {translateToFr(recommendation.displayValue)}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="mt-2 text-sm text-slate-500">
                    Aucune recommandation à afficher.
                  </p>
                )}
              </>
            ) : (
              <p className="mt-3 text-sm text-amber-600">
                Analyse Lighthouse indisponible.
                {result.lighthouseError
                  ? ` ${result.lighthouseError}`
                  : ' Vérifiez la configuration de Google PageSpeed.'}
              </p>
            )}
          </div>
        </div>
      )}

      {/* Présentation des fonctionnalités */}
      <div className="grid gap-4 md:grid-cols-3">
        {[
          {
            title: 'EcoIndex',
            description: 'Évaluation de la sobriété environnementale.',
          },
          {
            title: 'Performance',
            description: 'Analyse des performances avec PageSpeed Insights.',
          },
          {
            title: 'Hébergement vert',
            description:
              'Vérification avec Green Web Foundation. Cette fonctionnalité sera connectée séparément.',
          },
        ].map((item) => (
          <div
            key={item.title}
            className="rounded-xl border border-slate-200 bg-white p-5 dark:border-slate-700 dark:bg-slate-900"
          >
            <h3 className="font-semibold text-slate-900 dark:text-white">
              {item.title}
            </h3>
            <p className="mt-2 text-sm text-slate-500">{item.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

function Metric({
  label,
  value,
}: {
  label: string;
  value: string | number;
}) {
  return (
    <div className="rounded-lg bg-slate-50 p-4 dark:bg-slate-800">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-1 text-xl font-semibold text-slate-900 dark:text-white">
        {value}
      </p>
    </div>
  );
}

export default WebAuditPage;
