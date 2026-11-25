export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}

export class CloudApiError extends Error {
  public readonly status: number;
  public readonly error: string;
  public readonly timestamp: string;

  constructor(payload: ApiErrorResponse) {
    super(payload.message);
    this.name = "CloudApiError";
    this.status = payload.status;
    this.error = payload.error;
    this.timestamp = payload.timestamp;
  }

  get isValidationError(): boolean {
    return this.status === 400;
  }

  get isServiceUnavailable(): boolean {
    return this.status === 503;
  }
}

export interface SimulatePayload {
  vcpu: number;
  ramGb: number;
  storageGb: number;
  hoursPerDay: number;
  durationDays: number;
  strictRgpd?: boolean;
}

export interface RegionSimulationResult {
  zoneCode: string;
  displayName: string;
  country: string;
  pue: number;
  gdprStatus: string;
  legalNote?: string;
  carbonIntensityGco2PerKwh: number;
  itPowerKw: number;
  energyKwh: number;
  carbonGrams: number;
  eligibleForRecommendation: boolean;
}

export interface SimulationComparisonResult {
  itPowerKw: number;
  averageCarbonGrams: number;
  recommended: RegionSimulationResult;
  deltaGrams: number;
  deltaPercent: number;
  results: RegionSimulationResult[];
}

export interface SaveFavoritePayload {
  userId?: number | null;
  name?: string | null;
  vcpu: number;
  ramGb: number;
  storageGb: number;
  hoursPerDay: number;
  durationDays: number;
}

export interface CloudSimulation {
  id: number;
  userId: number | null;
  name: string;
  vcpu: number;
  ramGb: number;
  storageGb: number;
  hoursPerDay: number;
  durationDays: number;
  favorite: boolean;
  createdAt: string;
}

const API_BASE = "/api/cloud";

async function parseError(response: Response): Promise<CloudApiError> {
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

export async function runSimulation(payload: SimulatePayload): Promise<SimulationComparisonResult> {
  const response = await fetch(`${API_BASE}/simulate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw await parseError(response);
  }
  return (await response.json()) as SimulationComparisonResult;
}

export async function fetchFavorites(userId?: number): Promise<CloudSimulation[]> {
  const url = userId != null
    ? `${API_BASE}/favorites?userId=${encodeURIComponent(String(userId))}`
    : `${API_BASE}/favorites`;
  const response = await fetch(url);
  if (!response.ok) {
    throw await parseError(response);
  }
  return (await response.json()) as CloudSimulation[];
}

export async function createFavorite(payload: SaveFavoritePayload): Promise<CloudSimulation> {
  const response = await fetch(`${API_BASE}/favorites`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw await parseError(response);
  }
  return (await response.json()) as CloudSimulation;
}

export async function deleteFavorite(id: number): Promise<void> {
  const response = await fetch(`${API_BASE}/favorites/${id}`, {
    method: "DELETE",
  });
  if (!response.ok && response.status !== 404) {
    throw await parseError(response);
  }
}