import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { BookOpen, Check, ChevronDown, Clock, Edit3, PlayCircle, Star } from 'lucide-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { api, ApiError } from '../lib/api';
import { useAuth } from '../lib/auth';
import type { Course, Page, Review } from '../types';
import { ErrorState } from '../components/States';

export default function CourseDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const course = useQuery({ queryKey: ['course', id], queryFn: () => api<Course>(`/api/courses/${id}`) });
  const reviews = useQuery({ queryKey: ['reviews', id], queryFn: () => api<Page<Review>>(`/api/courses/${id}/reviews`) });
  const enroll = useMutation({
    mutationFn: () => api(`/api/courses/${id}/enroll`, { method: 'POST' }),
    onSuccess: () => { toast.success('You’re enrolled—welcome in!'); queryClient.invalidateQueries({ queryKey: ['enrollments'] }); queryClient.invalidateQueries({ queryKey: ['course', id] }); navigate(`/learn/${id}`); },
    onError: (error: ApiError) => toast.error(error.message),
  });

  if (course.isLoading) return <div className="container-page py-20"><div className="skeleton h-96" /></div>;
  if (!course.data) return <div className="container-page py-20"><ErrorState /></div>;

  const current = course.data;
  const lessonCount = current.sections.reduce((sum, section) => sum + section.lessons.length, 0);
  return (
    <>
      <section className="bg-ink py-16 text-white">
        <div className="container-page grid gap-10 lg:grid-cols-[1fr_380px]">
          <div><div className="flex items-center gap-3"><p className="eyebrow !text-mint">{current.category?.name ?? 'Featured course'}</p>{current.status !== 'PUBLISHED' && <span className="rounded-full bg-white/10 px-2.5 py-1 text-xs font-semibold">{current.status}</span>}</div><h1 className="mt-5 max-w-3xl font-display text-5xl font-bold leading-tight">{current.title}</h1><p className="mt-5 max-w-2xl text-lg leading-8 text-white/70">{current.shortDescription}</p><div className="mt-7 flex flex-wrap gap-5 text-sm text-white/70"><span className="flex items-center gap-2"><Star className="fill-amber-400 text-amber-400" size={17} />{Number(current.averageRating).toFixed(1)} ({current.ratingCount} reviews)</span><span className="flex items-center gap-2"><BookOpen size={17} />{lessonCount} lessons</span><span className="flex items-center gap-2"><Clock size={17} />{current.level.toLowerCase()}</span></div><p className="mt-7 text-sm">Created by <span className="font-semibold text-mint">{current.instructor.displayName}</span></p></div>
          <div className="card overflow-hidden bg-white text-ink">{current.thumbnailUrl && <img src={current.thumbnailUrl} className="aspect-video w-full object-cover" alt="" />}<div className="p-6"><p className="font-display text-2xl font-bold">Ready when you are.</p><p className="mt-2 text-sm text-stone-500">Full access · Learn at your pace · Progress tracking</p><CourseAction course={current} user={user} pending={enroll.isPending} enroll={() => enroll.mutate()} /><ul className="mt-5 space-y-2 text-sm text-stone-600"><li className="flex gap-2"><Check size={17} className="text-forest" />Expert-led curriculum</li><li className="flex gap-2"><Check size={17} className="text-forest" />Lesson-level progress</li></ul></div></div>
        </div>
      </section>
      <section className="container-page grid gap-12 py-16 lg:grid-cols-[1fr_340px]">
        <div><h2 className="font-display text-3xl font-bold">What you’ll learn</h2><p className="mt-5 whitespace-pre-line leading-8 text-stone-600">{current.description}</p><h2 className="mt-12 font-display text-3xl font-bold">Course content</h2><div className="mt-5 divide-y overflow-hidden rounded-2xl border bg-white">{current.sections.map((section) => <details key={section.id} className="group p-5" open={section.position === 0}><summary className="flex cursor-pointer list-none items-center justify-between font-semibold">{section.title}<ChevronDown className="transition group-open:rotate-180" size={18} /></summary><div className="mt-4 space-y-3">{section.lessons.map((lesson) => <div key={lesson.id} className="flex items-center justify-between text-sm text-stone-600"><span className="flex items-center gap-2"><PlayCircle size={16} />{lesson.title}{lesson.preview && <span className="rounded-full bg-mint/50 px-2 py-0.5 text-[10px] font-semibold text-forest">Preview</span>}</span><span>{lesson.durationMinutes}m</span></div>)}</div></details>)}</div><ReviewComposer courseId={current.id} enrolled={current.enrolled} /></div>
        <aside><h3 className="font-display text-xl font-semibold">Learner feedback</h3><div className="mt-5 space-y-4">{reviews.data?.content.map((review) => <ReviewCard key={review.id} review={review} courseId={current.id} own={user?.id === review.studentId} />)}{reviews.data?.content.length === 0 && <p className="text-sm text-stone-500">No reviews yet. Enrolled learners can share the first one.</p>}</div></aside>
      </section>
    </>
  );
}

