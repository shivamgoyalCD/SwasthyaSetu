CREATE TABLE otp_session (
  id uuid PRIMARY KEY,
  phone varchar NOT NULL,
  otp_hash varchar NOT NULL,
  expires_at timestamp NOT NULL,
  attempts int NOT NULL DEFAULT 0,
  status varchar NOT NULL,
  created_at timestamp NOT NULL
);

CREATE INDEX idx_otp_session_phone ON otp_session (phone);
