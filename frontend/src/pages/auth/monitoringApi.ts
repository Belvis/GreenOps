import { apiClient } from "../../api/apiClient";

export type TargetType = "WEB" | "GIT";

export interface Subscription {
  id: number;
  targetType: TargetType;
  targetId: string;
  frequencyCron: string;
  thresholdDelta: number;
  active: boolean;
  lastCheckedAt: string | null;
  lastScore: number | null;
}

export const listSubscriptions = () =>
  apiClient.get<Subscription[]>("/monitoring/subscriptions");

export const createSubscription = (body: {
  targetType: TargetType;
  targetId: string;
  thresholdDelta?: number;
}) => apiClient.post<Subscription>("/monitoring/subscriptions", body);

export const updateSubscription = (
  id: number,
  body: { active?: boolean; thresholdDelta?: number; frequencyCron?: string }
) => apiClient.patch<Subscription>(`/monitoring/subscriptions/${id}`, body);

export const deleteSubscription = (id: number) =>
  apiClient.delete<void>(`/monitoring/subscriptions/${id}`);

export const sendTestEmail = () =>
  apiClient.post<{ message: string }>("/notifications/test-email");