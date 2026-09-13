-- Professional e User representam o mesmo titular. Mantém os dados de acesso
-- alinhados com o perfil profissional após atualizações anteriores.
UPDATE users u
SET name = p.full_name,
    email = p.email,
    updated_at = CURRENT_TIMESTAMP
FROM professionals p
WHERE u.professional_id = p.id
  AND u.role = 'PROFESSIONAL';
