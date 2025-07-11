package com.telefonica.cose.provenance.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


public class Consumer {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class.getSimpleName());

    public static void main(String[] args) {
        log.info("Welcome to the Kafka Consumer");

        String groupId = "kafka-consumer-test";
        // String topic = "xml_topic";
        String topic = "json_topic";

        // Create Consumer Properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("group.id", groupId);
        properties.setProperty("auto.offset.reset", "earliest"); // Read entire history of the topic

        // Create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Create Producer Properties
        Properties producerProps = new Properties();
        producerProps.setProperty("bootstrap.servers", "localhost:9092");
        producerProps.setProperty("key.serializer", StringSerializer.class.getName());
        producerProps.setProperty("value.serializer", StringSerializer.class.getName());


        // Create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Subscribed to topic: {}", topic);

        //XMLSigner signer = new XMLSigner();
        JSONSigner signer = new JSONSigner();

        try {
            while (true) {
                log.info("Polling for records...");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000)); // Increased polling duration

                if (records.isEmpty()) {
                    log.info("No records found.");
                } else {
                    log.info("Found {} records.", records.count());
                }

//                records.forEach(record -> {
//                    log.info("Received record: key = {}, value = {}", record.key(), record.value());
//                    try {
//                        signer.process(record.value());
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Received record: key = {}, value = {}", record.key(), record.value());
                    try {
                        String signedMessage = signer.process(record.value());

                        // Enviar mensaje firmado al nuevo topic
                        String outputTopic = "json_signed_topic";
                        ProducerRecord<String, String> signedRecord = new ProducerRecord<>(outputTopic, record.key(), signedMessage);
                        producer.send(signedRecord, (metadata, exception) -> {
                            if (exception == null) {
                                log.info("Message sent to topic {} partition {} offset {}", metadata.topic(), metadata.partition(), metadata.offset());
                            } else {
                                log.error("Error while sending message", exception);
                            }
                        });

                    } catch (Exception e) {
                        log.error("Error processing record", e);
                    }
                }

                // Commit offsets (optional) ---> i
                //consumer.commitSync(); no va a leer los leídos anteriormente
            }
        } catch (Exception e) {
            log.error("An error occurred:", e);
        } finally {
            // Close the consumer
            consumer.close();
            log.info("Consumer closed.");
        }
    }
}