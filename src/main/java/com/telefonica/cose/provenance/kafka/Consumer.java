package com.telefonica.cose.provenance.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

//public class Consumer {
//
//    private static final Logger log = LoggerFactory.getLogger(Consumer.class.getSimpleName());
//
//
//    public static void main(String[] args) {
//        log.info("Welcome to the Kafka Consumer");
//
//        String groupId = "kafka-consumer-test";
//        String topic = "json_topic";
//
//        //create Consumer Properties
//        // connection to localhost
//        Properties properties = new Properties();
//        properties.setProperty("bootstrap.servers", "localhost:9092");
//
//        //consumer config
//        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
//        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
//        properties.setProperty("group.id", groupId);
//        properties.setProperty("auto.offset.reset", "earliest"); //read entire history of the topic
//
//
//        //create the consumer
//        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
//
//
//        // Subscribe to the topic
//        consumer.subscribe(Collections.singletonList(topic));
//
//        Signer signer = new Signer();
//
//
//        try{
//            while(true){
//                System.out.println("Polling for records...");
//                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
//
//                if (records.isEmpty()) {
//                    System.out.println("No records found.");
//                } else {
//                    System.out.println("Found " + records.count() + " records.");
//                }
//
//                records.forEach(record -> {signer.process(record.value());});
//                consumer.commitSync();
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // Close the consumer
//            consumer.close();
//        }
//
//
//        //correct shutdown
//
//
//
////        final Thread mainThread = Thread.currentThread();
////        Runtime.getRuntime().addShutdownHook(new Thread() {
////            public void run() {
////                log.info("Shutting down");
////                consumer.wakeup();
////                try {
////                    mainThread.join();
////                }catch (InterruptedException e){
////                    e.printStackTrace();
////                }
////
////            }
////        });
//
////        try {
////            //subscribe to a topic
////            consumer.subscribe(Arrays.asList(topic));
////
////            //poll for  data
////
////            while (true) {
////                log.info("Polling");
////                ConsumerRecords<String, String> records = consumer.poll(1000);
////
////                for (ConsumerRecord<String, String> record : records) {
////                    log.info("Key: " + record.key() + ", Value: " + record.value());
////                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
////                }
////            }
////        } catch (WakeupException e){
////            log.info("Consumer starting to shut down");
////        } catch (Exception e){
////            log.error("Unexpected exception", e);
////        } finally {
////            consumer.close();
////            log.info("Consumer shut down correctly.");
////        }
//
//
//
//
//
//
//
//    }
//}


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

                records.forEach(record -> {
                    log.info("Received record: key = {}, value = {}", record.key(), record.value());
                    try {
                        signer.process(record.value());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

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