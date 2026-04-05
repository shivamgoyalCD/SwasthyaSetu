CREATE TABLE notifications (
  id uuid PRIMARY KEY,
  user_id uuid,
  type varchar,
  payload text,
  status varchar,
  retries int,
  created_at timestamp
);
