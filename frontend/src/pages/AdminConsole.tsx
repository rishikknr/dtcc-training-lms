import { useState } from 'react';
import { BookOpen, Check, ExternalLink, FileClock, FolderTree, Search, ShieldCheck, UserCog, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { api, ApiError } from '../lib/api';
import type { AdminUser, Category, CourseSummary, InstructorApplication, Page, Role } from '../types';

type Tab = 'applications' | 'users' | 'courses' | 'categories' | 'audit';
type AuditLog = { id: number; actorId?: string; action: string; resourceType: string; resourceId?: string; ipAddress?: string; createdAt: string };

export default function AdminConsole() {
  const [tab, setTab] = useState<Tab>('applications');
  return (
    <section className="container-page py-14">
      <p className="eyebrow">Platform governance</p>
      <h1 className="mt-3 font-display text-4xl font-bold">Admin console</h1>
      <div className="mt-8 flex flex-wrap gap-2" role="tablist">
        <TabButton active={tab === 'applications'} onClick={() => setTab('applications')}><ShieldCheck size={17} /> Instructor applications</TabButton>
        <TabButton active={tab === 'users'} onClick={() => setTab('users')}><UserCog size={17} /> Users</TabButton>
        <TabButton active={tab === 'courses'} onClick={() => setTab('courses')}><BookOpen size={17} /> Courses</TabButton>
        <TabButton active={tab === 'categories'} onClick={() => setTab('categories')}><FolderTree size={17} /> Categories</TabButton>
        <TabButton active={tab === 'audit'} onClick={() => setTab('audit')}><FileClock size={17} /> Audit log</TabButton>
      </div>
      <div className="mt-6">
        {tab === 'applications' && <Applications />}
        {tab === 'users' && <Users />}
        {tab === 'courses' && <Courses />}
        {tab === 'categories' && <Categories />}
        {tab === 'audit' && <AuditHistory />}
      </div>
    </section>
  );
}

function TabButton({ active, onClick, children }: { active: boolean; onClick: () => void; children: React.ReactNode }) {
  return <button role="tab" aria-selected={active} onClick={onClick} className={active ? 'btn-primary' : 'btn-secondary'}>{children}</button>;
}

function Applications() {
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ['admin', 'instructor-applications'],
    queryFn: () => api<Page<InstructorApplication>>('/api/instructor-applications?status=PENDING&size=50'),
  });
  const decide = useMutation({
    mutationFn: ({ id, status, note }: { id: string; status: 'APPROVED' | 'REJECTED'; note?: string }) =>
      api(`/api/instructor-applications/${id}/decision`, { method: 'PATCH', body: JSON.stringify({ status, note }) }),
    onSuccess: () => {
      toast.success('Application decision saved');
      queryClient.invalidateQueries({ queryKey: ['admin'] });
    },
    onError: (error: ApiError) => toast.error(error.message),
  });

  if (!query.data?.content.length) return <EmptyPanel title="No applications waiting" message="New instructor requests will appear here for review." />;
  return (
    <div className="space-y-4">
      {query.data.content.map((application) => (
        <article key={application.id} className="card p-6">
          <div className="flex flex-wrap items-start justify-between gap-5">
            <div>
              <h2 className="font-display text-2xl font-semibold">{application.applicantName}</h2>
              <p className="mt-1 text-sm text-stone-500">{application.applicantEmail}</p>
            </div>
            {application.portfolioUrl && <a href={application.portfolioUrl} target="_blank" rel="noreferrer" className="btn-secondary">Portfolio <ExternalLink size={15} /></a>}
          </div>
          <div className="mt-6 grid gap-5 md:grid-cols-2">
            <div><p className="label">Expertise</p><p className="text-sm leading-6 text-stone-600">{application.expertise}</p></div>
            <div><p className="label">Teaching motivation</p><p className="text-sm leading-6 text-stone-600">{application.motivation}</p></div>
          </div>
          <div className="mt-6 flex flex-wrap gap-3">
            <button disabled={decide.isPending} onClick={() => decide.mutate({ id: application.id, status: 'APPROVED', note: 'Approved by platform administrator' })} className="btn-primary"><Check size={17} /> Approve</button>
            <button disabled={decide.isPending} onClick={() => {
              const note = window.prompt('Reason for rejection (shown to the applicant):');
              if (note?.trim()) decide.mutate({ id: application.id, status: 'REJECTED', note });
            }} className="btn-secondary text-red-700"><X size={17} /> Reject</button>
          </div>
        </article>
      ))}
    </div>
  );
}

