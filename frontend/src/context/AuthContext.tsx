import { createContext, useContext, useEffect, useMemo, useState, ReactNode } from "react";

export type UserRole = "VISITOR" | "PROGRAMMER" | "STAFF" | "SUBMITTER";

export type FestivalRole = {
  festivalId: number;
  role: string;
};

export interface User {
  userId: number;
  username: string;
  role: UserRole; 
  festivalRoles: FestivalRole[];
  basicAuth: string; 
}

interface AuthContextType {
  user: User | null;
  login: (u: User) => void;
  logout: () => void;
  authHeader: string | undefined;
}

const STORAGE_KEY = "fm_auth_user";

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw) as User;
        if (parsed?.userId && parsed?.username && parsed?.role && parsed?.basicAuth) setUser(parsed);
      }
    } catch {
      // ignore
    }
  }, []);

  const login = (u: User) => {
    setUser(u);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(u));
    localStorage.setItem("role", u.role);
    localStorage.setItem("userId", String(u.userId));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem("role");
    localStorage.removeItem("userId");
  };

  const value = useMemo(
    () => ({
      user,
      login,
      logout,
      authHeader: user?.basicAuth,
    }),
    [user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};
