package com.telefonica.cose.provenance.kafka;

import COSE.CoseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.telefonica.cose.provenance.JSONVerification;
import com.telefonica.cose.provenance.JSONVerificationInterface;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

import static com.telefonica.cose.provenance.kafka.MessageCorruption.tamperAtSecondLevel;


public class JSONVerifierConsumer {

    private static final Logger log = LoggerFactory.getLogger(JSONVerifierConsumer.class.getSimpleName());

    static {
        // Inicializar librerías necesarias
        Security.addProvider(new BouncyCastleProvider());
        org.apache.xml.security.Init.init();
    }

    public static void main(String[] args) {
        log.info("Starting VerifierConsumer...");

        // Kafka topic settings
        String inputTopic = "json_signedCS1_messages";
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

//        Random random = new Random();

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    log.info("Received signed message:\n{}", message);

                    try {
                        JsonNode doc = mapper.readTree(message);
                        JsonNode docCS = mapper.readTree(message);

                        // Añadido randomizador
//                        boolean applyTampering = random.nextBoolean(); // 50% de probabilidad
//                        JsonNode processedNode = applyTampering ? tamperAtSecondLevel(doc) : doc;
                        JsonNode processedNode = doc;

                        // Mensaje debug
//                        if (applyTampering) {
//                            log.info("Tampering applied to message");
//                        } else {
//                            log.info("No tampering applied — verifying original message");
//                        }

//                        String processedString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(processedNode);
                        boolean valid = verifier.verifyJSONWithCountersigns(processedNode, "ietf-provenance-augmented", "interfaces-provenance");
//
//                        if (valid) {
//                            log.info("ORIGINAL SIGNATURE VALID");
//                        } else {
//                            log.warn("ORIGINAL SIGNATURE INVALID");
//                        }



                        //boolean validCS = verifier.verifyJSONCounterSignByKid(processedNode, "ietf-provenance-augmented", "interfaces-provenance", "ec3.key");


                        if (valid) {
                            log.info("CS SIGNATURE VALID");
                        } else {
                            log.warn("CS SIGNATURE INVALID");
                        }



//                        log.info("Message content:\n{}",
//                                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(processedNode));


//                        String destinationTopic = valid ? validTopic : invalidTopic;
//
//
//                        // Reenviar mensaje al topic correspondiente
//                        ProducerRecord<String, String> newRecord =
//                                new ProducerRecord<>(destinationTopic, record.key(), processedString);
//
//                        producer.send(newRecord, (metadata, exception) -> {
//                            if (exception != null) {
//                                log.error("Failed to produce to topic {}", destinationTopic, exception);
//                            } else {
//                                log.info("Message sent to {} [partition={}, offset={}]", destinationTopic, metadata.partition(), metadata.offset());
//                            }
//                        });

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

    private static JsonNode prepareForCountersignVerification(JsonNode input, String fullLeafName) {
        if (input == null || !input.isObject()) {
            throw new IllegalArgumentException("El JSON root debe ser un objeto");
        }

        ObjectNode root = (ObjectNode) input.deepCopy();

        // detectar container dinámicamente
        String containerName = root.fieldNames().next();
        JsonNode container = root.get(containerName);

        if (container != null && container.isObject() && container.has(fullLeafName)) {
            root.put(fullLeafName, container.get(fullLeafName).asText());
        } else {
            throw new IllegalArgumentException(
                    "No se encontró la firma en el container para el leaf: " + fullLeafName
            );
        }

        return root;
    }
}
