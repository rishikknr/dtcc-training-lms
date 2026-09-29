import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './lib/auth';
import type { Role } from './types';
import Layout from './components/Layout';
import Landing from './pages/Landing';
import Catalog from './pages/Catalog';
import CourseDetails from './pages/CourseDetails';
import AuthPage from './pages/AuthPage';
import Dashboard from './pages/Dashboard';
import Learning from './pages/Learning';
import Profile from './pages/Profile';
import CourseEditor from './pages/CourseEditor';
import InstructorApplicationPage from './pages/InstructorApplicationPage';
import ReviewModerationPage from './pages/ReviewModerationPage';
import AdminConsole from './pages/AdminConsole';

function Protected({ children, roles }: { children: React.ReactNode; roles?: Role[] }) {
  const { user, loading } = useAuth();
  if (loading) {
    return (
      <div className="grid min-h-[60vh] place-items-center">
        <div className="h-9 w-9 animate-spin rounded-full border-4 border-stone-200 border-t-forest" />
      </div>
    );
  }
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.some((role) => user.roles.includes(role))) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Landing />} />
        <Route path="courses" element={<Catalog />} />
        <Route path="courses/:id" element={<CourseDetails />} />
        <Route path="login" element={<AuthPage mode="login" />} />
        <Route path="register" element={<AuthPage mode="register" />} />
        <Route path="dashboard" element={<Protected><Dashboard /></Protected>} />
        <Route path="learn/:id" element={<Protected roles={['STUDENT']}><Learning /></Protected>} />
        <Route path="profile" element={<Protected><Profile /></Protected>} />
        <Route path="become-instructor" element={<Protected roles={['STUDENT']}><InstructorApplicationPage /></Protected>} />
        <Route path="instructor/courses/new" element={<Protected roles={['INSTRUCTOR', 'ADMIN']}><CourseEditor /></Protected>} />
        <Route path="instructor/courses/:id" element={<Protected roles={['INSTRUCTOR', 'ADMIN']}><CourseEditor /></Protected>} />
        <Route path="instructor/reviews" element={<Protected roles={['INSTRUCTOR', 'ADMIN']}><ReviewModerationPage /></Protected>} />
        <Route path="admin" element={<Protected roles={['ADMIN']}><AdminConsole /></Protected>} />
        <Route
          path="*"
          element={
            <div className="container-page py-28 text-center">
              <p className="eyebrow">404</p>
              <h1 className="mt-3 font-display text-4xl font-bold">That lesson wandered off.</h1>
            </div>
          }
        />
      </Route>
    </Routes>
  );
}
