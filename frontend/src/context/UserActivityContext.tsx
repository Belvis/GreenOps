import React, { createContext, useContext, useEffect, useState } from 'react';
import { useApp } from './AppContext';

export type ActivityModule = 'AUDIT_WEB' | 'GIT_CI' | 'CLOUD_SIM';

export interface ActivityItem {
  id: string;
  module: ActivityModule;
  title: string;
  subtitle: string;
  route: string;
  createdAt: string;
  isFavorite: boolean;
  isDownloaded: boolean;
  downloadedAt?: string;
  payload: unknown;
}

interface UserActivityContextType {
  items: ActivityItem[];
  recentItems: ActivityItem[];
  favoriteItems: ActivityItem[];
  downloadedItems: ActivityItem[];
  recordSearch: (entry: {
    module: ActivityModule;
    title: string;
    subtitle: string;
    route: string;
    payload: unknown;
    favorite?: boolean;
  }) => string;
  toggleFavorite: (id: string) => void;
  downloadItem: (id: string) => void;
  removeItem: (id: string) => void;
  clearHistory: () => void;
}

const UserActivityContext = createContext<UserActivityContextType | undefined>(undefined);

function storageKeyForEmail(email: string): string {
  const clean = (email || 'guest').trim().toLowerCase();
  return `greenops_activity_${clean}`;
}

export function triggerJsonDownload(filename: string, data: unknown) {
  const jsonStr = JSON.stringify(data, null, 2);
  const blob = new Blob([jsonStr], { type: 'application/json;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export function UserActivityProvider({ children }: { children: React.ReactNode }) {
  const { userEmail } = useApp();
  const storageKey = storageKeyForEmail(userEmail);

  const [items, setItems] = useState<ActivityItem[]>(() => {
    try {
      const raw = localStorage.getItem(storageKey);
      return raw ? (JSON.parse(raw) as ActivityItem[]) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    try {
      const raw = localStorage.getItem(storageKey);
      setItems(raw ? (JSON.parse(raw) as ActivityItem[]) : []);
    } catch {
      setItems([]);
    }
  }, [storageKey]);

  useEffect(() => {
    try {
      localStorage.setItem(storageKey, JSON.stringify(items));
    } catch {
      // Ignorer les erreurs de quota localStorage
    }
  }, [items, storageKey]);

  const recordSearch: UserActivityContextType['recordSearch'] = ({
    module,
    title,
    subtitle,
    route,
    payload,
    favorite = false,
  }) => {
    const id = `${module}_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`;
    const now = new Date().toISOString();
    setItems((prev) => {
      const existingSame = prev.find(
        (x) => x.module === module && x.title === title && x.subtitle === subtitle
      );
      const keepFavorite = existingSame ? existingSame.isFavorite || favorite : favorite;
      const keepDownloaded = existingSame ? existingSame.isDownloaded : false;
      const keepDownloadedAt = existingSame ? existingSame.downloadedAt : undefined;
      const filtered = prev.filter((x) => x.id !== existingSame?.id);
      const newEntry: ActivityItem = {
        id,
        module,
        title,
        subtitle,
        route,
        createdAt: now,
        isFavorite: keepFavorite,
        isDownloaded: keepDownloaded,
        downloadedAt: keepDownloadedAt,
        payload,
      };
      return [newEntry, ...filtered].slice(0, 60);
    });
    return id;
  };

  const toggleFavorite = (id: string) => {
    setItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, isFavorite: !item.isFavorite } : item))
    );
  };

  const downloadItem = (id: string) => {
    const target = items.find((i) => i.id === id);
    if (!target) return;
    const safeSlug = target.title
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-|-$/g, '');
    triggerJsonDownload(`greenops-${target.module.toLowerCase()}-${safeSlug || 'export'}.json`, {
      exportedAt: new Date().toISOString(),
      userEmail: userEmail || 'utilisateur',
      module: target.module,
      title: target.title,
      subtitle: target.subtitle,
      result: target.payload,
    });
    const now = new Date().toISOString();
    setItems((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, isDownloaded: true, downloadedAt: now } : item
      )
    );
  };

  const removeItem = (id: string) => {
    setItems((prev) => prev.filter((item) => item.id !== id));
  };

  const clearHistory = () => {
    setItems((prev) => prev.filter((item) => item.isFavorite));
  };

  const recentItems = items;
  const favoriteItems = items.filter((item) => item.isFavorite);
  const downloadedItems = items.filter((item) => item.isDownloaded);

  return (
    <UserActivityContext.Provider
      value={{
        items,
        recentItems,
        favoriteItems,
        downloadedItems,
        recordSearch,
        toggleFavorite,
        downloadItem,
        removeItem,
        clearHistory,
      }}
    >
      {children}
    </UserActivityContext.Provider>
  );
}

export function useUserActivity() {
  const ctx = useContext(UserActivityContext);
  if (!ctx) throw new Error('useUserActivity must be used inside UserActivityProvider');
  return ctx;
}