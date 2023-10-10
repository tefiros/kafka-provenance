package com.insa;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {
    public static void main(String[] args) {
        // Configuration du producteur Kafka
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Adresse du serveur Kafka
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Création d'un producteur Kafka
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Envoyer un message à un sujet (topic) Kafka
        String topic = "channel";
        String message = "Hey !";

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message envoyé avec succès. Offset : " + metadata.offset());
            } else {
                System.err.println("Erreur lors de l'envoi du message : " + exception.getMessage());
            }
        });

        // Fermer le producteur Kafka
        producer.close();
    }
}
