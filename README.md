# Confirmação de Consultas

API REST para gerenciamento de clínicas (tenants) e seus profissionais. O projeto foi desenvolvido com Java e Spring Boot, utilizando PostgreSQL em execução local e Flyway para versionamento do banco de dados.

## Funcionalidades

- Cadastro, consulta, atualização, ativação e desativação de tenants.
- Listagem paginada de tenants, com filtro por status e ordenação.
- Cadastro, consulta, atualização, ativação e desativação de profissionais por tenant.
- Listagem paginada e ordenada de profissionais.
- Validação de timezone no padrão IANA, como `America/Sao_Paulo`.
- Bloqueio das operações de profissionais quando o tenant está inativo.
- Registro profissional único dentro de cada tenant.
- Respostas de erro padronizadas para validações, recursos não encontrados e conflitos.
- Documentação interativa com OpenAPI e Swagger UI.
- Coleção Postman versionada com variáveis e testes automáticos.

## Tecnologias

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Flyway
- H2 para testes
- JUnit 5 e Mockito
- Springdoc OpenAPI
- Maven Wrapper

## Requisitos

Antes de iniciar, instale:

- [Java 21](https://adoptium.net/temurin/releases/?version=21)
- [PostgreSQL](https://www.postgresql.org/download/)
- [Git](https://git-scm.com/downloads)
- [Postman](https://www.postman.com/downloads/) — opcional, para testar a API

Não é necessário instalar o Maven separadamente, pois o projeto inclui o Maven Wrapper.

Para verificar o Java instalado:

```bash
java -version
```

## Clonando o projeto

```bash
git clone https://github.com/alexpsantos/confirmacao-consultas.git
cd confirmacao-consultas
```

## Configurando o PostgreSQL

Crie um banco vazio. As tabelas serão criadas automaticamente pelo Flyway quando a aplicação iniciar.

```sql
CREATE DATABASE confirmacao_consultas;
```

Configure as credenciais por variáveis de ambiente. Não coloque a senha real em arquivos enviados ao Git.

### Windows PowerShell

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/confirmacao_consultas"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="sua_senha"
```

### Linux ou macOS

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/confirmacao_consultas"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="sua_senha"
```

Se o PostgreSQL estiver em outra porta ou utilizar outro usuário, altere os valores de acordo com seu ambiente.

## Executando a aplicação

### Windows

No mesmo PowerShell em que as variáveis foram configuradas:

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux ou macOS

```bash
./mvnw spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

Ao iniciar, o Flyway executa as migrações existentes em `src/main/resources/db/migration`.

## Executando pelo IntelliJ IDEA

1. Abra a pasta do projeto no IntelliJ IDEA.
2. Aguarde a importação das dependências do Maven.
3. Abra a configuração de execução da classe `ConfirmacaoConsultasApplication`.
4. Adicione as três variáveis abaixo em **Environment variables**:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/confirmacao_consultas
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=sua_senha
```

5. Execute a classe `ConfirmacaoConsultasApplication`.

## Documentação da API

Com a aplicação em execução:

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

O Swagger UI permite consultar os contratos e realizar chamadas diretamente pelo navegador.

## Endpoints principais

### Tenants

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/tenants` | Cadastra um tenant |
| `GET` | `/api/v1/tenants` | Lista tenants com paginação e filtro opcional `active` |
| `GET` | `/api/v1/tenants/{id}` | Busca um tenant por ID |
| `PUT` | `/api/v1/tenants/{id}` | Atualiza um tenant |
| `DELETE` | `/api/v1/tenants/{id}` | Desativa um tenant |
| `PATCH` | `/api/v1/tenants/{id}/activate` | Ativa um tenant |

Exemplo de listagem paginada e filtrada:

```text
GET /api/v1/tenants?active=true&page=0&size=20&sort=displayName,asc
```

### Profissionais

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/tenants/{tenantId}/professionals` | Cadastra um profissional |
| `GET` | `/api/v1/tenants/{tenantId}/professionals` | Lista profissionais com paginação |
| `GET` | `/api/v1/tenants/{tenantId}/professionals/{professionalId}` | Busca um profissional por ID |
| `PUT` | `/api/v1/tenants/{tenantId}/professionals/{professionalId}` | Atualiza um profissional |
| `DELETE` | `/api/v1/tenants/{tenantId}/professionals/{professionalId}` | Desativa um profissional |
| `PATCH` | `/api/v1/tenants/{tenantId}/professionals/{professionalId}/activate` | Ativa um profissional |

Exemplo de listagem paginada e ordenada:

```text
GET /api/v1/tenants/{tenantId}/professionals?page=0&size=20&sort=fullName,asc
```

## Coleção Postman

A coleção versionada está em:

```text
postman/confirmacao-consultas.postman_collection.json
```

Para importar:

1. Abra o Postman.
2. Clique em **Import**.
3. Selecione o arquivo da pasta `postman`.
4. Execute primeiro **Criar tenant** e depois **Criar profissional**.

A coleção possui as variáveis `baseUrl`, `tenantId` e `professionalId`. As requisições de criação armazenam automaticamente os IDs retornados, permitindo executar as demais chamadas sem copiar UUIDs manualmente.

## Executando os testes

Os testes utilizam o banco H2 em memória e não alteram os dados do PostgreSQL local.

### Windows

```powershell
.\mvnw.cmd test
```

### Linux ou macOS

```bash
./mvnw test
```

Também é possível executar uma classe ou pacote de testes pelo IntelliJ IDEA usando **Run with Coverage** para visualizar a cobertura.

## Gerando o arquivo executável

### Windows

```powershell
.\mvnw.cmd clean package
java -jar target\confirmacao-consultas-0.0.1-SNAPSHOT.jar
```

### Linux ou macOS

```bash
./mvnw clean package
java -jar target/confirmacao-consultas-0.0.1-SNAPSHOT.jar
```

As variáveis de conexão com o PostgreSQL também precisam estar configuradas ao executar o arquivo JAR.

## Estrutura do projeto

```text
src/main/java/br/com/confirmacao
├── tenant          # Regras e endpoints de tenants
├── professional    # Regras e endpoints de profissionais
└── shared          # Respostas, erros e configurações compartilhadas

src/main/resources
└── db/migration    # Migrações versionadas do Flyway

src/test/java       # Testes automatizados
postman             # Coleção Postman versionada
```

## Observações

- Exclusões são lógicas: os endpoints `DELETE` desativam os registros.
- Um tenant inativo não permite operações em seus profissionais.
- Use timezones reconhecidos pelo Java, preferencialmente no padrão região/cidade, como `America/Sao_Paulo`.

## Financeiro por sessão

Cada sessão armazena financialAmount, paymentStatus (PENDING, PAID, EXEMPT, CANCELED), paymentDate e financialNotes. PATCH /api/v1/sessions/{id}/finance permite editar esses campos, inclusive após o atendimento, sempre limitado ao profissional autenticado. PAID exige data de pagamento; as demais situações não aceitam data. A observação financeira é independente da observação clínica.

O valor padrão persistido do profissional é copiado na criação de sessões individuais e séries. Alterar esse padrão atualiza somente sessões agendadas ou confirmadas que ainda não começaram, com lançamento pendente e sem valor personalizado. Sessões passadas e valores personalizados são preservados. A migração V23 registra o controle de valores personalizados. Cancelamentos pela agenda ou WhatsApp cancelam o lançamento e removem a data de pagamento, preservando uma eventual isenção.

A migração V22 inicializa sessões existentes com o padrão atual do profissional e situação pendente (ou cancelada para sessões canceladas). Sessões realizadas não são presumidas pagas: revise seus lançamentos. O front calcula recebido no mês pela data de pagamento e pendente pela situação financeira; canceladas e isentas são excluídas dos totais.

### Filtros, forma de pagamento e histórico financeiro

A aba Financeiro permite filtrar por intervalo de datas, situação e data de referência (sessão ou pagamento). Tabela e totais usam os mesmos filtros. Total financeiro = recebido + pendente; isenções são apresentadas separadamente e cancelamentos são excluídos. Com intervalo por pagamento, lançamentos sem data de pagamento não aparecem.

Lançamentos pagos podem informar Pix, dinheiro, cartão, transferência ou outro. A observação tem contador de 500 caracteres e pode ser expandida na tabela.

A migração V24 cria um histórico persistido com os dados anteriores e posteriores, responsável e horário. O registro ocorre na mesma transação da alteração; atualizações sem mudança e transações revertidas não geram histórico. Alterações de preferência em sessões futuras e cancelamentos também são registrados. GET /api/v1/sessions/{id}/finance/history exige que a sessão pertença ao profissional autenticado. O histórico começa após a migração e não reconstrói alterações anteriores.

### Reservas simultâneas da agenda

Agendamento individual, séries, reagendamento e criação de bloqueios adquirem uma trava de escrita no registro do profissional antes de consultar disponibilidade. A trava permanece até o término da transação e vale entre abas, dispositivos e instâncias da API conectadas ao mesmo banco. A segunda reserva consulta novamente a disponibilidade após o commit da primeira e retorna HTTP 409 quando houver sobreposição. Profissionais diferentes não compartilham essa trava. Não é necessária nova migração.

ScheduleConcurrencyTest verifica disputas entre sessões, bloqueios, séries e reagendamentos usando transações independentes e também verifica que outro profissional pode reservar em paralelo.

### Financeiro consolidado do profissional

GET /api/v1/sessions/finance consulta lançamentos de todos os pacientes do profissional autenticado. Filtros opcionais: patientId, from/to (datas inclusivas), basis=SESSION|PAYMENT, status=PENDING|PAID|EXEMPT|CANCELED, method=PIX|CASH|CARD|BANK_TRANSFER|OTHER e unspecifiedMethod=true. Paginação: page (inicia em zero) e size (1 a 100; padrão 20).

A resposta contém entries (página de sessões) e totals (received, pending, exempt, total). Os totais consideram todos os registros filtrados, independentemente da página, usando o mesmo filtro da tabela. Total = recebido + pendente; cancelamentos são excluídos e isenções apresentadas separadamente. Datas de sessão usam o fuso persistido do profissional; datas de pagamento são datas civis. A consulta não permite acesso financeiro de outros profissionais e não exige migração adicional.

### Reagendamento individual e das próximas sessões

A migração V25 vincula novas séries por series_id e series_position e cria o histórico de reagendamentos. Séries anteriores não são agrupadas por inferência, pois não há identificação confiável no cadastro antigo.

PATCH /api/v1/sessions/{id}/reschedule recebe startsAt, endsAt, reason (opcional, até 500 caracteres) e scope=ONE|FOLLOWING. ONE altera apenas a sessão escolhida; FOLLOWING altera essa e as próximas sessões futuras agendadas/confirmadas da mesma série, preservando os intervalos semanais no fuso do profissional. Canceladas, realizadas, faltas e sessões passadas são preservadas. O novo horário e duração são aplicados às sessões selecionadas; financeiro e observações clínicas são mantidos.

Todos os novos horários são verificados com a trava de agenda do profissional. Conflito em qualquer data impede o reagendamento inteiro. O histórico guarda início/término anteriores e novos, motivo, responsável e momento da alteração. Alterações de horário pelo formulário geral também geram histórico. GET /api/v1/sessions/{id}/reschedule/history limita a consulta ao profissional autenticado. O histórico pode ser consultado nos detalhes da sessão; não reconstrói alterações anteriores à V25.

### Cancelamento individual ou em série

PATCH /api/v1/sessions/{id}/cancel recebe scope=ONE|FOLLOWING e reason opcional. FOLLOWING cancela a sessão selecionada e as próximas futuras agendadas/confirmadas da mesma série, mantendo as anteriores e as finalizadas. Os lançamentos são cancelados, preservando isenções; o motivo fica separado da observação clínica. A migração V26 adiciona cancellation_reason.

Nos detalhes de uma sessão cancelada futura, Agendar nova sessão neste horário abre um novo cadastro com paciente e horário preenchidos. A sessão cancelada permanece no histórico e a nova reserva passa pelas verificações de disponibilidade e proteção contra concorrência.
