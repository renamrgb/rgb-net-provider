# RGB Net Provider ERP

Sistema ERP completo para provedores de internet, com foco em escalabilidade, segurança e boas práticas de desenvolvimento.

## Arquitetura

Este projeto utiliza uma arquitetura modular baseada em Event Sourcing e CQRS, permitindo alta escalabilidade e separação clara entre operações de leitura e escrita.

### Componentes Principais

- **Core**: Domínio central com entidades e lógica de negócios
- **API**: Endpoints REST para acesso às funcionalidades
- **Persistence**: Camada de persistência com suporte a PostgreSQL
- **Security**: Implementação JWT para autenticação e autorização
- **Mensageria**: Integração com Kafka para comunicação assíncrona

## Funcionalidades

- **Controle de Estoque**: Gerenciamento completo de produtos e inventário
- **Controle Financeiro**: Boletos, carnês, pagamentos e relatórios financeiros
- **Área do Cliente**: Portal para clientes acessarem informações e serviços
- **Controle de Planos**: Cadastro e gerenciamento de planos de internet
- **Comunicação com Equipamentos**: Integração com equipamentos de rede
- **Segurança**: Criptografia de dados sensíveis e conformidade com LGPD

## Tecnologias

- **Backend**: Java 21 com Spring Boot 3+
- **Persistência**: PostgreSQL com Flyway para migrações
- **Mensageria**: Apache Kafka
- **Segurança**: Spring Security com JWT
- **Containerização**: Docker e Docker Compose

## Pré-requisitos

- Java 21
- Docker e Docker Compose
- PostgreSQL 15+
- Apache Kafka

## Como Executar

### Ambiente de Desenvolvimento

1. Clone o repositório
2. Configure o banco de dados PostgreSQL
3. Configure o Kafka
4. Execute a aplicação com perfil "dev":

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Com Docker

```bash
docker-compose up -d
```

## Monitoramento e Observabilidade

A aplicação expõe endpoints do Spring Actuator para monitoramento:

- Health check: `/api/actuator/health`
- Métricas: `/api/actuator/metrics`
- Info: `/api/actuator/info`

## Arquitetura de Event Sourcing

O sistema utiliza Event Sourcing como padrão arquitetural central, onde:

1. Todas as mudanças no estado são registradas como eventos
2. O estado atual é derivado da sequência de eventos
3. Eventos são imutáveis e mantêm o histórico completo

### Benefícios do Event Sourcing

- **Auditoria completa**: Histórico de todas as operações
- **Recuperação de estados**: Possibilidade de reconstruir estados históricos
- **Escalabilidade**: Separação entre modelos de leitura e escrita

## Estrutura do Projeto

```
├── src
│   ├── main
│   │   ├── java/com/rgbnet/provider
│   │   │   ├── config        # Configurações do Spring
│   │   │   ├── controller    # Endpoints da API
│   │   │   ├── domain        # Entidades e modelos
│   │   │   ├── dto           # Objetos de transferência de dados
│   │   │   ├── event         # Eventos do sistema
│   │   │   ├── exception     # Tratamento de exceções
│   │   │   ├── repository    # Interfaces de persistência
│   │   │   ├── security      # Configurações de segurança
│   │   │   ├── service       # Serviços de negócios
│   │   │   └── util          # Classes utilitárias
│   │   └── resources
│   │       ├── db/migration  # Scripts Flyway
│   │       └── application.yml # Configuração principal
│   └── test                  # Testes unitários e de integração
```

## Contribuição

1. Utilize o Git Flow para desenvolvimento
2. Crie feature branches para novas funcionalidades
3. Envie pull requests para a branch develop 