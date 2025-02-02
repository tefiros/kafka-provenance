package com.insa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insa.kafka.serializers.yang.json.KafkaYangJsonSchemaDeserializer;
import com.insa.kafka.serializers.yang.json.KafkaYangJsonSchemaDeserializerConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) {
        System.out.println("Consumer ->");

        Properties consumerConfig = new Properties();

        consumerConfig.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        consumerConfig.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "test");
        consumerConfig.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        consumerConfig.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaYangJsonSchemaDeserializer.class.getName());
        consumerConfig.setProperty(KafkaYangJsonSchemaDeserializerConfig.YANG_JSON_FAIL_INVALID_SCHEMA, "true");
        consumerConfig.setProperty(KafkaYangJsonSchemaDeserializerConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class.getName());
        consumerConfig.setProperty("schema.registry.url", "http://127.0.0.1:8081");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        KafkaConsumer<String, YangDataDocument> consumer = new KafkaConsumer<>(consumerConfig);
        String topic = "yang.tests";

        consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            try {
                ConsumerRecords<String, YangDataDocument> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, YangDataDocument> r : records) {
                    Headers headers = r.headers();
                    for (Header header : headers) {
                        System.out.println("[Header] Key: " + header.key() + ", Value: " + new String(header.value()));
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode;
                    jsonNode = mapper.readTree(r.value().getDocString());
                    System.out.println("Clé : " + r.key() + ", Valeur : " + jsonNode + ", Offset : " + r.offset());
                }
            } catch (RecordDeserializationException e) {
                System.out.println("Error during deserialization : message is ignored");
                consumer.seek(e.topicPartition(), e.offset() + 1L);
            } catch (JsonProcessingException ignored) {
                System.out.println("Error reading json");
            }
        }

    }
}
