package com.rgbnet.provider.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaIntegrationTest {

    private static final String TOPIC_NAME = "test-topic";

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

    @Test
    public void testKafkaProducerAndConsumer() throws Exception {
        // Mensagem única para este teste
        String testId = UUID.randomUUID().toString();
        String message = "Mensagem de teste: " + testId;

        // Configura o producer
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            // Envia a mensagem
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, testId, message);
            producer.send(producerRecord).get(10, TimeUnit.SECONDS);
        }

        // Configura o consumer
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            // Inscreve-se no tópico
            consumer.subscribe(Collections.singletonList(TOPIC_NAME));

            // Lê a mensagem
            ConsumerRecord<String, String> consumerRecord = null;
            int maxRetries = 10;
            int retries = 0;

            while (consumerRecord == null && retries < maxRetries) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofSeconds(1))) {
                    if (record.key().equals(testId)) {
                        consumerRecord = record;
                        break;
                    }
                }
                retries++;
            }

            // Verifica a mensagem
            assertNotNull(consumerRecord, "Mensagem não recebida do Kafka");
            assertEquals(testId, consumerRecord.key(), "Chave da mensagem incorreta");
            assertEquals(message, consumerRecord.value(), "Conteúdo da mensagem incorreto");
        }
    }
} 