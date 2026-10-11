CREATE TABLE professional_locations (
    professional_id UUID PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    state CHAR(2) NOT NULL,
    timezone VARCHAR(60) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_professional_locations_professional FOREIGN KEY (professional_id) REFERENCES professionals(id),
    CONSTRAINT ck_professional_locations_state CHECK (CHAR_LENGTH(state) = 2)
);
