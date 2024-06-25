package com.insa;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.parser.YangParserException;
import java.io.IOException;
import java.util.Properties;

public class ProducerAvro {

    static String key = "key1";
    static String userSchema = "{\"type\":\"record\"," +
            "\"name\":\"myrecord\"," +
            "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";

    public static void main(String[] args) throws IOException, YangParserException, DocumentException {
        System.out.println("Producer ->");

        Properties producerConfig = new Properties();

        producerConfig.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerConfig.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
        producerConfig.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());
        producerConfig.setProperty("schema.registry.url", "http://localhost:8081");

        KafkaProducer<Object, Object> producer = new KafkaProducer<>(producerConfig);

        String key = "key1";
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("f1", "value1");

        String topic = "topic.test-avro";

        ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, key, avroRecord);
        try {
            producer.send(record);
        } catch (SerializationException e) {
            System.out.println(e.getMessage());
        } finally {
            producer.flush();
            producer.close();
        }

        System.out.println("finish !");

    }
}
