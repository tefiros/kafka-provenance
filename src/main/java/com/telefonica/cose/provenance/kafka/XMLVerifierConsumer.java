package com.telefonica.cose.provenance.kafka;

import COSE.CoseException;
import com.telefonica.cose.provenance.XMLVerification;
import com.telefonica.cose.provenance.XMLVerificationInterface;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.security.Security;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

import static com.telefonica.cose.provenance.kafka.MessageCorruption.*;


public class XMLVerifierConsumer {

    private static final Logger log = LoggerFactory.getLogger(XMLVerifierConsumer.class.getSimpleName());

    static {
        // Inicializar librerías necesarias
        Security.addProvider(new BouncyCastleProvider());
        org.apache.xml.security.Init.init();
    }

    public static void main(String[] args) {
        log.info("Starting VerifierConsumer...");

        // Kafka topic settings
        String inputTopic = "xml_signed_messages";
        String validTopic = "xml_valid_messages";
        String invalidTopic = "xml_invalid_messages";

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

        SAXBuilder saxBuilder = new SAXBuilder();
        XMLVerificationInterface verifier = new XMLVerification();

        Random random = new Random();

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    log.info("Received signed message:\n{}", message);

                    try {
                        Document doc = saxBuilder.build(new StringReader(message));


                        // Añadido randomizador
                        boolean applyTampering = random.nextBoolean(); // 50% de probabilidad
                        Document processedDoc = applyTampering ? tamperAtSecondLevelXML(doc) : doc;

                        // Mensaje debug
                        if (applyTampering) {
                            log.info("Tampering applied to message");
                        } else {
                            log.info("No tampering applied — verifying original message");
                        }

                        String processedString = toPrettyString(processedDoc);
                        boolean valid = verifier.verify(processedDoc);
                        String destinationTopic = valid ? validTopic : invalidTopic;


                        // Reenviar mensaje al topic correspondiente
                        ProducerRecord<String, String> newRecord =
                                new ProducerRecord<>(destinationTopic, record.key(), processedString);

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
