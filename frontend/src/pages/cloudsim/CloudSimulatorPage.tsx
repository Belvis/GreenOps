import React, { useEffect, useState } from "react";
import { useApp } from "../../context/AppContext";
import {
  CloudApiError,
  deleteFavorite,
  fetchFavorites,
  fetchRegions,
  runSimulation,
  saveFavorite,
} from "./api";
import type {
  CloudSimulation,
  RegionSimulationResult,
  SimulationComparisonResult,
} from "./api";
import type { CloudRegion, GdprStatus } from "./types";

interface UiErrorState {
  status: number;
  error: string;
  message: string;
  timestamp?: string;
}

function deriveUserIdFromEmail(email: string): number {
  let hash = 0;
  for (let i = 0; i < email.length; i += 1) {
    hash = (hash * 31 + email.charCodeAt(i)) % 100000;
  }
  return Math.max(1, hash);
}

function formatGdprBadge(status: GdprStatus): string {
  switch (status) {
    case "UE_RGPD":
      return "UE / RGPD";
    case "ADEQUAT":
      return "Adéquation RGPD";
    case "HORS_UE_CLOUD_ACT":
      return "Hors UE (CLOUD Act)";
  }
}

function toUiError(err: unknown, fallbackMessage: string): UiErrorState {
  if (err instanceof CloudApiError) {
    return {
      status: err.status,
      error: err.error,
      message: err.message,
      timestamp: err.timestamp,
    };
  }
  return {
    status: 500,
    error: "Error",
    message: err instanceof Error ? err.message : fallbackMessage,
  };
}

interface EmissionsChartProps {
  results: RegionSimulationResult[];
  recommendedZoneCode?: string;
}

