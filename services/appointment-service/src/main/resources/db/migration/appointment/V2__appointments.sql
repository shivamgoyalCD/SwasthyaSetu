CREATE TABLE appointments (
  id uuid PRIMARY KEY,
  patient_id uuid NOT NULL,
  doctor_id uuid NOT NULL,
  start_ts timestamp NOT NULL,
  end_ts timestamp NOT NULL,
  status varchar NOT NULL,
  created_at timestamp NOT NULL
);

ALTER TABLE appointments
  ADD CONSTRAINT uk_appointments_doctor_start_ts UNIQUE (doctor_id, start_ts);

CREATE INDEX idx_appointments_patient_start_ts
  ON appointments (patient_id, start_ts);
