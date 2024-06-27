package com.insa;

import com.fasterxml.jackson.databind.JsonNode;
import com.insa.kafka.serializers.yang.json.KafkaYangJsonSchemaDeserializer;
import com.insa.kafka.serializers.yang.json.KafkaYangJsonSchemaDeserializerConfig;
import com.insa.kafka.serializers.yang.json.KafkaYangJsonSchemaSerializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args){
        System.out.println("Consumer ->");

        Properties consumerConfig = new Properties();

        consumerConfig.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        consumerConfig.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "test");
        consumerConfig.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        consumerConfig.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaYangJsonSchemaDeserializer.class.getName());
        consumerConfig.setProperty(KafkaYangJsonSchemaDeserializerConfig.YANG_JSON_FAIL_INVALID_SCHEMA, "true");
        consumerConfig.setProperty("schema.registry.url", "http://127.0.0.1:8081");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(consumerConfig);
        String topic = "yang.tests";

        consumer.subscribe(Collections.singletonList(topic));

        while(true){
            ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(100));

            for(ConsumerRecord<Object, Object> r: records){
                System.out.println("Clé : " + r.key() + ", Valeur : " + r.value() + ", Offset : " + r.offset());
            }
        }

    }
}
