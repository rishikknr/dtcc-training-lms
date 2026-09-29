import { Link } from 'react-router-dom';
import { ArrowRight, BookOpen, ChartNoAxesColumnIncreasing, GraduationCap, MessageSquareText, Plus, ShieldCheck, Star, Users } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useAuth } from '../lib/auth';
import type { AdminStats, CourseSummary, Enrollment, InstructorStats, Page, Review, StudentStats } from '../types';
import CourseCard from '../components/CourseCard';
import { Empty, LoadingCards } from '../components/States';

export default function Dashboard() {
  const { user } = useAuth();
  if (user?.roles.includes('ADMIN')) return <AdminDashboard />;
  if (user?.roles.includes('INSTRUCTOR')) return <InstructorDashboard />;
  return <StudentDashboard />;
}

function Heading({ eyebrow, title, children }: { eyebrow: string; title: string; children?: React.ReactNode }) {
  return <div className="flex flex-wrap items-end justify-between gap-4"><div><p className="eyebrow">{eyebrow}</p><h1 className="mt-3 font-display text-4xl font-bold">{title}</h1></div>{children}</div>;
}

function StudentDashboard() {
  const { user } = useAuth();
  const enrollments = useQuery({ queryKey: ['enrollments'], queryFn: () => api<Enrollment[]>('/api/enrollments') });
  const stats = useQuery({ queryKey: ['student-stats'], queryFn: () => api<StudentStats>('/api/dashboard/student') });
  const reviews = useQuery({ queryKey: ['my-reviews'], queryFn: () => api<Page<Review>>('/api/reviews/mine?size=10') });
  return (
    <section className="container-page py-14">
      <Heading eyebrow="Your learning space" title={`Welcome back, ${user?.displayName.split(' ')[0]}.`} />
      <div className="mt-8 grid grid-cols-3 gap-3 sm:max-w-2xl"><Stat icon={<BookOpen />} label="Enrolled" value={stats.data?.enrolledCourses ?? '—'} /><Stat icon={<GraduationCap />} label="Completed" value={stats.data?.completedCourses ?? '—'} /><Stat icon={<ChartNoAxesColumnIncreasing />} label="Avg. progress" value={stats.data ? `${stats.data.averageProgress}%` : '—'} /></div>
      <div className="mt-10 grid gap-6 lg:grid-cols-[1fr_300px]">
        <div><h2 className="font-display text-2xl font-semibold">Continue learning</h2><div className="mt-5">{enrollments.isLoading ? <LoadingCards /> : !enrollments.data?.length ? <Empty title="Your next course is waiting" message="Explore the catalog and start building a new skill." /> : <div className="grid gap-5 md:grid-cols-2">{enrollments.data.map((enrollment) => <div key={enrollment.id} className="card overflow-hidden"><div className="p-5"><p className="text-xs font-semibold uppercase tracking-wider text-forest">{enrollment.course.categoryName}</p><h3 className="mt-2 font-display text-xl font-semibold">{enrollment.course.title}</h3><div className="mt-5 flex justify-between text-xs text-stone-500"><span>Progress</span><span>{enrollment.progress}%</span></div><div className="mt-2 h-2 overflow-hidden rounded-full bg-stone-100"><div className="h-full rounded-full bg-coral transition-all" style={{ width: `${enrollment.progress}%` }} /></div><Link to={`/learn/${enrollment.course.id}`} className="btn-primary mt-5 w-full">Continue <ArrowRight size={16} /></Link></div></div>)}</div>}</div></div>
        <aside className="space-y-5"><div className="card bg-forest p-6 text-white"><Star className="text-mint" /><h3 className="mt-8 font-display text-2xl font-semibold">Keep the momentum</h3><p className="mt-3 text-sm leading-6 text-white/70">Complete one lesson today. Your progress is saved lesson by lesson.</p></div><div className="card p-6"><GraduationCap className="text-forest" /><h3 className="mt-5 font-display text-xl font-semibold">Have expertise to share?</h3><p className="mt-2 text-sm leading-6 text-stone-600">Apply for instructor access. An admin reviews every request.</p><Link to="/become-instructor" className="btn-secondary mt-5 w-full">Become an instructor</Link></div></aside>
      </div>
      <div className="mt-12"><h2 className="font-display text-2xl font-semibold">Your reviews</h2>{reviews.data?.content.length ? <div className="mt-5 grid gap-4 md:grid-cols-2">{reviews.data.content.map((review) => <Link key={review.id} to={`/courses/${review.courseId}`} className="card p-5 transition hover:border-forest"><div className="flex items-center justify-between"><strong>{review.courseTitle}</strong><span className="text-amber-500">{'★'.repeat(review.rating)}</span></div><p className="mt-2 line-clamp-2 text-sm text-stone-600">{review.comment}</p><span className={`mt-3 inline-block text-xs font-semibold ${review.status === 'PUBLISHED' ? 'text-emerald-700' : 'text-amber-700'}`}>{review.status}</span></Link>)}</div> : <p className="mt-3 text-sm text-stone-500">Reviews you write for enrolled courses will appear here.</p>}</div>
    </section>
  );
}

