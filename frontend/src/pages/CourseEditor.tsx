import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Archive, ChevronDown, ChevronUp, Edit3, Eye, Plus, Save, Send, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { api, ApiError } from '../lib/api';
import { useAuth } from '../lib/auth';
import type { AdminUser, Category, Course, CourseLevel, CourseSection, CourseStudent, Lesson, Page } from '../types';
import { ErrorState } from '../components/States';

type CourseForm = { title: string; shortDescription: string; description: string; level: CourseLevel; categoryId: string; thumbnailUrl: string; prerequisites: string; learningObjectives: string; tags: string; language: string };

export default function CourseEditor() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { has } = useAuth();
  const [sectionTitle, setSectionTitle] = useState('');
  const course = useQuery({ queryKey: ['course', id, 'manage'], queryFn: () => api<Course>(`/api/courses/${id}`), enabled: editing });
  const categories = useQuery({ queryKey: ['categories'], queryFn: () => api<Category[]>('/api/categories') });
  const instructors = useQuery({ queryKey: ['admin', 'instructors'], queryFn: () => api<Page<AdminUser>>('/api/admin/users?size=50'), enabled: editing && has('ADMIN') });
  const { register, handleSubmit, reset, formState: { isSubmitting, errors } } = useForm<CourseForm>({ defaultValues: { level: 'BEGINNER', categoryId: '', thumbnailUrl: '', prerequisites: '', learningObjectives: '', tags: '', language: 'English' } });

  useEffect(() => {
    if (!course.data) return;
    reset({ title: course.data.title, shortDescription: course.data.shortDescription, description: course.data.description, level: course.data.level, categoryId: course.data.category?.id ?? '', thumbnailUrl: course.data.thumbnailUrl ?? '', prerequisites: course.data.prerequisites ?? '', learningObjectives: course.data.learningObjectives ?? '', tags: course.data.tags ?? '', language: course.data.language ?? 'English' });
  }, [course.data, reset]);

  const refresh = async () => { await course.refetch(); await queryClient.invalidateQueries({ queryKey: ['managed'] }); };
  const submit = async (values: CourseForm) => {
    try {
      const saved = await api<Course>(editing ? `/api/courses/${id}` : '/api/courses', { method: editing ? 'PUT' : 'POST', body: JSON.stringify({ ...values, categoryId: values.categoryId || null, thumbnailUrl: values.thumbnailUrl || null, prerequisites: values.prerequisites || null, learningObjectives: values.learningObjectives || null, tags: values.tags || null }) });
      toast.success(editing ? 'Course details saved' : 'Draft created');
      await queryClient.invalidateQueries({ queryKey: ['managed'] });
      if (!editing) navigate(`/instructor/courses/${saved.id}`);
      else await course.refetch();
    } catch (error) { toast.error((error as ApiError).message); }
  };

  const changeStatus = async (status: Course['status']) => {
    try { await api(`/api/courses/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }); toast.success(status === 'PUBLISHED' ? 'Course published' : status === 'ARCHIVED' ? 'Course archived' : 'Course moved to draft'); await refresh(); }
    catch (error) { toast.error((error as ApiError).message); }
  };
  const remove = async () => {
    if (!id || !window.confirm('Delete this draft permanently?')) return;
    try { await api(`/api/courses/${id}`, { method: 'DELETE' }); toast.success('Draft deleted'); navigate('/dashboard'); }
    catch (error) { toast.error((error as ApiError).message); }
  };
  const addSection = async () => {
    if (!id || !sectionTitle.trim()) return;
    try { await api(`/api/courses/${id}/sections`, { method: 'POST', body: JSON.stringify({ title: sectionTitle }) }); setSectionTitle(''); await refresh(); toast.success('Section added'); }
    catch (error) { toast.error((error as ApiError).message); }
  };
  const reorderSections = async (from: number, direction: -1 | 1) => {
    const sections = [...(course.data?.sections ?? [])];
    const to = from + direction;
    if (to < 0 || to >= sections.length || !id) return;
    [sections[from], sections[to]] = [sections[to], sections[from]];
    try { await api(`/api/courses/${id}/sections/order`, { method: 'PUT', body: JSON.stringify({ ids: sections.map((section) => section.id) }) }); await refresh(); }
    catch (error) { toast.error((error as ApiError).message); }
  };

  if (editing && course.isLoading) return <div className="container-page py-16"><div className="skeleton h-96" /></div>;
  if (editing && (!course.data || !course.data.manageable)) return <div className="container-page py-16"><ErrorState message="This course does not exist or you are not authorized to manage it." /></div>;

  const current = course.data;
  const lessonCount = current?.sections.reduce((sum, section) => sum + section.lessons.length, 0) ?? 0;
  return (
    <section className="container-page py-14">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div><p className="eyebrow">Instructor studio</p><h1 className="mt-3 font-display text-4xl font-bold">{editing ? 'Course management' : 'Create a new course'}</h1>{current && <p className="mt-2 text-sm text-stone-500">Status: <strong>{current.status}</strong> · {current.sections.length} sections · {lessonCount} lessons</p>}</div>
        {editing && <div className="flex flex-wrap gap-2">{current?.status === 'DRAFT' && <button onClick={remove} className="btn-secondary text-red-700"><Trash2 size={17} /> Delete draft</button>}{current?.status === 'PUBLISHED' ? <button onClick={() => changeStatus('ARCHIVED')} className="btn-secondary"><Archive size={17} /> Archive</button> : <button onClick={() => changeStatus('PUBLISHED')} className="btn-primary"><Send size={17} /> Publish</button>}</div>}
      </div>

      <div className="mt-9 grid gap-7 lg:grid-cols-[minmax(0,1fr)_380px]">
        <div className="space-y-7">
          <form onSubmit={handleSubmit(submit)} className="card space-y-5 p-6 sm:p-8">
            <div><h2 className="font-display text-2xl font-semibold">Course details</h2><p className="mt-1 text-sm text-stone-500">Set the promise, audience, and visual identity.</p></div>
            <label className="block"><span className="label">Course title</span><input className="input text-lg" {...register('title', { required: true, maxLength: 160 })} placeholder="A title learners will remember" />{errors.title && <span className="mt-1 text-xs text-red-600">A title is required.</span>}</label>
            <label className="block"><span className="label">Short description</span><input className="input" {...register('shortDescription', { required: true, maxLength: 300 })} placeholder="One clear promise in a sentence" /></label>
            <label className="block"><span className="label">Full description</span><textarea className="input min-h-44" {...register('description', { required: true, maxLength: 20000 })} placeholder="Outcomes, approach, and who this is for" /></label>
            <div className="grid gap-5 sm:grid-cols-2"><label><span className="label">Learning objectives</span><textarea className="input min-h-28" maxLength={5000} {...register('learningObjectives')} placeholder="One objective per line" /></label><label><span className="label">Prerequisites</span><textarea className="input min-h-28" maxLength={5000} {...register('prerequisites')} placeholder="What should learners know first?" /></label></div>
            <div className="grid gap-5 sm:grid-cols-2"><label><span className="label">Level</span><select className="input" {...register('level')}><option>BEGINNER</option><option>INTERMEDIATE</option><option>ADVANCED</option></select></label><label><span className="label">Category</span><select className="input" {...register('categoryId')}><option value="">Choose a category</option>{categories.data?.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label></div>
            <div className="grid gap-5 sm:grid-cols-2"><label><span className="label">Tags</span><input className="input" maxLength={500} {...register('tags')} placeholder="security, architecture, risk" /></label><label><span className="label">Language</span><input className="input" maxLength={80} {...register('language', { required: true })} /></label></div>
            <label className="block"><span className="label">Cover image URL</span><input className="input" type="url" {...register('thumbnailUrl')} placeholder="https://…" /></label>
            <button disabled={isSubmitting} className="btn-primary"><Save size={17} />{editing ? 'Save details' : 'Create draft'}</button>
          </form>

          {editing && <div><div className="flex items-end justify-between"><div><p className="eyebrow">Curriculum builder</p><h2 className="mt-2 font-display text-3xl font-bold">Sections & lessons</h2></div></div><div className="mt-5 space-y-4">{current?.sections.map((section, index) => <SectionEditor key={section.id} section={section} index={index} total={current.sections.length} move={(direction) => reorderSections(index, direction)} refresh={refresh} />)}<div className="card flex gap-3 p-4"><input className="input" value={sectionTitle} onChange={(event) => setSectionTitle(event.target.value)} placeholder="New section title" maxLength={160} /><button type="button" disabled={!sectionTitle.trim()} onClick={addSection} className="btn-primary shrink-0"><Plus size={17} /> Add section</button></div></div></div>}
          {current && <CourseRoster courseId={current.id} />}
        </div>

        <aside className="space-y-5 lg:sticky lg:top-28 lg:h-fit">
          <div className="card p-6"><h2 className="font-display text-xl font-semibold">Publishing checklist</h2><ul className="mt-4 space-y-3 text-sm"><Checklist done={Boolean(current?.category)} text="Category selected" /><Checklist done={Boolean(current?.thumbnailUrl)} text="Cover image added" /><Checklist done={Boolean(current?.sections.length)} text="At least one section" /><Checklist done={lessonCount > 0} text="At least one lesson" /></ul></div>
          {current && has('ADMIN') && <InstructorAssignment course={current} users={instructors.data?.content ?? []} refresh={refresh} />}
          {current && <a href={`/courses/${current.id}`} className="btn-secondary w-full"><Eye size={17} /> Preview course page</a>}
        </aside>
      </div>
    </section>
  );
}

function SectionEditor({ section, index, total, move, refresh }: { section: CourseSection; index: number; total: number; move: (direction: -1 | 1) => void; refresh: () => Promise<void> }) {
  const [editing, setEditing] = useState(false);
  const [title, setTitle] = useState(section.title);
  const [addingLesson, setAddingLesson] = useState(false);
  const save = async () => { try { await api(`/api/sections/${section.id}`, { method: 'PUT', body: JSON.stringify({ title }) }); setEditing(false); await refresh(); toast.success('Section updated'); } catch (error) { toast.error((error as ApiError).message); } };
  const remove = async () => { if (!window.confirm(`Delete “${section.title}” and all of its lessons?`)) return; try { await api(`/api/sections/${section.id}`, { method: 'DELETE' }); await refresh(); toast.success('Section deleted'); } catch (error) { toast.error((error as ApiError).message); } };
  const reorderLessons = async (from: number, direction: -1 | 1) => { const ordered = [...section.lessons]; const to = from + direction; if (to < 0 || to >= ordered.length) return; [ordered[from], ordered[to]] = [ordered[to], ordered[from]]; try { await api(`/api/sections/${section.id}/lessons/order`, { method: 'PUT', body: JSON.stringify({ ids: ordered.map((lesson) => lesson.id) }) }); await refresh(); } catch (error) { toast.error((error as ApiError).message); } };
  return (
    <article className="card overflow-hidden">
      <header className="flex flex-wrap items-center justify-between gap-3 border-b bg-stone-50 px-5 py-4"><div className="flex min-w-0 items-center gap-3"><span className="grid h-8 w-8 shrink-0 place-items-center rounded-full bg-forest text-xs font-bold text-white">{index + 1}</span>{editing ? <input className="input !py-2" value={title} onChange={(event) => setTitle(event.target.value)} /> : <div><h3 className="truncate font-display text-lg font-semibold">{section.title}</h3><p className="text-xs text-stone-500">{section.lessons.length} lessons</p></div>}</div><div className="flex items-center gap-1">{editing ? <><button onClick={save} className="btn-primary !px-3 !py-2">Save</button><button onClick={() => setEditing(false)} className="btn !px-3 !py-2">Cancel</button></> : <><IconButton label="Move section up" disabled={index === 0} onClick={() => move(-1)}><ChevronUp size={17} /></IconButton><IconButton label="Move section down" disabled={index === total - 1} onClick={() => move(1)}><ChevronDown size={17} /></IconButton><IconButton label="Rename section" onClick={() => setEditing(true)}><Edit3 size={16} /></IconButton><IconButton label="Delete section" onClick={remove} danger><Trash2 size={16} /></IconButton></>}</div></header>
      <div className="divide-y">{section.lessons.map((lesson, lessonIndex) => <LessonEditor key={lesson.id} lesson={lesson} index={lessonIndex} total={section.lessons.length} move={(direction) => reorderLessons(lessonIndex, direction)} refresh={refresh} />)}</div>
      <div className="p-4">{addingLesson ? <LessonForm onCancel={() => setAddingLesson(false)} onSaved={async () => { setAddingLesson(false); await refresh(); }} sectionId={section.id} /> : <button onClick={() => setAddingLesson(true)} className="btn-secondary w-full"><Plus size={16} /> Add lesson</button>}</div>
    </article>
  );
}

function LessonEditor({ lesson, index, total, move, refresh }: { lesson: Lesson; index: number; total: number; move: (direction: -1 | 1) => void; refresh: () => Promise<void> }) {
  const [editing, setEditing] = useState(false);
  const remove = async () => { if (!window.confirm(`Delete “${lesson.title}”?`)) return; try { await api(`/api/lessons/${lesson.id}`, { method: 'DELETE' }); await refresh(); toast.success('Lesson deleted'); } catch (error) { toast.error((error as ApiError).message); } };
  if (editing) return <div className="p-5"><LessonForm lesson={lesson} onCancel={() => setEditing(false)} onSaved={async () => { setEditing(false); await refresh(); }} /></div>;
  return <div className="flex items-center justify-between gap-3 px-5 py-4"><div className="min-w-0"><p className="truncate text-sm font-semibold">{index + 1}. {lesson.title}</p><p className="mt-1 text-xs text-stone-500">{lesson.durationMinutes} min {lesson.preview && '· Free preview'} {!lesson.published && '· Hidden from learners'}</p></div><div className="flex shrink-0"><IconButton label="Move lesson up" disabled={index === 0} onClick={() => move(-1)}><ChevronUp size={16} /></IconButton><IconButton label="Move lesson down" disabled={index === total - 1} onClick={() => move(1)}><ChevronDown size={16} /></IconButton><IconButton label="Edit lesson" onClick={() => setEditing(true)}><Edit3 size={16} /></IconButton><IconButton label="Delete lesson" onClick={remove} danger><Trash2 size={16} /></IconButton></div></div>;
}

function LessonForm({ lesson, sectionId, onCancel, onSaved }: { lesson?: Lesson; sectionId?: string; onCancel: () => void; onSaved: () => Promise<void> }) {
  const [title, setTitle] = useState(lesson?.title ?? ''); const [description, setDescription] = useState(lesson?.description ?? ''); const [content, setContent] = useState(lesson?.content ?? ''); const [videoUrl, setVideoUrl] = useState(lesson?.videoUrl ?? ''); const [resourceUrl, setResourceUrl] = useState(lesson?.resourceUrl ?? ''); const [durationMinutes, setDuration] = useState(lesson?.durationMinutes ?? 10); const [preview, setPreview] = useState(lesson?.preview ?? false); const [published, setPublished] = useState(lesson?.published ?? true); const [saving, setSaving] = useState(false);
  const save = async () => { if (!title.trim() || !content.trim()) return; setSaving(true); try { await api(lesson ? `/api/lessons/${lesson.id}` : `/api/sections/${sectionId}/lessons`, { method: lesson ? 'PUT' : 'POST', body: JSON.stringify({ title, description: description || null, content, videoUrl: videoUrl || null, resourceUrl: resourceUrl || null, durationMinutes, preview, published }) }); toast.success(lesson ? 'Lesson updated' : 'Lesson added'); await onSaved(); } catch (error) { toast.error((error as ApiError).message); } finally { setSaving(false); } };
  return <div className="space-y-3 rounded-2xl bg-stone-50 p-4"><div className="grid gap-3 sm:grid-cols-[1fr_130px]"><label><span className="label">Lesson title</span><input className="input" value={title} maxLength={160} onChange={(event) => setTitle(event.target.value)} /></label><label><span className="label">Minutes</span><input className="input" type="number" min={0} max={1440} value={durationMinutes} onChange={(event) => setDuration(Number(event.target.value))} /></label></div><label><span className="label">Lesson description</span><input className="input" maxLength={500} value={description} onChange={(event) => setDescription(event.target.value)} /></label><label><span className="label">Lesson content</span><textarea className="input min-h-36" maxLength={50000} value={content} onChange={(event) => setContent(event.target.value)} /></label><div className="grid gap-3 sm:grid-cols-2"><label><span className="label">Video URL <span className="text-stone-400">(optional)</span></span><input className="input" type="url" value={videoUrl} onChange={(event) => setVideoUrl(event.target.value)} /></label><label><span className="label">Resource URL <span className="text-stone-400">(optional)</span></span><input className="input" type="url" value={resourceUrl} onChange={(event) => setResourceUrl(event.target.value)} /></label></div><div className="flex flex-wrap gap-5"><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={preview} onChange={(event) => setPreview(event.target.checked)} /> Free preview</label><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={published} onChange={(event) => setPublished(event.target.checked)} /> Published to learners</label></div><div className="flex gap-2"><button disabled={saving || !title.trim() || !content.trim()} onClick={save} className="btn-primary">{saving ? 'Saving…' : 'Save lesson'}</button><button onClick={onCancel} className="btn-secondary">Cancel</button></div></div>;
}

function CourseRoster({ courseId }: { courseId: string }) {
  const roster = useQuery({ queryKey: ['course-students', courseId], queryFn: () => api<Page<CourseStudent>>(`/api/courses/${courseId}/students?size=100`) });
  return <section><div className="flex items-end justify-between"><div><p className="eyebrow">Enrollments</p><h2 className="mt-2 font-display text-3xl font-bold">Course students</h2></div><span className="text-sm text-stone-500">{roster.data?.totalElements ?? 0} learners</span></div><div className="card mt-5 overflow-x-auto"><table className="w-full text-left text-sm"><thead className="bg-stone-50 text-xs uppercase tracking-wider text-stone-500"><tr><th className="px-5 py-4">Student</th><th className="px-5 py-4">Progress</th><th className="px-5 py-4">Status</th><th className="px-5 py-4">Last active</th></tr></thead><tbody className="divide-y">{roster.data?.content.map((student) => <tr key={student.enrollmentId}><td className="px-5 py-4"><strong>{student.studentName}</strong><span className="block text-xs text-stone-500">{student.studentEmail}</span></td><td className="px-5 py-4">{student.progress}%</td><td className="px-5 py-4">{student.status}</td><td className="px-5 py-4 text-stone-500">{new Date(student.lastAccessedAt).toLocaleDateString()}</td></tr>)}</tbody></table>{roster.data?.content.length === 0 && <p className="p-8 text-center text-sm text-stone-500">No learners have enrolled yet.</p>}</div></section>;
}

function IconButton({ label, onClick, disabled, danger, children }: { label: string; onClick: () => void; disabled?: boolean; danger?: boolean; children: React.ReactNode }) { return <button type="button" aria-label={label} title={label} disabled={disabled} onClick={onClick} className={`rounded-lg p-2 hover:bg-white disabled:opacity-30 ${danger ? 'text-red-600' : 'text-stone-500'}`}>{children}</button>; }
function Checklist({ done, text }: { done: boolean; text: string }) { return <li className={`flex items-center gap-2 ${done ? 'text-forest' : 'text-stone-400'}`}><span className={`grid h-5 w-5 place-items-center rounded-full text-xs ${done ? 'bg-mint' : 'bg-stone-100'}`}>{done ? '✓' : '○'}</span>{text}</li>; }

function InstructorAssignment({ course, users, refresh }: { course: Course; users: AdminUser[]; refresh: () => Promise<void> }) {
  const [instructorId, setInstructorId] = useState(course.instructor.id);
  const [saving, setSaving] = useState(false);
  const approved = users.filter((user) => user.enabled && user.roles.includes('INSTRUCTOR'));
  const assign = async () => {
    setSaving(true);
    try { await api(`/api/courses/${course.id}/instructor`, { method: 'PUT', body: JSON.stringify({ instructorId }) }); toast.success('Course instructor reassigned'); await refresh(); }
    catch (error) { toast.error((error as ApiError).message); }
    finally { setSaving(false); }
  };
  return <div className="card p-6"><h2 className="font-display text-xl font-semibold">Course owner</h2><p className="mt-1 text-sm text-stone-500">Admins can assign an approved instructor.</p><select className="input mt-4" value={instructorId} onChange={(event) => setInstructorId(event.target.value)}>{approved.map((user) => <option key={user.id} value={user.id}>{user.displayName} · {user.email}</option>)}</select><button disabled={saving || instructorId === course.instructor.id} onClick={assign} className="btn-secondary mt-3 w-full">{saving ? 'Assigning…' : 'Assign instructor'}</button></div>;
}
