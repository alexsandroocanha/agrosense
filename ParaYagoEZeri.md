# AgroGestão Intelligence

Um sistema de controle de insumos rurais feito com **React + TypeScript no frontend**, **Java + Spring Boot no backend** e **PostgreSQL no banco**. A ideia é controlar insumos, registrar entradas e saídas, consultar o histórico e gerar alertas básicos de estoque.

Este README é para a galera que quer rodar a aplicação sem precisar invocar os espíritos do Stack Overflow. Vamos por partes e sem fingir que esse código nasceu perfeito: é uma base funcional para desenvolvimento, ainda não um sistema pronto para colocar na fazenda inteira sem revisar e testar.

## 1. O que você precisa instalar

### Caminho mais fácil: Docker

Instale o **Docker Desktop** e deixe ele aberto. O Docker Compose sobe banco, API e frontend em containers, então você não precisa instalar Java, Maven, Node e PostgreSQL separadamente na máquina para executar o projeto.

- Docker Desktop com Docker Compose.
- Git, se quiser clonar o repositório.
- Um terminal: PowerShell no Windows, Terminal no Linux/macOS.

### Se quiser rodar cada parte manualmente

- **Java JDK 21** — a API foi configurada para Java 21.
- **Maven 3.9+** — para compilar e executar a API Java.
- **Node.js LTS e npm** — para instalar dependências e executar o React/Vite.
- **PostgreSQL 16** — banco de dados.

O `pom.xml` baixa as bibliotecas Java pelo Maven. O `package.json` lista as bibliotecas do frontend que o npm instala. Não precisa sair baixando JAR ou pacote na mão, pelo amor da lavoura.

## 2. Rodando tudo com Docker (recomendado)

Abra o terminal na pasta raiz do projeto, aquela onde ficam `docker-compose.yml` e `.env.example`.

### Windows PowerShell

```powershell
Copy-Item .env.example .env
```

Abra o `.env` e troque os valores de exemplo. Depois:

```powershell
docker compose up --build
```

No Linux/macOS, a cópia do arquivo é:

```bash
cp .env.example .env
docker compose up --build
```

Na primeira execução, o Docker precisa baixar imagens e compilar as aplicações, então pode demorar um pouco. Nas próximas, tende a ser mais rápido.

Quando tudo estiver de pé:

- **Frontend:** http://localhost:5173
- **API Java:** http://localhost:8080
- **Documentação Swagger da API:** http://localhost:8080/swagger-ui.html
- **PostgreSQL:** `localhost:5432` (banco `agrogestao`)

Para parar os containers, aperte `Ctrl+C` no terminal e rode:

```bash
docker compose down
```

Para subir novamente reconstruindo as imagens depois de mudar código:

```bash
docker compose up --build
```

Para acompanhar os logs:

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

Para apagar também o banco persistido e começar do zero, use `docker compose down -v`. **Atenção:** isso apaga os dados do volume do PostgreSQL. Não rode essa porra se tiver dados que queira guardar.

### Se o build reclamar de `ChronoUnit`

Se aparecer algo como `package ChronoUnit does not exist`, confira se `StockService.java` tem este import no começo do arquivo:

```java
import java.time.temporal.ChronoUnit;
```

Salve o arquivo e tente de novo com `docker compose up --build`. Não é um problema do Compose: é o compilador Java reclamando que o código usou uma classe sem importá-la.

## 3. Configuração do `.env`

O arquivo `.env.example` é um modelo. Copie para `.env` e altere pelo menos estes valores:

- `SPRING_DATASOURCE_PASSWORD`: senha usada pelo PostgreSQL no ambiente local.
- `APP_JWT_SECRET`: segredo usado para assinar os tokens de autenticação. Use uma sequência aleatória longa, com pelo menos 32 bytes.

Provavelmente eu suba o que eu testei no meu ambiente ao desenvolver esse projeto

## 4. Como a aplicação está organizada

```text
agrogestao-intelligence/
├── docker-compose.yml          # Sobe banco, backend e frontend
├── .env.example                # Modelo das variáveis de ambiente
├── backend/
│   ├── Dockerfile              # Compila e empacota a API Java
│   ├── pom.xml                 # Dependências e configuração Maven
│   └── src/
│       ├── main/java/br/com/agrogestao/
│       ├── main/resources/
│       └── test/java/
└── frontend/
    ├── Dockerfile              # Monta e serve o frontend
    ├── package.json            # Dependências e comandos npm
    ├── vite.config.ts          # Configuração do Vite
    └── src/
        ├── main.tsx            # Ponto de entrada React
        ├── App.tsx             # Tela principal e interações
        └── styles.css          # Estilos da interface
```

## 5. Backend Java: quem faz o quê

O backend fica em `backend/`. Ele recebe requisições HTTP, valida dados, aplica as regras de estoque e conversa com o PostgreSQL.

### Arquivo de inicialização

