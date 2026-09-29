import { useState } from 'react';
import { ArrowRight, BadgeCheck, Clock3, ExternalLink, Send, ShieldCheck } from 'lucide-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { api, ApiError } from '../lib/api';
import { useAuth } from '../lib/auth';
import type { InstructorApplication } from '../types';

type ApplicationForm = { expertise: string; motivation: string; portfolioUrl: string };

export default function InstructorApplicationPage() {
  const { user, refresh } = useAuth();
  const queryClient = useQueryClient();
  const [refreshing, setRefreshing] = useState(false);
  const application = useQuery({
    queryKey: ['instructor-application', 'mine'],
    queryFn: () => api<InstructorApplication | null>('/api/instructor-applications/mine'),
  });
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<ApplicationForm>();

  const submit = async (values: ApplicationForm) => {
    try {
      await api('/api/instructor-applications', {
        method: 'POST',
        body: JSON.stringify({ ...values, portfolioUrl: values.portfolioUrl || null }),
      });
      toast.success('Application submitted for admin review');
      await queryClient.invalidateQueries({ queryKey: ['instructor-application'] });
    } catch (error) {
      toast.error((error as ApiError).message);
    }
  };

  const refreshAccess = async () => {
    setRefreshing(true);
    try {
      const nextUser = await refresh();
      if (nextUser?.roles.includes('INSTRUCTOR')) toast.success('Instructor workspace unlocked');
    } catch (error) {
      toast.error((error as ApiError).message);
    } finally {
      setRefreshing(false);
    }
  };

  if (user?.roles.includes('INSTRUCTOR')) {
    return (
      <section className="container-page max-w-3xl py-16">
        <div className="card p-8 text-center sm:p-12">
          <BadgeCheck className="mx-auto text-forest" size={48} />
          <h1 className="mt-5 font-display text-4xl font-bold">You’re an instructor.</h1>
          <p className="mt-3 text-stone-600">Your studio is ready for course creation and learner support.</p>
          <a href="/dashboard" className="btn-primary mt-7">Open instructor studio <ArrowRight size={17} /></a>
        </div>
      </section>
    );
  }

  const current = application.data;
  if (current?.status === 'PENDING') {
    return <StatusCard icon={<Clock3 size={44} />} title="Your application is under review" message="An administrator will review your teaching background and motivation. You can keep learning while you wait." />;
  }
  if (current?.status === 'APPROVED') {
    return (
      <section className="container-page max-w-3xl py-16">
        <div className="card p-8 text-center sm:p-12">
          <BadgeCheck className="mx-auto text-forest" size={48} />
          <p className="eyebrow mt-5">Approved</p>
          <h1 className="mt-3 font-display text-4xl font-bold">Welcome to the teaching team.</h1>
          <p className="mx-auto mt-3 max-w-xl text-stone-600">Refresh your secure session to load the newly granted instructor permissions.</p>
          <button onClick={refreshAccess} disabled={refreshing} className="btn-primary mt-7">
            {refreshing ? 'Refreshing…' : 'Activate instructor access'} <ArrowRight size={17} />
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="container-page py-14">
      <div className="mx-auto max-w-5xl">
        <p className="eyebrow">Teach on Academy</p>
        <h1 className="mt-3 max-w-3xl font-display text-5xl font-bold">Turn your experience into someone else’s breakthrough.</h1>
        <div className="mt-10 grid gap-7 lg:grid-cols-[1fr_330px]">
          <form onSubmit={handleSubmit(submit)} className="card space-y-6 p-6 sm:p-8">
            {current?.status === 'REJECTED' && (
              <div className="rounded-2xl bg-amber-50 p-4 text-sm text-amber-900">
                <strong>Previous review:</strong> {current.reviewNote}. You can strengthen and resubmit your application.
              </div>
            )}
            <label className="block">
              <span className="label">Your subject expertise</span>
              <textarea className="input min-h-28" maxLength={500} {...register('expertise', { required: true, minLength: 20 })} placeholder="Describe the domains, tools, and real-world work you can teach." />
              {errors.expertise && <span className="mt-1 block text-xs text-red-600">Please provide at least 20 characters.</span>}
            </label>
            <label className="block">
              <span className="label">Why you want to teach</span>
              <textarea className="input min-h-40" maxLength={2000} {...register('motivation', { required: true, minLength: 50 })} placeholder="Tell us about your teaching approach, intended learners, and the outcomes you want to create." />
              {errors.motivation && <span className="mt-1 block text-xs text-red-600">Please provide at least 50 characters.</span>}
            </label>
            <label className="block">
              <span className="label">Portfolio or professional profile <span className="text-stone-400">(optional)</span></span>
              <input className="input" type="url" {...register('portfolioUrl')} placeholder="https://…" />
            </label>
            <button disabled={isSubmitting} className="btn-primary">
              <Send size={17} /> {isSubmitting ? 'Submitting…' : current ? 'Resubmit application' : 'Submit application'}
            </button>
          </form>
          <aside className="card h-fit bg-forest p-6 text-white">
            <ShieldCheck className="text-mint" size={34} />
            <h2 className="mt-6 font-display text-2xl font-semibold">Quality first</h2>
            <p className="mt-3 text-sm leading-6 text-white/70">Instructor access is granted by an administrator after review. Roles cannot be selected during registration or changed by the browser.</p>
            <a href="/courses" className="mt-6 inline-flex items-center gap-2 text-sm font-semibold text-mint">Explore course quality <ExternalLink size={15} /></a>
          </aside>
        </div>
      </div>
    </section>
  );
}

function StatusCard({ icon, title, message }: { icon: React.ReactNode; title: string; message: string }) {
  return (
    <section className="container-page max-w-3xl py-16">
      <div className="card p-8 text-center sm:p-12">
        <div className="mx-auto text-forest">{icon}</div>
        <h1 className="mt-5 font-display text-4xl font-bold">{title}</h1>
        <p className="mx-auto mt-3 max-w-xl text-stone-600">{message}</p>
      </div>
    </section>
  );
}
