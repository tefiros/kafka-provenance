<<<<<<< HEAD:schema-registry-samples-cbor/src/main/java/com/insa/Producer.java
package com.insa;

<<<<<<<< HEAD:schema-registry-samples-cbor/src/main/java/com/insa/Producer.java
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
|||||||| 02550c0:KafkaProducer/src/main/java/com/app/insa/Main.java
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
========
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.NotificationMessageJsonCodec;
import org.yangcentral.yangkit.data.codec.json.YangDataParser;
import org.yangcentral.yangkit.data.codec.json.YangDataWriterJson;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;
>>>>>>>> init-project:KafkaProducer/src/main/java/com/app/insa/Main.java

<<<<<<<< HEAD:schema-registry-samples-cbor/src/main/java/com/insa/Producer.java
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
|||||||| 02550c0:KafkaProducer/src/main/java/com/app/insa/Main.java
import java.util.Properties;
========
import java.io.*;
>>>>>>>> init-project:KafkaProducer/src/main/java/com/app/insa/Main.java

<<<<<<<< HEAD:schema-registry-samples-cbor/src/main/java/com/insa/Producer.java
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
|||||||| 02550c0:KafkaProducer/src/main/java/com/app/insa/Main.java
public class Main {
    public static void main(String[] args) {
        // Configuration du producteur Kafka
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Adresse du serveur Kafka
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
========
public class Main {
    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        System.out.println("Lancement du programme !");
>>>>>>>> init-project:KafkaProducer/src/main/java/com/app/insa/Main.java

<<<<<<<< HEAD:schema-registry-samples-cbor/src/main/java/com/insa/Producer.java
        // Create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
|||||||| 02550c0:KafkaProducer/src/main/java/com/app/insa/Main.java
        // Création d'un producteur Kafka
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
========
        String jsonFileValid = Main.class.getClassLoader().getResource("valid.json").getFile();
        String jsonFileInvalid = Main.class.getClassLoader().getResource("invalid.json").getFile();
        String jsonFileMissing = Main.class.getClassLoader().getResource("missing.json").getFile();
        String yangPath = Main.class.getClassLoader().getResource("test.yang").getFile();
>>>>>>>> init-project:KafkaProducer/src/main/java/com/app/insa/Main.java

<<<<<<<< HEAD:schema-registry-samples-cbor/src/main/java/com/insa/Producer.java
        // Send message to Kafka topic
        String topic = "yang.test";
        // TODO: read json and send JSON
        String message = "Hey !";
|||||||| 02550c0:KafkaProducer/src/main/java/com/app/insa/Main.java
        // Envoyer un message à un sujet (topic) Kafka
        String topic = "channel";
        String message = "Hey !";
========
        // parse yang file
        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validatorResult = schemaContext.validate();
        System.out.println("yang -> " + validatorResult);
>>>>>>>> init-project:KafkaProducer/src/main/java/com/app/insa/Main.java

        // parse json file : valid
        JsonNode elementValid = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            elementValid = objectMapper.readTree(new File(jsonFileValid));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse json file : invalid
        JsonNode elementInvalid = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            elementInvalid = objectMapper.readTree(new File(jsonFileInvalid));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse json file : missing
        JsonNode elementMissing = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            elementMissing = objectMapper.readTree(new File(jsonFileMissing));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidatorResultBuilder validatorResultBuilderValid = new ValidatorResultBuilder();
        YangDataDocument yangDataDocumentValid = new YangDataParser(elementValid, schemaContext, false).parse(validatorResultBuilderValid);

        ValidatorResultBuilder validatorResultBuilderInvalid = new ValidatorResultBuilder();
        YangDataDocument yangDataDocumentInvalid = new YangDataParser(elementInvalid, schemaContext, false).parse(validatorResultBuilderInvalid);

        ValidatorResultBuilder validatorResultBuilderMissing = new ValidatorResultBuilder();
        YangDataDocument yangDataDocumentMissing = new YangDataParser(elementMissing, schemaContext, false).parse(validatorResultBuilderMissing);

        yangDataDocumentInvalid.update();
        validatorResult = yangDataDocumentInvalid.validate();
        validatorResultBuilderInvalid.merge(validatorResult);
        System.out.println("yang data document invalid (false?)-> " + validatorResultBuilderInvalid.build());

        yangDataDocumentValid.update();
        validatorResult = yangDataDocumentValid.validate();
        validatorResultBuilderValid.merge(validatorResult);
        System.out.println("yang data document valid (true?)-> " + validatorResultBuilderValid.build());

        yangDataDocumentMissing.update();
        validatorResult = yangDataDocumentMissing.validate();
        validatorResultBuilderMissing.merge(validatorResult);
        System.out.println("yang data document missing (false?)-> " + validatorResultBuilderMissing.build());

        System.out.println("Fin du programme !");
    }
}
||||||| 02550c0:schema-registry-samples-cbor/src/main/java/com/insa/Producer.java
=======
package com.app.insa;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {

    public Producer(){
        // Configuration du producteur Kafka
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Adresse du serveur Kafka
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        ProducerRecord producerRecord = new ProducerRecord("channel","msg", "hey");

        KafkaProducer kafkaProducer = new KafkaProducer(properties);

        kafkaProducer.send(producerRecord, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message envoyé avec succès. Offset : " + metadata.offset());
            } else {
                System.err.println("Erreur lors de l'envoi du message : " + exception.getMessage());
            }
        });

        // Fermer le producteur Kafka
        kafkaProducer.close();

    }

}
>>>>>>> init-project:KafkaProducer/src/main/java/com/app/insa/Producer.java
