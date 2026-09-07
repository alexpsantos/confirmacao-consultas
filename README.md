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
