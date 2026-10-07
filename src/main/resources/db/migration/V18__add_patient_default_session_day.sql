ALTER TABLE patients ADD COLUMN default_session_weekday SMALLINT;
ALTER TABLE patients ADD CONSTRAINT ck_patients_default_session_weekday
    CHECK (default_session_weekday IS NULL OR default_session_weekday BETWEEN 1 AND 7);
