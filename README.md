# CarManual API

API REST para gerenciamento de planos, clientes, mecânicos, manuais, diagnósticos e avaliações de serviços automotivos.

O projeto foi desenvolvido com Spring Boot e MySQL, com endpoints prontos para testes no Postman.

## Tecnologias

- Java 17+
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA / Hibernate
- MySQL
- Maven
- Postman

## Funcionalidades

- Cadastro, consulta, atualização e exclusão de planos
- Cadastro de clientes e associação com planos
- Cadastro de mecânicos e cálculo da média das avaliações
- Cadastro de manuais técnicos
- Registro de diagnósticos vinculados a clientes e manuais
- Registro de avaliações vinculadas a clientes e mecânicos
- Modo seguro para testar exclusões sem apagar dados, usando `dryRun=true`

## Pré-requisitos

- Java 17 ou superior
- Maven 3.9+
- MySQL Server ou MariaDB
- Postman, opcionalmente, para testar a API

## Configuração do banco

Crie o banco no MySQL:

```sql
CREATE DATABASE IF NOT EXISTS carmanual
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

O script também está disponível em [database/carmanual.sql](database/carmanual.sql).

Configure as credenciais por variáveis de ambiente:

```powershell
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "sua-senha"
$env:JWT_SECRET = "uma-chave-secreta-com-pelo-menos-32-caracteres"
```

Por padrão, a aplicação usa `root` e senha vazia caso as variáveis do MySQL não sejam definidas. `JWT_SECRET` é obrigatória para iniciar a autenticação. Nunca publique senhas ou chaves reais no repositório.

## Executando localmente

Na raiz do projeto:

```bash
mvn clean package
mvn spring-boot:run
```

Ou execute o JAR gerado:

```bash
java -jar target/CarManual-0.0.1-SNAPSHOT.jar
```

A API ficará disponível em `http://localhost:8080`.

## Endpoints principais

| Recurso | Rota base |
| --- | --- |
| Planos | `/plano` |
| Clientes | `/cliente` |
| Mecânicos | `/mecanico` |
| Manuais | `/manual` |
| Diagnósticos | `/diagnostico` |
| Avaliações | `/avaliacao` |

Exemplo de criação de avaliação:

```http
POST http://localhost:8080/avaliacao
Content-Type: application/json
```

```json
{
	"nota": 5,
	"comentario": "Excelente atendimento!",
	"cliente": { "id": 1 },
	"mecanico": { "id": 1 }
}
```

Para criar um diagnóstico, os relacionamentos podem ser informados como parâmetros:

```http
POST http://localhost:8080/diagnostico?idCliente=1&idManual=1
```

```json
{
	"descricao": "Ruído no motor",
	"resultado": "Necessária revisão",
	"idVeic": 1
}
```

Para testar uma exclusão sem remover o registro, acrescente `dryRun=true`:

```http
DELETE http://localhost:8080/avaliacao/1?dryRun=true
```

Nesse modo, a API retorna o registro e preserva os dados do banco.

## Postman

Importe a coleção [postman/CarManual.postman_collection.json](postman/CarManual.postman_collection.json). A ordem sugerida é:

1. Criar plano
2. Criar cliente
3. Criar mecânico
4. Criar manual
5. Criar diagnóstico
6. Criar avaliação

## Tratamento de exceções

As respostas de erro usam o formato JSON abaixo:

```json
{
	"status": 404,
	"message": "Plano não encontrado com ID: 9999",
	"timestamp": "2026-05-03T14:20:45.321"
}
```

- `404 Not Found`: entidade não encontrada pelo ID solicitado.
- `400 Bad Request`: corpo válido em JSON que falha nas validações dos campos.
- `500 Internal Server Error`: erro inesperado. O detalhe interno não é exposto ao cliente.

Para exercitar esses casos sem persistir registros, importe a coleção do Postman e consulte as instruções em [postman/exception-tests.md](postman/exception-tests.md). O caso de JSON malformado envia um corpo inválido para `POST /diagnostico` e verifica o handler genérico.

## Estrutura

```text
CarManual/
├── database/       # Script de criação do banco
├── postman/        # Coleção de requisições
├── src/main/java/  # Controllers, services, models e repositories
├── src/main/resources/
│   ├── application.properties
│   └── data.sql
├── pom.xml
└── README.md
```

## Observação

`Diagnostico.idVeic` é mantido como `Long`, pois o modelo atual ainda não possui uma entidade `Veiculo`.
