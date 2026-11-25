export type {
  ApiErrorResponse,
  CloudSimulation,
  RegionSimulationResult,
  SaveFavoritePayload,
  SimulatePayload,
  SimulationComparisonResult,
} from "../../api/cloudSimApi";
export {
  CloudApiError,
  createFavorite as saveFavorite,
  deleteFavorite,
  fetchFavorites,
  runSimulation,
} from "../../api/cloudSimApi";
import type { ApiErrorResponse } from "../../api/cloudSimApi";
import { CloudApiError } from "../../api/cloudSimApi";
import type { CloudRegion, GdprStatus } from "./types";

const API_BASE = "/api/cloud";

async function parseApiError(response: Response): Promise<CloudApiError> {
  try {
    const data = (await response.json()) as Partial<ApiErrorResponse>;
    return new CloudApiError({
      timestamp: data.timestamp ?? new Date().toISOString(),
      status: data.status ?? response.status,
      error: data.error ?? response.statusText,
      message: data.message ?? `Erreur HTTP ${response.status}`,
    });
  } catch {
    return new CloudApiError({
      timestamp: new Date().toISOString(),
      status: response.status,
      error: response.statusText,
      message: `Erreur HTTP ${response.status}`,
    });
  }
}

export async function fetchRegions(gdprStatus?: GdprStatus): Promise<CloudRegion[]> {
  const query = gdprStatus ? `?gdprStatus=${encodeURIComponent(gdprStatus)}` : "";
  const response = await fetch(`${API_BASE}/regions${query}`);
  if (!response.ok) {
    throw await parseApiError(response);
  }
  return (await response.json()) as CloudRegion[];
}