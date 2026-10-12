ALTER TABLE sessions ADD COLUMN payment_method VARCHAR(20);
ALTER TABLE sessions ADD CONSTRAINT ck_session_payment_method CHECK (payment_method IS NULL OR (payment_status = 'PAID' AND payment_method IN ('PIX','CASH','CARD','BANK_TRANSFER','OTHER')));

CREATE TABLE session_financial_history (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES sessions(id),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_name VARCHAR(150) NOT NULL,
    before_state JSONB NOT NULL,
    after_state JSONB NOT NULL
);
CREATE INDEX idx_session_financial_history_session ON session_financial_history(session_id, changed_at DESC, id DESC);

CREATE FUNCTION record_session_financial_change() RETURNS TRIGGER AS $$
DECLARE
    previous JSONB;
    current_state JSONB;
    actor VARCHAR(150);
BEGIN
    previous = jsonb_build_object('financialAmount', OLD.financial_amount, 'paymentStatus', OLD.payment_status, 'paymentDate', OLD.payment_date, 'financialNotes', OLD.financial_notes, 'paymentMethod', OLD.payment_method);
    current_state = jsonb_build_object('financialAmount', NEW.financial_amount, 'paymentStatus', NEW.payment_status, 'paymentDate', NEW.payment_date, 'financialNotes', NEW.financial_notes, 'paymentMethod', NEW.payment_method);
    IF previous IS DISTINCT FROM current_state THEN
        actor = NULLIF(current_setting('app.finance_actor', true), '');
        INSERT INTO session_financial_history(session_id, actor_name, before_state, after_state)
        VALUES (NEW.id, COALESCE(actor, 'Sistema (agenda ou WhatsApp)'), previous, current_state);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_session_financial_history AFTER UPDATE ON sessions
FOR EACH ROW EXECUTE FUNCTION record_session_financial_change();
