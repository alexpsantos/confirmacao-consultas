ALTER TABLE sessions ADD COLUMN financial_amount_overridden BOOLEAN NOT NULL DEFAULT FALSE;
-- Preserve previously edited amounts and settled financial entries.
UPDATE sessions SET financial_amount_overridden = TRUE
WHERE payment_status <> 'PENDING'
   OR financial_amount <> (SELECT p.default_session_price FROM professionals p WHERE p.id = sessions.professional_id);
