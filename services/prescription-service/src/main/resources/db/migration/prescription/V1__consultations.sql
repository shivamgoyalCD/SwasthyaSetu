CREATE TABLE consultations (
  id uuid PRIMARY KEY,
  appointment_id uuid UNIQUE NOT NULL,
  status varchar,
  started_at timestamp,
  ended_at timestamp,
  created_at timestamp
);

CREATE TABLE llm_summary (
  id uuid PRIMARY KEY,
  consultation_id uuid UNIQUE NOT NULL REFERENCES consultations(id),
  json_summary text,
  created_at timestamp
);

CREATE TABLE prescriptions (
  id uuid PRIMARY KEY,
  consultation_id uuid UNIQUE NOT NULL REFERENCES consultations(id),
  pdf_s3_key varchar,
  created_at timestamp
);
