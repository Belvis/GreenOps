const API_BASE_URL = 'http://localhost:8080/api';

export interface ApiRequestOptions extends RequestInit {
  skipAuth?: boolean;
}

export async function apiRequest<T>(endpoint: string, options: ApiRequestOptions = {}): Promise<T> {
  const { skipAuth = false, headers, ...rest } = options;
  const token = localStorage.getItem('greenops_token');

  const mergedHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(headers as Record<string, string>),
  };

  if (!skipAuth && token) {
    mergedHeaders['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...rest,
    headers: mergedHeaders,
  });

  if (response.status === 401 && !skipAuth) {
    localStorage.removeItem('greenops_token');
    localStorage.removeItem('greenops_user');
    window.location.href = '/login';
    throw new Error('Session expirée. Veuillez vous reconnecter.');
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = data?.error || data?.message || `Erreur serveur (${response.status})`;
    throw new Error(message);
  }

  return data as T;
}