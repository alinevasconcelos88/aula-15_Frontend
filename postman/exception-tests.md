# Testes de exceções no Postman

A coleção `CarManual.postman_collection.json` inclui a pasta **07 - Testes de exceções (sem persistência)**. Importe a coleção no Postman, configure `baseUrl` (por padrão `http://localhost:8080`) e ajuste `planoId` para um plano que já exista.

As rotas testadas nessa pasta agora exigem JWT. Para obtê-lo, execute primeiro os passos da pasta **00 - Autenticação JWT** descritos em [authentication-tests.md](./authentication-tests.md). A variável `token` da coleção será enviada automaticamente nas requisições protegidas.

## Preparar a API sem alterar o banco

Configure as credenciais MySQL localmente por `MYSQL_USERNAME` e `MYSQL_PASSWORD`. Não salve credenciais na coleção nem no repositório. Para rodar apenas os casos da pasta 07 sem atualizações no banco, inicie a aplicação com a carga SQL e a atualização de schema desativadas:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.sql.init.mode=never,--spring.jpa.hibernate.ddl-auto=none"
```

Essas opções impedem a execução de `data.sql` e operações automáticas no schema. Os testes desta pasta são GETs ou requisições POST inválidas rejeitadas antes de persistir dados.

O cadastro e o login de teste da pasta 00 precisam ser executados com o schema atualizado, pois o login usa a nova coluna de senha em `app_user`. Cada cadastro cria um usuário persistente.

## Casos cobertos

| Requisição | Resultado esperado | O que verifica |
| --- | --- | --- |
| `GET /plano/{{planoId}}` | `200 OK` | Retorna um plano existente. Use um ID já presente no banco. |
| `GET /plano/9999` | `404 Not Found` | JSON com `status`, `message` contendo entidade e ID, e `timestamp`. Confirme que o ID 9999 não existe no seu banco. |
| `POST /diagnostico` com campos obrigatórios vazios | `400 Bad Request` | Mensagens de validação para `descricao`, `resultado` e `idVeic`. O controller valida o corpo antes de chamar o service. |
| `POST /diagnostico` com JSON malformado | `500 Internal Server Error` | Exercita o handler genérico sem chegar ao service ou gravar dados. |

Cada requisição contém um script na aba **Scripts > Post-response**. Use **Send** para executar uma chamada individual ou rode a pasta no Collection Runner. O Collection Runner para no primeiro caso que falhar.

## Formato de erro esperado

```json
{
  "status": 404,
  "message": "Plano não encontrado com ID: 9999",
  "timestamp": "2026-05-03T14:20:45.321"
}
```

A mensagem e o horário variam por erro e instante da chamada. Os scripts validam o código HTTP, os campos e o conteúdo relevante, sem comparar o timestamp literalmente.
