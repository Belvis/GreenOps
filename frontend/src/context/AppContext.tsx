import React, { createContext, useContext, useEffect, useState } from 'react';

export type UserRole = 'ROLE_USER' | 'ROLE_DEV' | 'ROLE_INFRA' | 'ROLE_RSE' | 'ROLE_ADMIN';

interface AppContextType {
  darkMode: boolean;
  toggleDarkMode: () => void;
  role: UserRole;
  setRole: (role: UserRole) => void;
  userEmail: string;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [darkMode, setDarkMode] = useState(false);
  const [role, setRole] = useState<UserRole>('ROLE_ADMIN');

  useEffect(() => {
    document.documentElement.classList.toggle('dark', darkMode);
  }, [darkMode]);

  return (
    <AppContext.Provider
      value={{
        darkMode,
        toggleDarkMode: () => setDarkMode((prev) => !prev),
        role,
        setRole,
        userEmail: 'demo@greenops.io',
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
