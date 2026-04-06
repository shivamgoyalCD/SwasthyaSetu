import { useNavigate } from 'react-router-dom';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import StatCard from '../components/StatCard';
import usePageMeta from '../hooks/usePageMeta';
import { mockDoctors } from '../store/mockDoctors';

export default function PatientHome() {
  usePageMeta('Patient Home', 'Browse a mock doctor list and open doctor profiles.');
  const navigate = useNavigate();

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Patient workspace"
        title="Browse doctors and open a profile before booking a consultation."
        description="This patient home page uses a hardcoded doctor list for discovery and navigation. It does not call the user-service yet."
        actions={
          <>
            <button className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white">
              Explore doctors
            </button>
            <button className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700">
              View specialties
            </button>
          </>
        }
      />

      <div className="page-grid">
        <StatCard
          label="Doctors available"
          value={String(mockDoctors.length)}
          hint="Hardcoded profiles for frontend flow testing"
        />
        <StatCard
          label="Earliest slot"
          value={mockDoctors[0].nextAvailable}
          hint="Based on the first available doctor in the mock list"
        />
        <StatCard
          label="Top specialty"
          value={mockDoctors[0].specialty}
          hint="Use doctor cards below to open full profiles"
        />
      </div>

      <div className="grid gap-5 xl:grid-cols-[1.2fr_0.8fr]">
        <SectionCard title="Available doctors" eyebrow="Mock directory">
          <div className="space-y-4">
            {mockDoctors.map((doctor) => (
              <button
                key={doctor.id}
                className="w-full rounded-2xl border border-slate-200/80 bg-slate-50/80 p-4 text-left transition hover:border-spruce/40 hover:bg-white"
                onClick={() => navigate(`/doctor/profile/${doctor.id}`)}
                type="button"
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div className="space-y-2">
                    <div>
                      <p className="font-semibold text-ink">{doctor.name}</p>
                      <p className="mt-1 text-sm text-slate-500">
                        {doctor.specialty} · {doctor.experience} · {doctor.location}
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {doctor.focusAreas.map((item) => (
                        <span
                          key={item}
                          className="rounded-full bg-white px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-spruce"
                        >
                          {item}
                        </span>
                      ))}
                    </div>
                  </div>
                  <div className="min-w-[170px] space-y-2 lg:text-right">
                    <p className="text-sm font-semibold text-ink">Rs. {doctor.fee}</p>
                    <p className="text-sm text-slate-500">Next: {doctor.nextAvailable}</p>
                    <p className="text-sm text-slate-500">
                      Rating {doctor.rating} · {doctor.reviews} reviews
                    </p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </SectionCard>

        <SectionCard title="How this flow works" eyebrow="Frontend only">
          <div className="space-y-3 text-sm leading-7 text-slate-600">
            <div className="rounded-2xl bg-white/80 p-4">
              The doctor directory is hardcoded in the frontend so you can continue building the
              patient-to-profile flow without waiting on `user-service`.
            </div>
            <div className="rounded-2xl bg-white/80 p-4">
              Clicking any doctor card navigates to `DoctorProfile` using a `doctorId` route
              parameter.
            </div>
            <div className="rounded-2xl bg-white/80 p-4">
              Once the real directory endpoint exists, this list can be swapped from mock data to an
              API response with minimal routing changes.
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}
