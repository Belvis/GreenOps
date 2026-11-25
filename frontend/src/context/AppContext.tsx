import React, { createContext, useContext, useEffect, useState } from 'react';

export type UserRole = 'ROLE_USER' | 'ROLE_DEV' | 'ROLE_INFRA' | 'ROLE_RSE' | 'ROLE_ADMIN';

interface StoredUser {
  email: string;
  role: UserRole;
}

interface AppContextType {
  darkMode: boolean;
  toggleDarkMode: () => void;
  token: string | null;
  role: UserRole;
  userEmail: string;
  isAuthenticated: boolean;
  login: (token: string, email: string, role: UserRole) => void;
  logout: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [darkMode, setDarkMode] = useState(false);
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('greenops_token'));
  const [user, setUser] = useState<StoredUser | null>(() => {
    const raw = localStorage.getItem('greenops_user');
    return raw ? (JSON.parse(raw) as StoredUser) : null;
  });

  useEffect(() => {
    document.documentElement.classList.toggle('dark', darkMode);
  }, [darkMode]);

  const login = (newToken: string, email: string, role: UserRole) => {
    const userData: StoredUser = { email, role };
    localStorage.setItem('greenops_token', newToken);
    localStorage.setItem('greenops_user', JSON.stringify(userData));
    setToken(newToken);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('greenops_token');
    localStorage.removeItem('greenops_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AppContext.Provider
      value={{
        darkMode,
        toggleDarkMode: () => setDarkMode((prev) => !prev),
        token,
        role: user?.role ?? 'ROLE_USER',
        userEmail: user?.email ?? '',
        isAuthenticated: Boolean(token && user),
        login,
        logout,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used inside AppProvider');
  return ctx;
}