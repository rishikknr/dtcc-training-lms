import {createContext,useContext,ReactNode} from 'react';import {useQuery,useQueryClient} from '@tanstack/react-query';import {api} from './api';import type {User} from '../types';
type Auth={user?:User;loading:boolean;refresh:()=>Promise<unknown>;logout:()=>Promise<void>;has:(role:string)=>boolean};
const Context=createContext<Auth|null>(null);
export function AuthProvider({children}:{children:ReactNode}){const query=useQuery({queryKey:['me'],queryFn:()=>api<User>('/api/auth/me'),retry:false,staleTime:60_000});const client=useQueryClient();const logout=async()=>{await api('/api/auth/logout',{method:'POST'});client.setQueryData(['me'],null)};return <Context.Provider value={{user:query.data,loading:query.isLoading,refresh:query.refetch,logout,has:r=>!!query.data?.roles.includes(r as never)}}>{children}</Context.Provider>}
export function useAuth(){const v=useContext(Context);if(!v)throw new Error('AuthProvider missing');return v}

