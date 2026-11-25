export type GdprStatus = 'UE_RGPD' | 'ADEQUAT' | 'HORS_UE_CLOUD_ACT';

export interface CloudRegion {
  zoneCode: string;
  displayName: string;
  country: string;
  pue: number;
  gdprStatus: GdprStatus;
  legalNote: string | null;
}

export interface RegionSimulationResult {
  zoneCode: string;
  displayName: string;
  country: string;
  pue: number;
  gdprStatus: GdprStatus;
  legalNote: string | null;
  carbonIntensityGco2PerKwh: number;
  itPowerKw: number;
  energyKwh: number;
  carbonGrams: number;
  eligibleForRecommendation: boolean;
}

export interface SimulationComparisonResult {
  itPowerKw: number;
  averageCarbonGrams: number;
  recommendedRegion: RegionSimulationResult;
  deltaCarbonGramsVsAverage: number;
  deltaCarbonPercentVsAverage: number;
  rankedRegions: RegionSimulationResult[];
}

export interface SimulatePayload {
  vcpu: number;
  ramGb: number;
  storageGb: number;
  hoursPerDay: number;
  durationDays: number;
  strictRgpd: boolean;
}

export interface SaveFavoritePayload {
  userId?: number | null;
  name: string;
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