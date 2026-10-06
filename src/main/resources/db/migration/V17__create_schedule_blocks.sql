CREATE TABLE schedule_blocks (
    id UUID PRIMARY KEY,
    professional_id UUID NOT NULL REFERENCES professionals(id),
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_schedule_blocks_period CHECK (ends_at > starts_at)
);

CREATE INDEX idx_schedule_blocks_professional_period ON schedule_blocks(professional_id, starts_at, ends_at);
