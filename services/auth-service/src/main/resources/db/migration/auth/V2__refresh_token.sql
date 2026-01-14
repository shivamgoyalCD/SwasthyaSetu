CREATE TABLE refresh_token (
  id uuid PRIMARY KEY,
  user_id uuid NOT NULL,
  token_hash varchar NOT NULL,
  expires_at timestamp NOT NULL,
  revoked boolean NOT NULL DEFAULT false,
  created_at timestamp NOT NULL
);
