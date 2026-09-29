import { useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { BookOpen, LayoutDashboard, LogOut, Menu, ShieldCheck, UserRound, X } from 'lucide-react';
import { useAuth } from '../lib/auth';

export default function Layout() {
  const [open, setOpen] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const signOut = async () => { await logout(); navigate('/'); };
  const close = () => setOpen(false);

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-40 border-b border-stone-200/80 bg-cream/90 backdrop-blur-xl">
        <div className="container-page flex h-18 items-center justify-between py-4">
          <Link to="/" className="flex items-center gap-2 font-display text-xl font-bold"><span className="grid h-9 w-9 place-items-center rounded-xl bg-forest text-white"><BookOpen size={19} /></span>academy<span className="text-coral">.</span></Link>
          <nav className="hidden items-center gap-7 md:flex"><NavLink to="/courses" className="text-sm font-medium hover:text-forest">Explore</NavLink>{user && <NavLink to="/dashboard" className="text-sm font-medium hover:text-forest">Dashboard</NavLink>}{user?.roles.includes('ADMIN') && <NavLink to="/admin" className="text-sm font-medium hover:text-forest">Admin</NavLink>}{user?.roles.includes('INSTRUCTOR') && <NavLink to="/instructor/reviews" className="text-sm font-medium hover:text-forest">Reviews</NavLink>}</nav>
          <div className="hidden items-center gap-3 md:flex">{user ? <><Link to="/profile" className="flex items-center gap-2 rounded-full bg-white px-3 py-2 text-sm font-medium"><UserRound size={16} />{user.displayName.split(' ')[0]}</Link><button onClick={signOut} className="rounded-full p-2 hover:bg-white" aria-label="Sign out"><LogOut size={18} /></button></> : <><Link to="/login" className="btn px-4">Sign in</Link><Link to="/register" className="btn-primary">Start learning</Link></>}</div>
          <button onClick={() => setOpen(!open)} className="md:hidden" aria-label="Toggle menu">{open ? <X /> : <Menu />}</button>
        </div>
        {open && <div className="container-page flex flex-col gap-3 border-t py-5 md:hidden"><Link onClick={close} to="/courses">Explore courses</Link>{user ? <><Link onClick={close} to="/dashboard"><LayoutDashboard className="mr-2 inline" size={17} />Dashboard</Link>{user.roles.includes('ADMIN') && <Link onClick={close} to="/admin"><ShieldCheck className="mr-2 inline" size={17} />Admin console</Link>}<Link onClick={close} to="/profile">Profile</Link><button className="text-left" onClick={signOut}>Sign out</button></> : <><Link onClick={close} to="/login">Sign in</Link><Link onClick={close} to="/register" className="btn-primary">Start learning</Link></>}</div>}
      </header>
      <main><Outlet /></main>
      <footer className="mt-24 border-t border-stone-200 bg-white"><div className="container-page grid gap-8 py-12 md:grid-cols-[1fr_auto_auto]"><div><div className="font-display text-xl font-bold">academy<span className="text-coral">.</span></div><p className="mt-2 max-w-sm text-sm text-stone-500">Purposeful learning for people building a safer, smarter world.</p></div><div><p className="text-sm font-semibold">Learn</p><Link to="/courses" className="mt-3 block text-sm text-stone-500">Course catalog</Link></div><div><p className="text-sm font-semibold">Teach</p><Link to="/become-instructor" className="mt-3 block text-sm text-stone-500">Become an instructor</Link></div></div></footer>
    </div>
  );
}
