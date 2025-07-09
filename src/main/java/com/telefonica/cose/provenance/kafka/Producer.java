package com.telefonica.cose.provenance.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Producer {

    private static final Logger log = LoggerFactory.getLogger(Producer.class.getSimpleName());


    public static void main(String[] args) throws IOException {
        log.info("Welcome to the Kafka Producer");

        String bootstrapServers = "localhost:9092";
        String topic = "json_topic";
        String filePath = "json-notif.json";

        String message = Files.readString(Paths.get(filePath));

        if (message == null) {
            System.out.println("Error reading JSON file.");
            return;
        }

        //create Producer Properties
        // connection to localhost
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());


        //create the Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        //create a Producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, message);



        //send data

        producer.send(producerRecord, (metadata, exception) -> {
            if (exception != null) {
                log.error("Error while producing", exception);
            } else {
                log.info("Produced record to topic {} partition {} @ offset {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });

        //flush and close the producer
        producer.flush();
        producer.close();
    }


    // Method to read JSON file content
    private static String readJsonFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}