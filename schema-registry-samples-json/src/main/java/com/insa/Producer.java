package com.insa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insa.kafka.serializers.yang.json.KafkaYangJsonSchemaSerializer;
import com.insa.kafka.serializers.yang.json.KafkaYangJsonSchemaSerializerConfig;
import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Producer {

    public static void main(String[] args) throws IOException, YangParserException, DocumentException {
        System.out.println("Producer ->");

        Properties producerConfig = new Properties();

        producerConfig.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        producerConfig.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
        producerConfig.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaYangJsonSchemaSerializer.class.getName());
        producerConfig.setProperty(KafkaYangJsonSchemaSerializerConfig.YANG_JSON_FAIL_INVALID_SCHEMA, "true");
        producerConfig.setProperty("schema.registry.url", "http://127.0.0.1:8081");

        KafkaProducer<Object, Object> producer = new KafkaProducer<>(producerConfig);

        YangSchemaContext schemaContext = YangYinParser.parse(Producer.class.getClassLoader().getResource("json/test2.yang").getFile());
        schemaContext.validate();
        JsonNode jsonNode = new ObjectMapper().readTree(new File(Producer.class.getClassLoader().getResource("json/invalid2.json").getFile()));
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument doc = new YangDataParser(jsonNode, schemaContext, false).parse(validatorResultBuilder);
        doc.validate();

        String key = "key1";
        String topic = "yang.tests";

        ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, key, doc);
        try {
            producer.send(record);
        } catch (SerializationException e) {
            System.out.println(e.getMessage());
        } finally {
            producer.flush();
            producer.close();
        }

        System.out.println("send -> " + jsonNode);
        System.out.println("finish !");

    }
}
