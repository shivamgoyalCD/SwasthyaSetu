CREATE TABLE conversations (
  id uuid PRIMARY KEY,
  appointment_id uuid UNIQUE NOT NULL,
  patient_id uuid,
  doctor_id uuid,
  status varchar,
  created_at timestamp
);

CREATE TABLE messages (
  id uuid PRIMARY KEY,
  conversation_id uuid NOT NULL REFERENCES conversations(id),
  sender_id uuid NOT NULL,
  type varchar,
  content text,
  original_lang varchar,
  translated_content text,
  created_at timestamp
);

CREATE TABLE attachments (
  id uuid PRIMARY KEY,
  message_id uuid REFERENCES messages(id),
  s3_key varchar,
  mime varchar,
  size bigint,
  created_at timestamp
);

CREATE INDEX idx_messages_conversation_created_at
  ON messages (conversation_id, created_at);
