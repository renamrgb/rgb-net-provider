#!/bin/bash

# Inicia o ambiente Docker (PostgreSQL e Kafka)
echo "Iniciando serviços Docker..."
docker compose up -d

# Função para verificar se o PostgreSQL está pronto
check_postgres() {
  docker exec rgb-provider-postgres pg_isready -U postgres > /dev/null 2>&1
  return $?
}

# Aguarda o PostgreSQL iniciar (com timeout)
echo "Aguardando PostgreSQL ficar disponível..."
MAX_RETRY=30
count=0
until check_postgres || [ $count -eq $MAX_RETRY ]; do
  echo "Tentando conexão com PostgreSQL ($((count+1))/$MAX_RETRY)..."
  sleep 3
  count=$((count+1))
done

if [ $count -eq $MAX_RETRY ]; then
  echo "Erro: Falha ao conectar ao PostgreSQL após $MAX_RETRY tentativas"
  exit 1
fi

echo "PostgreSQL está pronto!"

# Verifica se a porta do PostgreSQL está realmente acessível do host
echo "Verificando se a porta do PostgreSQL está acessível do host..."
POSTGRES_HOST="localhost"
if ! nc -z localhost 5432; then
  echo "AVISO: A porta 5432 não está acessível do host. Verificando configuração do container..."
  docker inspect rgb-provider-postgres | grep -A 10 PortBindings
  
  # Tenta descobrir o IP do container
  POSTGRES_IP=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' rgb-provider-postgres)
  echo "IP interno do PostgreSQL: $POSTGRES_IP"
  
  echo "Ajustando a URL de conexão para usar o IP interno do container: $POSTGRES_IP"
  POSTGRES_HOST=$POSTGRES_IP
fi

# Verifica se o banco de dados existe
echo "Verificando se o banco de dados 'rgb_provider' existe..."
if ! docker exec rgb-provider-postgres psql -U postgres -lqt | cut -d \| -f 1 | grep -qw rgb_provider; then
  echo "Criando banco de dados 'rgb_provider'..."
  docker exec rgb-provider-postgres psql -U postgres -c "CREATE DATABASE rgb_provider;"
fi

# Aguarda o Kafka iniciar
echo "Aguardando Kafka ficar disponível..."
MAX_RETRY=15
count=0
until docker exec rgb-provider-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1 || [ $count -eq $MAX_RETRY ]; do
  echo "Tentando conexão com Kafka ($((count+1))/$MAX_RETRY)..."
  sleep 4
  count=$((count+1))
done

if [ $count -eq $MAX_RETRY ]; then
  echo "Aviso: Kafka pode não estar pronto, mas continuando..."
fi

# Verificar conexão com o PostgreSQL usando Java
echo "Verificando conexão com PostgreSQL via Java..."
./gradlew checkPostgresConnection --info || {
  echo "AVISO: Não foi possível conectar ao PostgreSQL via Java. A migração Flyway pode falhar."
}

# Executa o Flyway migrate antes de iniciar a aplicação
echo "Executando migrações Flyway..."
./gradlew flywayMigrate -Pflyway.url=jdbc:postgresql://${POSTGRES_HOST}:5432/rgb_provider -Pflyway.user=postgres -Pflyway.password=postgres || {
  echo "AVISO: A migração Flyway via Gradle falhou, mas a aplicação ainda tentará executar o Flyway na inicialização."
}

# Inicia a aplicação Spring Boot com as configurações adequadas
echo "Iniciando a aplicação com Undertow..."
./gradlew bootRun --args="--spring.profiles.active=dev --spring.datasource.url=jdbc:postgresql://${POSTGRES_HOST}:5432/rgb_provider" -Dorg.gradle.jvmargs="-Xms1G -Xmx2G -XX:ParallelGCThreads=4" 