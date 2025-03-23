# Testes de Integração com Testcontainers

Este documento explica como realizar testes de integração usando Testcontainers no projeto RGB-ERP-Provider.

## O que é Testcontainers?

Testcontainers é uma biblioteca Java que permite criar e gerenciar contêineres Docker durante a execução de testes. Isso facilita a criação de ambientes isolados para testes de integração, garantindo maior confiabilidade e reduzindo a necessidade de mocks.

## Configuração no Projeto

O projeto já possui as dependências necessárias no arquivo `build.gradle`:

```gradle
dependencies {
    // ... outras dependências
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:kafka'
}
```

## Exemplos de Uso

### 1. Testes de Repositório com PostgreSQL

Para testar componentes que dependem do banco de dados, use o PostgreSQLContainer:

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
            
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    // Testes...
}
```

### 2. Testes de Mensageria com Kafka

Para testar a integração com Kafka:

```java
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaIntegrationTest {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

    @BeforeAll
    void setUp() {
        kafkaContainer.start();
    }

    @AfterAll
    void tearDown() {
        kafkaContainer.stop();
    }

    // Testes...
}
```

## Boas Práticas

1. **Reuse de contêineres**: Use `.withReuse(true)` para reutilizar contêineres entre testes quando apropriado.
2. **Isolamento**: Para testes que modificam dados, limpe o estado antes e depois do teste.
3. **Performance**: Agrupe testes relacionados para minimizar a criação de contêineres.
4. **Configuração dinâmica**: Use `@DynamicPropertySource` para configurar a aplicação com as informações do contêiner.

## Guia Passo a Passo

### Para criar um novo teste com Testcontainers:

1. Anote a classe de teste com `@Testcontainers`
2. Declare o contêiner com a anotação `@Container`
3. Configure os parâmetros específicos do contêiner
4. Use `@DynamicPropertySource` para integrar com o Spring
5. Escreva os testes usando os recursos proporcionados pelo contêiner

## Exemplos no Projeto

- `UserRepositoryTest`: Teste de repositório JPA com PostgreSQL
- `KafkaIntegrationTest`: Teste de integração com Kafka

## Depuração

Se ocorrerem problemas com os contêineres:

1. Verifique se o Docker está em execução
2. Examine os logs do Docker: `docker logs [container-id]`
3. Verifique a disponibilidade das portas usadas pelos contêineres
4. Limpe contêineres antigos: `docker container prune` 