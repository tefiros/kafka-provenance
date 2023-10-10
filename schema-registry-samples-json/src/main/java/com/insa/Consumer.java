package com.insa;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) {
        // Configuration du consommateur Kafka
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Adresse du serveur Kafka
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "mon_groupe_de_consommateurs");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Création d'un consommateur Kafka
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // S'abonner à un sujet (topic) Kafka
        String topic = "channel";
        consumer.subscribe(Collections.singletonList(topic));

        // Boucle de consommation des messages
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> r : records) {
                System.out.println("Clé : " + r.key() + ", Valeur : " + r.value() + ", Offset : " + r.offset());
            }
        }
    }
}
