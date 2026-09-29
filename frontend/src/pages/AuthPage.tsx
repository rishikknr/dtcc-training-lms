import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowRight, BookOpen, Eye, EyeOff } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import toast from 'react-hot-toast';
import { api, ApiError } from '../lib/api';
import { useAuth } from '../lib/auth';

const schema = z.object({
  displayName: z.string().optional(),
  email: z.string().email('Enter a valid email'),
  password: z.string().min(12, 'Use at least 12 characters'),
});

type Form = z.infer<typeof schema>;

export default function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const [show, setShow] = useState(false);
  const { refresh } = useAuth();
  const nav = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm<Form>({ resolver: zodResolver(schema) });

  const submit = async (values: Form) => {
    try {
      await api(`/api/auth/${mode}`, {
        method: 'POST',
        body: JSON.stringify(values),
      });
      await refresh();
      toast.success(mode === 'login' ? 'Welcome back' : 'Your learning space is ready');
      nav('/dashboard');
    } catch (error) {
      const apiError = error as ApiError;
      setError('root', { message: apiError.message });
    }
  };

  return (
    <div className="container-page grid min-h-[calc(100vh-73px)] items-stretch gap-0 py-8 lg:grid-cols-2">
      <div className="hidden overflow-hidden rounded-l-[2rem] bg-forest p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="flex items-center gap-2 font-display text-xl font-bold">
          <BookOpen />academy.
        </div>
        <blockquote className="max-w-lg">
          <p className="font-display text-4xl font-semibold leading-tight">
            “The beautiful thing about learning is that nobody can take it away from you.”
          </p>
          <footer className="mt-5 text-white/60">— B.B. King</footer>
        </blockquote>
        <p className="text-sm text-white/60">Learn with purpose. Build with confidence.</p>
      </div>

      <div className="flex items-center justify-center rounded-[2rem] bg-white px-6 py-14 lg:rounded-l-none">
        <div className="w-full max-w-md">
          <p className="eyebrow">{mode === 'login' ? 'Welcome back' : 'Join Academy'}</p>
          <h1 className="mt-3 font-display text-4xl font-bold">
            {mode === 'login' ? 'Continue your journey' : 'Start learning today'}
          </h1>
          <p className="mt-3 text-sm text-stone-500">
            {mode === 'login' ? 'New here? ' : 'Already a member? '}
            <Link
              className="font-semibold text-forest underline underline-offset-4"
              to={mode === 'login' ? '/register' : '/login'}
            >
              {mode === 'login' ? 'Create an account' : 'Sign in'}
            </Link>
          </p>

          <form className="mt-8 space-y-5" onSubmit={handleSubmit(submit)}>
            {mode === 'register' && (
              <Field label="Full name" error={errors.displayName?.message}>
                <input
                  className="input"
                  autoComplete="name"
                  {...register('displayName')}
                  placeholder="Maya Chen"
                />
              </Field>
            )}
            <Field label="Email address" error={errors.email?.message}>
              <input
                className="input"
                type="email"
                autoComplete="email"
                {...register('email')}
                placeholder="you@example.com"
              />
            </Field>
            <Field label="Password" error={errors.password?.message}>
              <div className="relative">
                <input
                  className="input pr-11"
                  type={show ? 'text' : 'password'}
                  autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                  {...register('password')}
                  placeholder="At least 12 characters"
                />
                <button
                  type="button"
                  onClick={() => setShow(!show)}
                  className="absolute right-3 top-3 text-stone-400"
                  aria-label="Toggle password visibility"
                >
                  {show ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </Field>
            {errors.root && (
              <p role="alert" className="rounded-xl bg-red-50 p-3 text-sm text-red-700">
                {errors.root.message}
              </p>
            )}
            <button disabled={isSubmitting} className="btn-primary w-full py-3.5">
              {isSubmitting ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
              <ArrowRight size={18} />
            </button>
          </form>
          {mode === 'login' && (
            <p className="mt-5 text-center text-xs text-stone-400">
              Development accounts use the password configured in SEED_PASSWORD.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block">
      <span className="label">{label}</span>
      {children}
      {error && <span className="mt-1 block text-xs text-red-600">{error}</span>}
    </label>
  );
}
