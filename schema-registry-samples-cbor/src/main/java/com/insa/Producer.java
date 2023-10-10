package com.insa;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class Producer {
    public static void main(String[] args) throws DocumentException, IOException, YangParserException {

        // Parsing Yang modules
        URL yangUrl = Producer.class.getClassLoader().getResource("yang");
        String yangDir = yangUrl.getFile();

        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult result = schemaContext.validate();
        System.out.println("Valid? " + result.isOk());
        System.out.println("Size modules = " + schemaContext.getModules().size());

        // Configure Kafka producer
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Adresse du serveur Kafka
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Send message to Kafka topic
        String topic = "yang.test";
        // TODO: read json and send JSON
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