- **`AgrogestaoApplication.java`** — é o botão de ligar da aplicação Spring Boot. O `main` chama `SpringApplication.run(...)` e o Spring monta os componentes.
- **`pom.xml`** — lista Spring Web (endpoints HTTP), Spring Data JPA (persistência), Spring Security (segurança), Validation (validação), PostgreSQL, Flyway (migrações do banco), JWT, Swagger/OpenAPI e dependências de teste. É a lista de compras Java; o Maven baixa essa tralha por você.
- **`application.yml`** — configura porta `8080`, conexão com o banco, Flyway, JPA e segredo JWT. A configuração usa variáveis de ambiente para não precisar deixar senhas fixas no código.
- **`V1__initial_schema.sql`** — primeira migração do Flyway: cria a estrutura inicial das tabelas. O Flyway controla a evolução do esquema do banco, em vez de depender de alguém executar SQL manual no escuro.

### `domain/` — as coisas que existem no sistema

- **`User.java`** — representa a conta de usuário: e-mail, hash da senha, nome da fazenda e papel. A senha não deve ser salva em texto puro; o sistema armazena o hash produzido pelo BCrypt.
- **`Supply.java`** — representa um insumo, com nome, categoria, unidade, saldo, estoque mínimo, fornecedor, localização e custo unitário. Também tem arquivamento lógico e versão para ajudar a evitar atualizações concorrentes perdidas.
- **`Movement.java`** — registra uma movimentação: entrada, saída ou ajuste, quantidade, data, insumo e informações complementares. É o histórico do que aconteceu com o estoque.
- **`StockAlert.java`** — representa um alerta de estoque, com tipo, prioridade, explicação, recomendação e data da análise.

Essas classes são entidades JPA: o Spring/Hibernate mapeia seus campos para tabelas no banco. Resumindo: são os objetos que a aplicação salva e consulta.

### `repo/` — quem conversa com o banco

- **`UserRepository.java`** — procura usuários pelo e-mail, sem diferenciar maiúsculas e minúsculas.
- **`SupplyRepository.java`** — busca insumos ativos por fazenda e tem uma consulta com bloqueio pessimista para proteger movimentações concorrentes do mesmo item.
- **`MovementRepository.java`** — busca o histórico geral e o histórico de um insumo, além de oferecer consultas de contagem.
- **`AlertRepository.java`** — lista e remove alertas de uma fazenda.

Essas interfaces estendem `JpaRepository`. O Spring Data implementa boa parte das operações automaticamente a partir dos nomes dos métodos. Sim, parece bruxaria, mas é convenção do framework.

### `service/` — onde ficam as regras

- **`AuthService.java`** — registra usuário, normaliza e-mail, impede cadastro duplicado, verifica a senha e entrega um token JWT após cadastro/login.
- **`StockService.java`** — concentra a lógica de estoque. Cria, lista, atualiza e arquiva insumos; valida quantidades; registra entradas, saídas e ajustes; impede saída maior que o saldo; salva o histórico; recalcula alertas e monta os dados do painel.

A análise de cobertura de estoque é **simples e heurística**, não uma inteligência artificial treinada nem uma previsão agrícola sofisticada. Ela usa o histórico de saídas para estimar consumo médio diário e sinalizar cobertura baixa quando há movimentações suficientes. Não leva em conta sazonalidade, clima, calendário de plantio ou todos os fatores que um modelo real precisaria considerar.

Detalhe importante: neste código, `AJUSTE` soma a quantidade informada ao saldo; ele não é um ajuste absoluto para definir o saldo final. Confirme essa regra antes de usar em operação real.

### `web/` — endpoints HTTP

- **`AuthController.java`** — expõe cadastro e login em `/api/auth/register` e `/api/auth/login`.
- **`StockController.java`** — expõe endpoints de insumos, movimentações, painel, alertas e execução da análise. É a porta de entrada HTTP para a lógica do `StockService`.
- **`ApiExceptionHandler.java`** — converte exceções conhecidas em respostas HTTP com uma mensagem JSON. Evita devolver detalhes internos desnecessários para o cliente; o tratamento genérico também significa que, se algo der errado, talvez seja preciso olhar os logs para descobrir a causa real.

### `auth/` e `config/` — segurança e configuração

- **`JwtService.java`** — cria tokens JWT com identificador do usuário e e-mail, assina o token e valida sua assinatura/expiração ao ler o identificador. O token tem validade de 24 horas.
- **`JwtFilter.java`** — procura o cabeçalho `Authorization: Bearer ...` em cada requisição protegida. Se o token for válido, coloca o usuário autenticado no contexto do Spring Security.
- **`SecurityConfig.java`** — configura endpoints públicos, exige autenticação para o restante, desativa sessão de servidor (API stateless), registra o filtro JWT, configura BCrypt e libera CORS para o frontend local em `localhost:5173`.

### Testes Java

- **`CoverageCalculationTest.java`** — contém um teste unitário relacionado ao cálculo de cobertura. Execute os testes com `mvn test` dentro da pasta `backend`, ou pelo container de build conforme a configuração do projeto.

## 6. Frontend React: a parte que você vê

O frontend fica em `frontend/` e foi feito com React, TypeScript e Vite.

