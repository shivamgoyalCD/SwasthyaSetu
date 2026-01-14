CREATE TABLE users (
  id uuid PRIMARY KEY,
  phone varchar UNIQUE,
  role varchar NOT NULL,
  name varchar,
  language varchar,
  created_at timestamp NOT NULL
);

CREATE TABLE doctor_profile (
  id uuid PRIMARY KEY,
  user_id uuid UNIQUE NOT NULL REFERENCES users(id),
  specialization varchar,
  license_no varchar,
  status varchar,
  experience_years int,
  created_at timestamp NOT NULL
);

CREATE TABLE doctor_documents (
  id uuid PRIMARY KEY,
  doctor_id uuid NOT NULL REFERENCES doctor_profile(id),
  doc_type varchar,
  s3_key varchar,
  status varchar,
  created_at timestamp NOT NULL
);