function InstructorDashboard() {
  const courses = useQuery({ queryKey: ['managed'], queryFn: () => api<Page<CourseSummary>>('/api/courses/managed') });
  const stats = useQuery({ queryKey: ['instructor-stats'], queryFn: () => api<InstructorStats>('/api/dashboard/instructor') });
  return (
    <section className="container-page py-14">
      <Heading eyebrow="Instructor studio" title="Teach what matters."><div className="flex gap-2"><Link to="/instructor/reviews" className="btn-secondary"><MessageSquareText size={17} /> Reviews</Link><Link to="/instructor/courses/new" className="btn-primary"><Plus size={17} /> New course</Link></div></Heading>
      <div className="mt-10 grid grid-cols-2 gap-4 lg:grid-cols-5"><Stat icon={<BookOpen />} label="Courses" value={stats.data?.courses ?? '—'} /><Stat icon={<ShieldCheck />} label="Published" value={stats.data?.publishedCourses ?? '—'} /><Stat icon={<Users />} label="Learners" value={stats.data?.learners ?? '—'} /><Stat icon={<Star />} label="Avg. rating" value={stats.data ? stats.data.averageRating.toFixed(1) : '—'} /><Stat icon={<ChartNoAxesColumnIncreasing />} label="Completion" value={stats.data ? `${stats.data.averageCompletion}%` : '—'} /></div>
      <h2 className="mt-12 font-display text-2xl font-semibold">Your courses</h2>
      <div className="mt-5">{courses.isLoading ? <LoadingCards /> : !courses.data?.content.length ? <Empty title="Create your first course" message="Start with a focused outcome, then build the curriculum lesson by lesson." /> : <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">{courses.data.content.map((course) => <div key={course.id}><CourseCard course={course} /><div className="mt-3 flex items-center justify-between"><span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${course.status === 'PUBLISHED' ? 'bg-emerald-50 text-emerald-700' : course.status === 'DRAFT' ? 'bg-amber-50 text-amber-700' : 'bg-stone-100 text-stone-600'}`}>{course.status}</span><Link to={`/instructor/courses/${course.id}`} className="text-sm font-semibold text-forest">Manage course →</Link></div></div>)}</div>}</div>
    </section>
  );
}

function AdminDashboard() {
  const stats = useQuery({ queryKey: ['admin-stats'], queryFn: () => api<AdminStats>('/api/dashboard/admin') });
  const courses = useQuery({ queryKey: ['managed'], queryFn: () => api<Page<CourseSummary>>('/api/courses/managed?size=6') });
  return (
    <section className="container-page py-14">
      <Heading eyebrow="Platform operations" title="Admin overview"><div className="flex gap-2"><Link to="/instructor/reviews" className="btn-secondary">Moderate reviews</Link><Link to="/admin" className="btn-primary"><ShieldCheck size={17} /> Admin console</Link></div></Heading>
      <div className="mt-10 grid grid-cols-2 gap-4 lg:grid-cols-6"><Stat icon={<Users />} label="Users" value={stats.data?.users ?? '—'} /><Stat icon={<BookOpen />} label="Courses" value={stats.data?.courses ?? '—'} /><Stat icon={<ChartNoAxesColumnIncreasing />} label="Enrollments" value={stats.data?.enrollments ?? '—'} /><Stat icon={<Star />} label="Reviews" value={stats.data?.reviews ?? '—'} /><Stat icon={<GraduationCap />} label="Applications" value={stats.data?.pendingInstructorApplications ?? '—'} /><Stat icon={<ShieldCheck />} label="Hidden" value={stats.data?.hiddenReviews ?? '—'} /></div>
      <div className="mt-12 flex items-center justify-between"><h2 className="font-display text-2xl font-semibold">Recent courses</h2><Link to="/instructor/courses/new" className="text-sm font-semibold text-forest">Create course</Link></div>
      <div className="mt-5 grid gap-6 md:grid-cols-2 lg:grid-cols-3">{courses.data?.content.map((course) => <div key={course.id}><CourseCard course={course} /><Link to={`/instructor/courses/${course.id}`} className="btn-secondary mt-3 w-full">Administer course</Link></div>)}</div>
    </section>
  );
}

function Stat({ icon, label, value }: { icon: React.ReactNode; label: string; value: string | number }) {
  return <div className="card p-5"><div className="text-forest">{icon}</div><p className="mt-5 font-display text-3xl font-bold">{value}</p><p className="mt-1 text-sm text-stone-500">{label}</p></div>;
}
