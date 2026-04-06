import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import AppShell from '../layouts/AppShell';
import Admin from '../pages/Admin';
import Call from '../pages/Call';
import Chat from '../pages/Chat';
import DoctorDashboard from '../pages/DoctorDashboard';
import DoctorProfile from '../pages/DoctorProfile';
import Login from '../pages/Login';
import NotFound from '../pages/NotFound';
import PatientHome from '../pages/PatientHome';
import Prescription from '../pages/Prescription';
import ProtectedRoute from './ProtectedRoute';
import WaitingRoom from '../pages/WaitingRoom';
import { defaultDoctorId } from '../store/mockDoctors';

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route index element={<Navigate replace to="/patient" />} />
            <Route path="/patient" element={<PatientHome />} />
            <Route
              path="/doctor/profile"
              element={<Navigate replace to={`/doctor/profile/${defaultDoctorId}`} />}
            />
            <Route path="/doctor/profile/:doctorId" element={<DoctorProfile />} />
            <Route path="/waiting-room" element={<WaitingRoom />} />
            <Route path="/chat" element={<Chat />} />
            <Route path="/call" element={<Call />} />
            <Route path="/doctor/dashboard" element={<DoctorDashboard />} />
            <Route path="/admin" element={<Admin />} />
            <Route path="/prescription" element={<Prescription />} />
          </Route>
        </Route>
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}
