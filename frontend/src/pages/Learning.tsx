import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, ChevronDown, Circle, PlayCircle } from 'lucide-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { api, ApiError } from '../lib/api';
import type { LearningCourse } from '../types';
import { ErrorState } from '../components/States';

export default function Learning() {
  const { id } = useParams();
  const queryClient = useQueryClient();
  const course = useQuery({ queryKey: ['learning', id], queryFn: () => api<LearningCourse>(`/api/learning/courses/${id}`) });
  const lessons = useMemo(() => course.data?.sections.flatMap((section) => section.lessons) ?? [], [course.data]);
  const [selectedId, setSelectedId] = useState<string>();
  useEffect(() => { if (!selectedId && lessons[0]) setSelectedId(lessons[0].id); }, [lessons, selectedId]);
  const active = lessons.find((lesson) => lesson.id === selectedId) ?? lessons[0];
  const completion = useMutation({
    mutationFn: ({ lessonId, completed }: { lessonId: string; completed: boolean }) => api(`/api/learning/lessons/${lessonId}/completion`, { method: 'PUT', body: JSON.stringify({ completed }) }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['learning', id] }); queryClient.invalidateQueries({ queryKey: ['enrollments'] }); queryClient.invalidateQueries({ queryKey: ['student-stats'] }); },
    onError: (error: ApiError) => toast.error(error.message),
  });

  if (course.isLoading) return <div className="container-page py-16"><div className="skeleton h-96" /></div>;
  if (!course.data) return <div className="container-page py-16"><ErrorState message="Enroll in this course to access its learning workspace." /></div>;
  return (
    <div className="min-h-[calc(100vh-73px)] bg-stone-100">
      <div className="border-b bg-white"><div className="container-page flex items-center justify-between py-4"><Link to={`/courses/${id}`} className="flex items-center gap-2 text-sm font-medium"><ArrowLeft size={17} />Course overview</Link><div className="min-w-44 text-right"><div className="flex justify-between text-xs text-stone-500"><span>Course progress</span><span>{course.data.progress}%</span></div><div className="mt-2 h-2 overflow-hidden rounded-full bg-stone-100"><div className="h-full rounded-full bg-coral transition-all" style={{ width: `${course.data.progress}%` }} /></div></div></div></div>
      <div className="grid lg:grid-cols-[340px_1fr]">
        <aside className="border-r bg-white lg:min-h-[calc(100vh-145px)]"><div className="border-b p-5"><p className="text-xs font-semibold uppercase tracking-wider text-forest">Learning path</p><h1 className="mt-2 font-display text-xl font-semibold">{course.data.title}</h1></div>{course.data.sections.map((section) => <div key={section.id} className="border-b"><div className="flex items-center justify-between p-4 font-semibold"><span>{section.title}</span><ChevronDown size={16} /></div>{section.lessons.map((lesson, index) => <button key={lesson.id} onClick={() => setSelectedId(lesson.id)} className={`flex w-full items-start gap-3 px-4 py-3 text-left text-sm ${active?.id === lesson.id ? 'bg-mint/40 text-forest' : 'hover:bg-stone-50'}`}>{lesson.completed ? <CheckCircle2 size={22} className="shrink-0 text-forest" /> : <span className="grid h-[22px] w-[22px] shrink-0 place-items-center rounded-full border text-[10px]">{index + 1}</span>}<span>{lesson.title}<span className="mt-1 block text-xs text-stone-400">{lesson.durationMinutes} min</span></span></button>)}</div>)}</aside>
        <main className="p-5 sm:p-10"><div className="mx-auto max-w-4xl">{active ? <LessonView lesson={active} saving={completion.isPending} toggle={() => completion.mutate({ lessonId: active.id, completed: !active.completed })} /> : <ErrorState message="No lessons have been added yet." />}</div></main>
      </div>
    </div>
  );
}

function LessonView({ lesson, saving, toggle }: { lesson: LearningCourse['sections'][number]['lessons'][number]; saving: boolean; toggle: () => void }) {
  return <><div className="aspect-video overflow-hidden rounded-3xl bg-ink shadow-soft">{lesson.videoUrl ? <video src={lesson.videoUrl} controls className="h-full w-full" /> : <div className="grid h-full place-items-center text-white"><div className="text-center"><PlayCircle className="mx-auto text-mint" size={56} /><p className="mt-3 text-sm text-white/60">Reading lesson</p></div></div>}</div><div className="mt-8 flex flex-wrap items-start justify-between gap-4"><div><p className="eyebrow">Current lesson</p><h2 className="mt-2 font-display text-3xl font-bold">{lesson.title}</h2></div><button disabled={saving} onClick={toggle} className={lesson.completed ? 'btn-primary' : 'btn-secondary'}>{lesson.completed ? <CheckCircle2 size={17} /> : <Circle size={17} />}{lesson.completed ? 'Completed' : 'Mark complete'}</button></div><article className="mt-7 whitespace-pre-line text-base leading-8 text-stone-700">{lesson.content}</article></>;
}
