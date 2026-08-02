import { atom } from "jotai";

export type AuthStatus = "INITIALIZING" | "AUTHENTICATING" | "AUTHENTICATED" | "UNAUTHENTICATED";

export interface UserProfile {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: string;
  tenantId?: string;
  createdAt?: string;
}

// Raw Atoms
export const accessTokenAtom = atom<string | null>(null);
export const userProfileAtom = atom<UserProfile | null>(null);
export const authStatusAtom = atom<AuthStatus>("INITIALIZING");
export const sessionExpiryWarningAtom = atom<boolean>(false);

// Derived Atoms
export const isAuthenticatedAtom = atom((get) => get(authStatusAtom) === "AUTHENTICATED");
export const isLoadingAuthAtom = atom((get) => {
  const status = get(authStatusAtom);
  return status === "INITIALIZING" || status === "AUTHENTICATING";
});