function Users() {
  const [search, setSearch] = useState('');
  const [role, setRole] = useState('');
  const [enabled, setEnabled] = useState('');
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ['admin', 'users', search, role, enabled],
    queryFn: () => api<Page<AdminUser>>(`/api/admin/users?${new URLSearchParams({ q: search, role, enabled, size: '50' })}`),
  });
  const updateRoles = useMutation({
    mutationFn: ({ user, nextRoles }: { user: AdminUser; nextRoles: Role[] }) => api(`/api/admin/users/${user.id}/roles`, { method: 'PATCH', body: JSON.stringify({ roles: nextRoles }) }),
    onSuccess: () => { toast.success('User roles updated'); queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }); },
    onError: (error: ApiError) => toast.error(error.message),
  });
  const toggleRole = (user: AdminUser, value: Role) => {
    const next = user.roles.includes(value) ? user.roles.filter((item) => item !== value) : [...user.roles, value];
    if (!next.length) return toast.error('A user must keep at least one role');
    updateRoles.mutate({ user, nextRoles: next });
  };
  const toggle = useMutation({
    mutationFn: (user: AdminUser) => api(`/api/admin/users/${user.id}/status`, { method: 'PATCH', body: JSON.stringify({ enabled: !user.enabled }) }),
    onSuccess: () => { toast.success('Account status updated'); queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }); },
    onError: (error: ApiError) => toast.error(error.message),
  });

  return (
    <div className="card overflow-hidden">
      <div className="grid gap-3 border-b p-5 md:grid-cols-[1fr_180px_180px]">
        <label className="relative block"><span className="sr-only">Search users</span><Search className="absolute left-4 top-3.5 text-stone-400" size={18} /><input className="input pl-11" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search name or email" /></label><select className="input" value={role} onChange={(event) => setRole(event.target.value)}><option value="">All roles</option><option>STUDENT</option><option>INSTRUCTOR</option><option>ADMIN</option></select><select className="input" value={enabled} onChange={(event) => setEnabled(event.target.value)}><option value="">Any status</option><option value="true">Active</option><option value="false">Disabled</option></select>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-stone-50 text-xs uppercase tracking-wider text-stone-500"><tr><th className="px-5 py-4">User</th><th className="px-5 py-4">Roles</th><th className="px-5 py-4">Status</th><th className="px-5 py-4 text-right">Action</th></tr></thead>
          <tbody className="divide-y">
            {query.data?.content.map((user) => <tr key={user.id}><td className="px-5 py-4"><span className="font-semibold">{user.displayName}</span><span className="block text-xs text-stone-500">{user.email}</span></td><td className="px-5 py-4"><div className="flex flex-wrap gap-1">{(['STUDENT', 'INSTRUCTOR', 'ADMIN'] as Role[]).map((item) => <button key={item} disabled={updateRoles.isPending} onClick={() => toggleRole(user, item)} className={`rounded-full px-2 py-1 text-[10px] font-semibold ${user.roles.includes(item) ? 'bg-forest text-white' : 'bg-stone-100 text-stone-500'}`}>{item}</button>)}</div></td><td className="px-5 py-4"><Status enabled={user.enabled} /></td><td className="px-5 py-4 text-right"><button disabled={toggle.isPending} onClick={() => toggle.mutate(user)} className="text-xs font-semibold text-forest hover:underline">{user.enabled ? 'Disable' : 'Enable'}</button></td></tr>)}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function Courses() {
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const query = useQuery({ queryKey: ['managed', 'admin', search, status], queryFn: () => api<Page<CourseSummary>>('/api/courses/managed?size=100') });
  const filtered = query.data?.content.filter((course) => (!search || `${course.title} ${course.instructorName}`.toLowerCase().includes(search.toLowerCase())) && (!status || course.status === status)) ?? [];
  return <div className="card overflow-hidden"><div className="grid gap-3 border-b p-5 md:grid-cols-[1fr_200px]"><label className="relative"><Search className="absolute left-4 top-3.5 text-stone-400" size={18}/><input className="input pl-11" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search courses or instructors" /></label><select className="input" value={status} onChange={(event) => setStatus(event.target.value)}><option value="">All statuses</option><option>DRAFT</option><option>PUBLISHED</option><option>ARCHIVED</option></select></div><div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead className="bg-stone-50 text-xs uppercase tracking-wider text-stone-500"><tr><th className="px-5 py-4">Course</th><th className="px-5 py-4">Instructor</th><th className="px-5 py-4">Status</th><th className="px-5 py-4">Rating</th><th className="px-5 py-4" /></tr></thead><tbody className="divide-y">{filtered.map((course) => <tr key={course.id}><td className="px-5 py-4 font-semibold">{course.title}<span className="block text-xs font-normal text-stone-500">{course.categoryName ?? 'Uncategorized'}</span></td><td className="px-5 py-4">{course.instructorName}</td><td className="px-5 py-4">{course.status}</td><td className="px-5 py-4">{Number(course.averageRating).toFixed(1)} ({course.ratingCount})</td><td className="px-5 py-4 text-right"><Link to={`/instructor/courses/${course.id}`} className="text-xs font-semibold text-forest">Manage</Link></td></tr>)}</tbody></table>{!filtered.length && <p className="p-8 text-center text-sm text-stone-500">No courses match these filters.</p>}</div></div>;
}

function Categories() {
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [description, setDescription] = useState('');
  const query = useQuery({ queryKey: ['categories'], queryFn: () => api<Category[]>('/api/categories') });
  const create = useMutation({
    mutationFn: () => api('/api/categories', { method: 'POST', body: JSON.stringify({ name, slug, description }) }),
    onSuccess: () => { setName(''); setSlug(''); setDescription(''); toast.success('Category created'); queryClient.invalidateQueries({ queryKey: ['categories'] }); },
    onError: (error: ApiError) => toast.error(error.message),
  });
  const remove = async (category: Category) => {
    if (!window.confirm(`Delete “${category.name}”? Existing courses become uncategorized.`)) return;
    try { await api(`/api/categories/${category.id}`, { method: 'DELETE' }); toast.success('Category deleted'); queryClient.invalidateQueries({ queryKey: ['categories'] }); }
    catch (error) { toast.error((error as ApiError).message); }
  };
  const edit = async (category: Category) => {
    const nextName = window.prompt('Category name:', category.name);
    if (!nextName?.trim()) return;
    const nextSlug = window.prompt('Category slug:', category.slug);
    if (!nextSlug?.trim()) return;
    const nextDescription = window.prompt('Category description:', category.description ?? '') ?? '';
    try { await api(`/api/categories/${category.id}`, { method: 'PUT', body: JSON.stringify({ name: nextName, slug: nextSlug, description: nextDescription }) }); toast.success('Category updated'); queryClient.invalidateQueries({ queryKey: ['categories'] }); }
    catch (error) { toast.error((error as ApiError).message); }
  };
  const makeSlug = (value: string) => value.toLowerCase().trim().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

  return (
    <div className="grid gap-6 lg:grid-cols-[380px_1fr]">
      <form onSubmit={(event) => { event.preventDefault(); create.mutate(); }} className="card h-fit space-y-4 p-6">
        <h2 className="font-display text-2xl font-semibold">New category</h2>
        <label><span className="label">Name</span><input required maxLength={80} className="input" value={name} onChange={(event) => { setName(event.target.value); setSlug(makeSlug(event.target.value)); }} /></label>
        <label><span className="label">Slug</span><input required pattern="[a-z0-9]+(-[a-z0-9]+)*" className="input" value={slug} onChange={(event) => setSlug(event.target.value)} /></label>
        <label><span className="label">Description</span><textarea maxLength={300} className="input min-h-24" value={description} onChange={(event) => setDescription(event.target.value)} /></label>
        <button disabled={create.isPending} className="btn-primary">Create category</button>
      </form>
      <div className="space-y-3">
        {query.data?.map((category) => <div key={category.id} className="card flex items-center justify-between gap-4 p-5"><div><h3 className="font-semibold">{category.name}</h3><p className="mt-1 text-sm text-stone-500">/{category.slug} · {category.description}</p></div><div className="flex gap-3"><button onClick={() => edit(category)} className="text-xs font-semibold text-forest">Edit</button><button onClick={() => remove(category)} className="text-xs font-semibold text-red-600">Delete</button></div></div>)}
      </div>
    </div>
  );
}

function Status({ enabled }: { enabled: boolean }) { return <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${enabled ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'}`}>{enabled ? 'Active' : 'Disabled'}</span>; }
function EmptyPanel({ title, message }: { title: string; message: string }) { return <div className="card p-10 text-center"><h2 className="font-display text-2xl font-semibold">{title}</h2><p className="mt-2 text-sm text-stone-500">{message}</p></div>; }

function AuditHistory() {
  const query = useQuery({ queryKey: ['admin', 'audit'], queryFn: () => api<Page<AuditLog>>('/api/admin/audit-logs?size=100') });
  return <div className="card overflow-hidden"><div className="overflow-x-auto"><table className="w-full text-left text-sm"><thead className="bg-stone-50 text-xs uppercase tracking-wider text-stone-500"><tr><th className="px-5 py-4">Time</th><th className="px-5 py-4">Action</th><th className="px-5 py-4">Resource</th><th className="px-5 py-4">Actor</th><th className="px-5 py-4">IP</th></tr></thead><tbody className="divide-y">{query.data?.content.map((entry) => <tr key={entry.id}><td className="whitespace-nowrap px-5 py-4 text-stone-500">{new Date(entry.createdAt).toLocaleString()}</td><td className="px-5 py-4 font-semibold">{entry.action.replaceAll('_', ' ')}</td><td className="px-5 py-4">{entry.resourceType}<span className="block max-w-48 truncate text-xs text-stone-400">{entry.resourceId}</span></td><td className="px-5 py-4 text-xs text-stone-500">{entry.actorId ?? 'System'}</td><td className="px-5 py-4 text-xs text-stone-500">{entry.ipAddress ?? '—'}</td></tr>)}</tbody></table></div></div>;
}
