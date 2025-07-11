package com.telefonica.cose.provenance.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.telefonica.cose.provenance.JSONVerification;
import com.telefonica.cose.provenance.JSONVerificationInterface;
import COSE.CoseException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;



public class VerifierConsumer {

    private static final Logger log = LoggerFactory.getLogger(VerifierConsumer.class.getSimpleName());

    static {
        // Inicializar librerías necesarias
        Security.addProvider(new BouncyCastleProvider());
        org.apache.xml.security.Init.init();
    }

    public static void main(String[] args) {
        log.info("Starting VerifierConsumer...");

        // Kafka topic settings
        String inputTopic = "json_signed_topic";
        String validTopic = "valid_messages";
        String invalidTopic = "invalid_messages";

        // Kafka consumer config
        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", "localhost:9092");
        consumerProps.setProperty("key.deserializer", StringDeserializer.class.getName());
        consumerProps.setProperty("value.deserializer", StringDeserializer.class.getName());
        consumerProps.setProperty("group.id", "verifier-group");
        consumerProps.setProperty("auto.offset.reset", "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // Kafka producer config
        Properties producerProps = new Properties();
        producerProps.setProperty("bootstrap.servers", "localhost:9092");
        producerProps.setProperty("key.serializer", StringSerializer.class.getName());
        producerProps.setProperty("value.serializer", StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        ObjectMapper mapper = new ObjectMapper();
        JSONVerificationInterface verifier = new JSONVerification();

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    log.info("Received signed message:\n{}", message);

                    try {
                        JsonNode doc = mapper.readTree(message);
                        boolean valid = verifier.verify(doc);

                        String destinationTopic = valid ? validTopic : invalidTopic;

                        // Reenviar mensaje al topic correspondiente
                        ProducerRecord<String, String> newRecord =
                                new ProducerRecord<>(destinationTopic, record.key(), message);

                        producer.send(newRecord, (metadata, exception) -> {
                            if (exception != null) {
                                log.error("Failed to produce to topic {}", destinationTopic, exception);
                            } else {
                                log.info("Message sent to {} [partition={}, offset={}]", destinationTopic, metadata.partition(), metadata.offset());
                            }
                        });

                    } catch (CoseException e) {
                        log.error("COSE verification failed", e);
                    } catch (Exception e) {
                        log.error("Error parsing/verifying message", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error in verifier loop", e);
        } finally {
            consumer.close();
            producer.close();
            log.info("VerifierConsumer shut down.");
        }
    }
}