const EmissionsBarChart: React.FC<EmissionsChartProps> = ({ results, recommendedZoneCode }) => {
  if (results.length === 0) {
    return null;
  }

  const maxCarbon = Math.max(...results.map((r) => r.carbonGrams), 1);
  const barWidth = 72;
  const gap = 28;
  const chartHeight = 180;
  const width = Math.max(360, results.length * (barWidth + gap) + 40);

  return (
    <div className="cloudsim-chart" aria-label="Comparatif des émissions carbone par région">
      <h3>Émissions estimées par région (gCO2eq)</h3>
      <svg
        viewBox={`0 0 ${width} ${chartHeight + 54}`}
        width="100%"
        height={chartHeight + 54}
        role="img"
      >
        {results.map((item, idx) => {
          const ratio = Math.max(0, item.carbonGrams) / maxCarbon;
          const height = Math.round(ratio * (chartHeight - 24));
          const x = 20 + idx * (barWidth + gap);
          const y = chartHeight - height;
          const isRecommended = item.zoneCode === recommendedZoneCode;
          const fill = isRecommended
            ? "#16a34a"
            : item.eligibleForRecommendation
              ? "#0284c7"
              : "#94a3b8";

          return (
            <g key={item.zoneCode} transform={`translate(${x}, 0)`}>
              <title>
                {`${item.displayName} (${item.zoneCode}) : ${item.carbonGrams.toFixed(1)} gCO2eq | PUE ${item.pue.toFixed(2)} | ${item.carbonIntensityGco2PerKwh.toFixed(1)} g/kWh`}
              </title>
              <text
                x={barWidth / 2}
                y={Math.max(12, y - 6)}
                textAnchor="middle"
                fontSize="11"
                fontWeight="600"
              >
                {item.carbonGrams.toFixed(0)}
              </text>
              <rect
                x={0}
                y={y}
                width={barWidth}
                height={Math.max(2, height)}
                rx={4}
                fill={fill}
              />
              <text
                x={barWidth / 2}
                y={chartHeight + 16}
                textAnchor="middle"
                fontSize="11"
                fontWeight={isRecommended ? "700" : "400"}
              >
                {item.zoneCode}
              </text>
              <text
                x={barWidth / 2}
                y={chartHeight + 31}
                textAnchor="middle"
                fontSize="10"
              >
                {isRecommended ? "Recommandé" : item.eligibleForRecommendation ? "Éligible" : "Exclu"}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
};

export const CloudSimulatorPage: React.FC = () => {
  const { userEmail } = useApp();
  const currentUserId = deriveUserIdFromEmail(userEmail);

  const [vcpu, setVcpu] = useState<number>(4);
  const [ramGb, setRamGb] = useState<number>(16);
  const [storageGb, setStorageGb] = useState<number>(250);
  const [hoursPerDay, setHoursPerDay] = useState<number>(24);
  const [durationDays, setDurationDays] = useState<number>(30);
  const [strictRgpd, setStrictRgpd] = useState<boolean>(true);
  const [favoriteName, setFavoriteName] = useState<string>("Cluster API Production");

  const [regions, setRegions] = useState<CloudRegion[]>([]);
  const [favorites, setFavorites] = useState<CloudSimulation[]>([]);
  const [simulation, setSimulation] = useState<SimulationComparisonResult | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [savingFav, setSavingFav] = useState<boolean>(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [apiError, setApiError] = useState<UiErrorState | null>(null);

  async function loadInitialData() {
    setApiError(null);
    try {
      const [loadedRegions, loadedFavorites] = await Promise.all([
        fetchRegions(),
        fetchFavorites(currentUserId),
      ]);
      setRegions(loadedRegions);
      setFavorites(loadedFavorites);
    } catch (err: unknown) {
      setApiError(toUiError(err, "Erreur de chargement initial"));
    }
  }

  useEffect(() => {
    void loadInitialData();
  }, [currentUserId]);

  async function handleSimulate(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setApiError(null);
    try {
      const res = await runSimulation({
        vcpu,
        ramGb,
        storageGb,
        hoursPerDay,
        durationDays,
        strictRgpd,
      });
      setSimulation(res);
    } catch (err: unknown) {
      setApiError(toUiError(err, "Erreur lors de la simulation"));
    } finally {
      setLoading(false);
    }
  }

  async function handleSaveFavorite() {
    setSavingFav(true);
    setApiError(null);
    try {
      const created = await saveFavorite({
        userId: currentUserId,
        name: favoriteName.trim() || `Simulation ${vcpu}vCPU / ${ramGb}Go`,
        vcpu,
        ramGb,
        storageGb,
        hoursPerDay,
        durationDays,
      });
      setFavorites((prev) => [created, ...prev]);
    } catch (err: unknown) {
      setApiError(toUiError(err, "Impossible d'enregistrer le favori"));
    } finally {
      setSavingFav(false);
    }
  }

  async function handleDeleteFavorite(id: number) {
    setDeletingId(id);
    setApiError(null);
    try {
      await deleteFavorite(id);
      setFavorites((prev) => prev.filter((item) => item.id !== id));
    } catch (err: unknown) {
      setApiError(toUiError(err, "Impossible de supprimer le favori"));
    } finally {
      setDeletingId(null);
    }
  }

  function applyFavorite(fav: CloudSimulation) {
    setApiError(null);
    setVcpu(fav.vcpu);
    setRamGb(fav.ramGb);
    setStorageGb(fav.storageGb);
    setHoursPerDay(fav.hoursPerDay);
    setDurationDays(fav.durationDays);
    setFavoriteName(fav.name);
  }

  const recommended: RegionSimulationResult | undefined = simulation?.recommended;
  const rankedList: RegionSimulationResult[] = simulation?.results ?? [];
  const deltaGrams: number = simulation?.deltaGrams ?? 0;
  const deltaPercent: number = simulation?.deltaPercent ?? 0;

  const alertKind =
    apiError?.status === 400
      ? "validation"
      : apiError?.status === 503
        ? "unavailable"
        : "generic";

  return (
    <section className="cloudsim-page">
      <header>
        <h1>Simulateur Cloud et Conformité RGPD</h1>
        <p>
          Comparez l&apos;empreinte énergétique et carbone des régions cloud tout en respectant les
          contraintes juridiques RGPD / CLOUD Act. Connecté en tant que{" "}
          <strong>{userEmail}</strong> (#{currentUserId}).
        </p>
      </header>

      {apiError && (
        <div
          role="alert"
          data-error-kind={alertKind}
          className={`cloudsim-alert cloudsim-alert--${alertKind}`}
        >
          <strong>
            {apiError.status === 400 && "Paramètre invalide (400) : "}
            {apiError.status === 503 && "Service indisponible (503) : "}
            {apiError.status !== 400 && apiError.status !== 503 && `Erreur (${apiError.status}) : `}
          </strong>
          <span>{apiError.message}</span>
          {apiError.status === 503 && (
            <button type="button" onClick={() => void loadInitialData()}>
              Réessayer
            </button>
          )}
        </div>
      )}

      <form onSubmit={handleSimulate} className="cloudsim-form">
        <label>
          vCPU
          <input
            type="number"
            min={0}
            step={1}
            value={vcpu}
            onChange={(e) => setVcpu(Number(e.target.value))}
            required
          />
        </label>

        <label>
          RAM (Go)
          <input
            type="number"
            min={0}
            step={0.5}
            value={ramGb}
            onChange={(e) => setRamGb(Number(e.target.value))}
            required
          />
        </label>

        <label>
          Stockage NVMe (Go)
          <input
            type="number"
            min={0}
            step={10}
            value={storageGb}
            onChange={(e) => setStorageGb(Number(e.target.value))}
            required
          />
        </label>

        <label>
          Heures / jour
          <input
            type="number"
            min={0}
            max={24}
            step={0.5}
            value={hoursPerDay}
            onChange={(e) => setHoursPerDay(Number(e.target.value))}
            required
          />
        </label>

        <label>
          Durée (jours)
          <input
            type="number"
            min={0}
            step={1}
            value={durationDays}
            onChange={(e) => setDurationDays(Number(e.target.value))}
            required
          />
        </label>

        <label className="cloudsim-checkbox">
          <input
            type="checkbox"
            checked={strictRgpd}
            onChange={(e) => setStrictRgpd(e.target.checked)}
          />
          Mode RGPD strict (exclut Hors UE / CLOUD Act de la recommandation)
        </label>

        <button type="submit" disabled={loading}>
          {loading ? "Simulation en cours..." : "Simuler les régions"}
        </button>
      </form>

      <div className="cloudsim-favorite-bar">
        <input
          type="text"
          value={favoriteName}
          onChange={(e) => setFavoriteName(e.target.value)}
          placeholder="Nom de la configuration favorite"
        />
        <button type="button" onClick={() => void handleSaveFavorite()} disabled={savingFav}>
          {savingFav ? "Enregistrement..." : "Sauvegarder en favori"}
        </button>
      </div>

      {simulation && (
        <div className="cloudsim-results">
          <div className="cloudsim-summary">
            <div>
              <strong>Puissance IT estimée :</strong> {simulation.itPowerKw.toFixed(4)} kW
            </div>
            {recommended && (
              <div>
                <strong>Région recommandée :</strong> {recommended.displayName} (
                {recommended.zoneCode}) - {recommended.carbonGrams.toFixed(1)} gCO2eq
              </div>
            )}
            <div>
              <strong>Écart vs moyenne :</strong> {deltaGrams.toFixed(1)} gCO2eq (
              {deltaPercent.toFixed(1)} %)
            </div>
          </div>

          <EmissionsBarChart
            results={rankedList}
            recommendedZoneCode={recommended?.zoneCode}
          />

          <table>
            <thead>
              <tr>
                <th>Région</th>
                <th>Statut RGPD</th>
                <th>PUE</th>
                <th>Intensité (g/kWh)</th>
                <th>Énergie (kWh)</th>
                <th>Carbone (gCO2eq)</th>
                <th>Éligible reco</th>
              </tr>
            </thead>
            <tbody>
              {rankedList.map((item) => (
                <tr key={item.zoneCode}>
                  <td>
                    {item.displayName} ({item.zoneCode})
                  </td>
                  <td>{formatGdprBadge(item.gdprStatus as GdprStatus)}</td>
                  <td>{item.pue.toFixed(2)}</td>
                  <td>{item.carbonIntensityGco2PerKwh.toFixed(1)}</td>
                  <td>{item.energyKwh.toFixed(2)}</td>
                  <td>{item.carbonGrams.toFixed(1)}</td>
                  <td>{item.eligibleForRecommendation ? "Oui" : "Non"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <aside className="cloudsim-side">
        <h2>Favoris enregistrés ({favorites.length})</h2>
        <ul>
          {favorites.map((fav) => (
            <li key={fav.id}>
              <span>
                {fav.name} - {fav.vcpu} vCPU, {fav.ramGb} Go RAM, {fav.storageGb}Go
              </span>
              <button type="button" onClick={() => applyFavorite(fav)}>
                Charger
              </button>
              <button
                type="button"
                disabled={deletingId === fav.id}
                onClick={() => void handleDeleteFavorite(fav.id)}
              >
                {deletingId === fav.id ? "Suppression..." : "Supprimer"}
              </button>
            </li>
          ))}
        </ul>

        <h2>Référentiel des régions ({regions.length})</h2>
        <ul>
          {regions.map((reg) => (
            <li key={reg.zoneCode}>
              <strong>{reg.zoneCode}</strong> - {reg.displayName} ({formatGdprBadge(reg.gdprStatus)},
              PUE {reg.pue.toFixed(2)})
            </li>
          ))}
        </ul>
      </aside>
    </section>
  );
};

export default CloudSimulatorPage;