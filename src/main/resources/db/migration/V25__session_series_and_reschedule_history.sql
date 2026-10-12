ALTER TABLE sessions ADD COLUMN series_id UUID;
ALTER TABLE sessions ADD COLUMN series_position INTEGER;
ALTER TABLE sessions ADD CONSTRAINT ck_session_series CHECK ((series_id IS NULL AND series_position IS NULL) OR (series_id IS NOT NULL AND series_position IS NOT NULL AND series_position >= 0));
CREATE UNIQUE INDEX idx_sessions_series_position ON sessions(series_id,series_position);
CREATE TABLE session_reschedule_history (
 id UUID PRIMARY KEY,
 session_id UUID NOT NULL REFERENCES sessions(id),
 previous_start TIMESTAMP WITH TIME ZONE NOT NULL,
 previous_end TIMESTAMP WITH TIME ZONE NOT NULL,
 new_start TIMESTAMP WITH TIME ZONE NOT NULL,
 new_end TIMESTAMP WITH TIME ZONE NOT NULL,
 reason VARCHAR(500),
 actor_name VARCHAR(150) NOT NULL,
 changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT ck_reschedule_period CHECK (previous_end > previous_start AND new_end > new_start)
);
CREATE INDEX idx_reschedule_history_session ON session_reschedule_history(session_id,changed_at DESC);