function CourseAction({ course, user, pending, enroll }: { course: Course; user: ReturnType<typeof useAuth>['user']; pending: boolean; enroll: () => void }) {
  if (course.manageable) return <Link to={`/instructor/courses/${course.id}`} className="btn-primary mt-5 w-full"><Edit3 size={17} /> Manage course</Link>;
  if (course.enrolled) return <Link to={`/learn/${course.id}`} className="btn-primary mt-5 w-full">Continue learning</Link>;
  if (user?.roles.includes('STUDENT')) return <button onClick={enroll} disabled={pending || course.status !== 'PUBLISHED'} className="btn-primary mt-5 w-full">{pending ? 'Enrolling…' : 'Enroll for free'}</button>;
  if (user) return <p className="mt-5 rounded-xl bg-stone-50 p-3 text-sm text-stone-600">Student access is required to enroll.</p>;
  return <Link to="/login" className="btn-primary mt-5 w-full">Sign in to enroll</Link>;
}

function ReviewComposer({ courseId, enrolled }: { courseId: string; enrolled: boolean }) {
  const { user } = useAuth();
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const queryClient = useQueryClient();
  const mutation = useMutation({ mutationFn: () => api('/api/reviews', { method: 'POST', body: JSON.stringify({ courseId, rating, comment }) }), onSuccess: () => { toast.success('Review published'); setComment(''); queryClient.invalidateQueries({ queryKey: ['reviews', courseId] }); queryClient.invalidateQueries({ queryKey: ['course', courseId] }); }, onError: (error: ApiError) => toast.error(error.message) });
  if (!user?.roles.includes('STUDENT')) return null;
  if (!enrolled) return <div className="mt-12 rounded-2xl border border-dashed p-6 text-sm text-stone-500">Enroll in the course before leaving a review.</div>;
  return <div className="card mt-12 p-6"><h3 className="font-display text-xl font-semibold">Share your experience</h3><div className="mt-4 flex gap-1">{[1, 2, 3, 4, 5].map((value) => <button key={value} onClick={() => setRating(value)} aria-label={`${value} stars`}><Star className={value <= rating ? 'fill-amber-400 text-amber-400' : 'text-stone-300'} /></button>)}</div><textarea className="input mt-4 min-h-28" maxLength={2000} value={comment} onChange={(event) => setComment(event.target.value)} placeholder="What did you find most useful?" /><button disabled={!comment.trim() || mutation.isPending} onClick={() => mutation.mutate()} className="btn-primary mt-3">Publish review</button></div>;
}

function ReviewCard({ review, courseId, own }: { review: Review; courseId: string; own: boolean }) {
  return <article className="card p-5"><div className="flex items-center justify-between"><span className="font-semibold">{review.studentName}</span><span className="flex text-amber-400">{'★'.repeat(review.rating)}</span></div><p className="mt-3 text-sm leading-6 text-stone-600">{review.comment}</p>{own && <ReviewOwnerActions review={review} courseId={courseId} />}</article>;
}

function ReviewOwnerActions({ review, courseId }: { review: Review; courseId: string }) {
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState(false);
  const [comment, setComment] = useState(review.comment);
  const [rating, setRating] = useState(review.rating);
  const refresh = () => { queryClient.invalidateQueries({ queryKey: ['reviews', courseId] }); queryClient.invalidateQueries({ queryKey: ['course', courseId] }); };
  const save = async () => { try { await api(`/api/reviews/${review.id}`, { method: 'PUT', body: JSON.stringify({ rating, comment }) }); setEditing(false); toast.success('Review updated'); refresh(); } catch (error) { toast.error((error as ApiError).message); } };
  const remove = async () => { if (!window.confirm('Delete your review?')) return; try { await api(`/api/reviews/${review.id}`, { method: 'DELETE' }); toast.success('Review deleted'); refresh(); } catch (error) { toast.error((error as ApiError).message); } };
  return editing ? <div className="mt-3"><select className="input mb-2" value={rating} onChange={(event) => setRating(Number(event.target.value))}>{[1, 2, 3, 4, 5].map((value) => <option key={value} value={value}>{value} stars</option>)}</select><textarea className="input min-h-20" value={comment} onChange={(event) => setComment(event.target.value)} /><div className="mt-2 flex gap-3 text-xs font-semibold"><button onClick={save} className="text-forest">Save</button><button onClick={() => setEditing(false)}>Cancel</button></div></div> : <div className="mt-3 flex gap-3 text-xs font-semibold"><button onClick={() => setEditing(true)} className="text-forest">Edit</button><button onClick={remove} className="text-red-600">Delete</button></div>;
}
