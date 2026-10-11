# Checklist de deploy

## Configurações obrigatórias

- Defina `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` para o banco de produção.
- Defina `SPRING_PROFILES_ACTIVE=prod`.
- Defina `JWT_SECRET` com uma chave Base64 de ao menos 256 bits e `JWT_ISSUER` com a URL pública da API.
- Defina `FRONTEND_URL` e `app.security.cors.allowed-origins` com as URLs públicas do frontend.
- Não use o perfil `local` ou o perfil `homolog` em produção.
- Credenciais de e-mail e WhatsApp devem ser fornecidas somente por variáveis de ambiente. Nunca entram no repositório.

## Verificação após publicar

- Confirme `GET /actuator/health/liveness` e `GET /actuator/health/readiness` com resposta `UP`.
- Confirme login, criação de paciente e criação de sessão com uma conta de teste.
- Verifique os logs da aplicação e os registros de auditoria para erros de autenticação ou operações inesperadas.

## Container da API

O `Dockerfile` inicia a API com o perfil `prod`. Gere a imagem após executar os testes:

```bash
docker build -t confirma-api .
```

No provedor, informe as variáveis obrigatórias desta lista. A API utiliza `PORT` quando o provedor a fornecer, ou `8080` como padrão. Não inclua arquivos `.env`, `application-local.yml` ou credenciais na imagem.

## Backup e retenção

O backup é responsabilidade da infraestrutura PostgreSQL: configure cópia diária do banco, retenção definida pela operação e teste periódico de restauração em ambiente isolado. As migrações Flyway são aplicadas no início da aplicação; mantenha o backup antes de publicar uma versão nova.

## Monitoramento

- Use os endpoints de liveness e readiness para o monitoramento da plataforma.
- Mantenha logs de aplicação no stdout e centralize-os no provedor de hospedagem.
- A auditoria funcional permanece na tabela `audit_logs`; ela não substitui logs técnicos da infraestrutura.
- Para incluir o SMTP no health check após configurá-lo, defina `MANAGEMENT_HEALTH_MAIL_ENABLED=true`.
