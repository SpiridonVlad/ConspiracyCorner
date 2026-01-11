import { createContext, useContext, useState, useCallback, useMemo, ReactNode } from 'react';
import { useMutation, useQuery } from '@apollo/client/react';
import { LOGIN_USER, REGISTER_USER, GET_ME, SET_ANONYMOUS_MODE } from '../graphql/operations';
import { User, LoginRequest, RegisterRequest, AuthResponse } from '../types';

interface MeQueryData {
  me: User | null;
}

interface LoginMutationData {
  login: AuthResponse;
}

interface RegisterMutationData {
  register: AuthResponse;
}

interface SetAnonymousModeMutationData {
  setAnonymousMode: { id: string; anonymousMode: boolean };
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (input: LoginRequest) => Promise<AuthResponse>;
  register: (input: RegisterRequest) => Promise<AuthResponse>;
  logout: () => void;
  toggleAnonymousMode: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [localUser, setLocalUser] = useState<User | null>(null);

  const { data: meData, loading: meLoading, refetch: refetchMe, error: meError } = useQuery<MeQueryData>(GET_ME, {
    skip: !token,
  });

  // Derive user from query data or local state
  const user = useMemo(() => {
    if (meError) {
      return null;
    }
    return meData?.me ?? localUser;
  }, [meData, meError, localUser]);

  const [loginMutation] = useMutation<LoginMutationData>(LOGIN_USER);
  const [registerMutation] = useMutation<RegisterMutationData>(REGISTER_USER);
  const [setAnonymousModeMutation] = useMutation<SetAnonymousModeMutationData>(SET_ANONYMOUS_MODE);

  const login = useCallback(async (input: LoginRequest): Promise<AuthResponse> => {
    const { data } = await loginMutation({ variables: { input } });
    if (!data) throw new Error('Login failed');
    const response = data.login;
    localStorage.setItem('token', response.token);
    setToken(response.token);
    await refetchMe();
    return response;
  }, [loginMutation, refetchMe]);

  const register = useCallback(async (input: RegisterRequest): Promise<AuthResponse> => {
    const { data } = await registerMutation({ variables: { input } });
    if (!data) throw new Error('Registration failed');
    const response = data.register;
    localStorage.setItem('token', response.token);
    setToken(response.token);
    await refetchMe();
    return response;
  }, [registerMutation, refetchMe]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setToken(null);
    setLocalUser(null);
  }, []);

  const toggleAnonymousMode = useCallback(async () => {
    if (!user) return;
    const { data } = await setAnonymousModeMutation({
      variables: { anonymous: !user.anonymousMode },
    });
    if (data) {
      setLocalUser({ ...user, anonymousMode: data.setAnonymousMode.anonymousMode });
    }
  }, [user, setAnonymousModeMutation]);

  // Handle auth errors by clearing token
  const effectiveToken = meError ? null : token;
  if (meError && token) {
    localStorage.removeItem('token');
    // Schedule token clear for next render to avoid setState during render
    setTimeout(() => setToken(null), 0);
  }

  const contextValue = useMemo(() => ({
    user,
    token: effectiveToken,
    isAuthenticated: !!effectiveToken && !!user,
    isLoading: meLoading,
    login,
    register,
    logout,
    toggleAnonymousMode,
  }), [user, effectiveToken, meLoading, login, register, logout, toggleAnonymousMode]);

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