- **`package.json`** — define as dependências e scripts. React renderiza a interface; TypeScript adiciona checagem de tipos; Vite roda o servidor de desenvolvimento e empacota a versão final; Recharts desenha gráficos; Lucide React fornece ícones. O projeto também inclui Vitest e Testing Library como ferramentas para testes de interface.
- **`index.html`** — página HTML base que recebe a aplicação React.
- **`src/main.tsx`** — ponto de entrada: encontra o elemento raiz do HTML e renderiza o componente principal.
- **`src/App.tsx`** — concentra a tela principal, navegação/abas e interações com o usuário, como exibir dados do painel e lidar com insumos e movimentações. É o arquivo para procurar primeiro quando quiser entender o comportamento da tela.
- **`src/styles.css`** — define cores, espaçamento, layout, tabelas, formulários e comportamento visual responsivo.
- **`src/vite-env.d.ts`** — declara tipos do ambiente Vite para o TypeScript.
- **`vite.config.ts`** — configura o Vite e o plugin React.
- **`tsconfig.json`** — configura como o TypeScript verifica e compila o código.
- **`Dockerfile`** — define os passos usados pelo Docker para construir e servir o frontend.

## 7. Rodando sem Docker (modo desenvolvimento)

### Banco

Tenha PostgreSQL 16 rodando e crie um banco chamado `agrogestao`, com usuário/senha compatíveis com a configuração. Se usar outros valores, exporte `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` no ambiente do backend.

### API Java

No terminal:

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. O Flyway executa as migrações na inicialização.

Para rodar os testes:

```bash
mvn test
```

Para gerar o pacote:

```bash
mvn package
```

### Frontend

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Abra `http://localhost:5173`. Para gerar a versão de produção:

```bash
npm run build
```

A URL da API é configurada no container pelo `VITE_API_URL`. Se estiver rodando o Vite localmente, confira no código/configuração do frontend qual endereço está sendo usado e ajuste para `http://localhost:8080/api` se necessário.

## 8. Endpoints principais

Os endpoints de negócio abaixo exigem o cabeçalho `Authorization: Bearer <token>`, obtido no cadastro ou login.

| Método | Caminho | Para que serve |
|---|---|---|
| `POST` | `/api/auth/register` | Cadastrar usuário/fazenda |
| `POST` | `/api/auth/login` | Entrar e receber token |
| `GET` | `/api/supplies` | Listar insumos ativos |
| `POST` | `/api/supplies` | Criar insumo |
| `PUT` | `/api/supplies/{id}` | Atualizar dados do insumo |
| `DELETE` | `/api/supplies/{id}` | Arquivar insumo (não é exclusão física) |
| `GET` | `/api/movements` | Consultar as 100 movimentações mais recentes |
| `GET` | `/api/supplies/{id}/movements` | Consultar histórico do insumo |
| `POST` | `/api/movements` | Registrar entrada, saída ou ajuste |
| `GET` | `/api/dashboard` | Buscar dados agregados do painel |
| `GET` | `/api/alerts` | Listar alertas atuais |
| `POST` | `/api/analysis/run` | Recalcular os alertas |

Use o Swagger em `/swagger-ui.html` para explorar os endpoints e testar requisições.

## 9. O que ainda não está pronto

É melhor deixar isso claro do que vender fumaça:

- Não há controle completo de lotes, validade e FEFO (usar primeiro o lote que vence primeiro).
- Importação de planilhas CSV/XLSX não está implementada no backend.
- A previsão de consumo é básica; não há backtesting, sazonalidade nem modelo estatístico avançado.
- A classificação ABC completa não está implementada.
- O ciclo de vida dos alertas ainda é simples: a análise atual apaga e recria os alertas da fazenda, então não trate os alertas como histórico imutável.
- O frontend contém partes demonstrativas e precisa ser conferido contra a API real; nem todo gráfico/dado visual deve ser entendido como dado calculado pelo backend.
- A suíte de testes ainda é pequena. É necessário adicionar testes de integração, autorização e fluxos completos.

Antes de colocar isso em produção, revise autenticação/autorização, isolamento por fazenda, validações, logs, tratamento de erros, backups, migrações, CORS, segredos, testes e regras de ajuste de estoque. Código que compila não significa automaticamente código pronto para cuidar do estoque real da fazenda — infelizmente o compilador ainda não lê pensamento.

## 10. Problemas comuns

**`mvn` não é reconhecido:** instale Maven ou use Docker, que executa a compilação dentro da imagem.

**`npm` não é reconhecido:** instale Node.js LTS e abra um terminal novo.

**Porta 5432, 8080 ou 5173 já está em uso:** pare o processo que ocupa a porta ou altere o mapeamento em `docker-compose.yml`.

**Backend não conecta ao banco:** confira se o serviço `db` está saudável, se a senha é a mesma e se a URL do banco usa `db` dentro do Compose. Dentro do container, `localhost` aponta para o próprio container, não para o container do PostgreSQL — essa pegadinha já fez muita gente perder tempo à toa.

**Mudou Java e o Docker parece ignorar:** rode `docker compose up --build` para reconstruir a imagem.

**Quer zerar tudo:** `docker compose down -v`, mas lembre-se de que o `-v` apaga o volume do banco.

---