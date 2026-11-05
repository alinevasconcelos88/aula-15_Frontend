# Testar autenticação JWT no Postman

A coleção `CarManual.postman_collection.json` já contém as chamadas de cadastro, login e teste das rotas protegidas.

## Preparar a API

1. Inicie o MySQL e confirme que o banco configurado pela aplicação está disponível.
2. Inicie a API normalmente para que o Hibernate atualize a tabela `app_user` com a coluna de senha.
3. Confirme que a API está respondendo em `http://localhost:8080`.

O cadastro de teste grava um usuário no banco. As senhas são armazenadas com BCrypt. Usuários antigos sem senha cadastrada não podem fazer login; cadastre um usuário de teste pela coleção.

## Importar e executar

1. No Postman, selecione **Import** e importe `CarManual.postman_collection.json`.
2. Selecione a coleção **CarManual API - MySQL** e confira a variável `baseUrl`. O padrão é `http://localhost:8080`.
3. Abra **00 - Autenticação JWT** e execute, nesta ordem:
   - **Cadastrar usuário de teste**: cria um usuário com nome e senha aleatórios.
   - **Fazer login e salvar JWT**: autentica o usuário e salva o token na variável da coleção `token`.
   - **Acessar rota protegida com JWT**: envia o JWT automaticamente como `Authorization: Bearer <token>`.
   - **403 - Rota protegida sem token**: remove a autenticação da chamada e confirma a rejeição.
   - **403 - Rota protegida com token inválido**: envia um token inválido e confirma a rejeição.
   - **401 - Login com senha inválida**: confirma que credenciais incorretas não autenticam.
4. Depois do login, execute outras requisições protegidas da coleção. A autenticação Bearer está configurada na coleção.

Também é possível executar a coleção inteira pelo **Collection Runner**: a pasta de autenticação vem primeiro, então o token é obtido antes das demais chamadas. Cada execução cadastra um novo usuário de teste no banco.

## Respostas esperadas

| Caso | Resultado |
| --- | --- |
| `POST /auth/register` com dados válidos | `201 Created`, com o ID e o nome do usuário; a senha não é retornada |
| `POST /login` com credenciais válidas | `200 OK`, com `token` e `tokenType: "Bearer"` |
| Requisição protegida com JWT válido | A requisição segue para o controller |
| Login com credenciais inválidas | `401 Unauthorized` |
| Rota protegida sem JWT ou com token inválido | `403 Forbidden` |

O token expira após uma hora. A coleção guarda o token como variável local da coleção; faça login novamente quando ele expirar.

## Observação sobre autorização

Atualmente, a API exige usuário autenticado para as rotas protegidas, mas ainda não diferencia permissões por perfil. Os usuários cadastrados recebem a autoridade `ROLE_USER`.
