CREATE TABLE doctor_availability (
  id uuid PRIMARY KEY,
  doctor_id uuid NOT NULL,
  day_of_week int NOT NULL,
  start_time time NOT NULL,
  end_time time NOT NULL,
  slot_minutes int NOT NULL,
  buffer_minutes int NOT NULL,
  created_at timestamp NOT NULL
);
