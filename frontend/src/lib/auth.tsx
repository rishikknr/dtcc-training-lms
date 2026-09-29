import { createContext, useContext, type ReactNode } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from './api';
import type { Role, User } from '../types';

type AuthContextValue = {
  user?: User;
  loading: boolean;
  refresh: () => Promise<User | undefined>;
  logout: () => Promise<void>;
  has: (role: Role) => boolean;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ['me'],
    queryFn: () => api<User>('/api/auth/me'),
    retry: false,
    staleTime: 60_000,
  });

  const refresh = async () => {
    const response = await api<{ user: User }>('/api/auth/refresh', { method: 'POST' });
    queryClient.setQueryData(['me'], response.user);
    return response.user;
  };

  const logout = async () => {
    await api('/api/auth/logout', { method: 'POST' });
    queryClient.clear();
  };

  return (
    <AuthContext.Provider
      value={{
        user: query.data,
        loading: query.isLoading,
        refresh,
        logout,
        has: (role) => Boolean(query.data?.roles.includes(role)),
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error('AuthProvider missing');
  return value;
}
