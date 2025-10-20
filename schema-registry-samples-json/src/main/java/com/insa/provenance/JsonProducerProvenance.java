/*
 * Copyright 2025 INSA Lyon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.insa.provenance;

import ch.swisscom.kafka.serializers.yang.json.KafkaYangJsonSchemaSerializer;
import ch.swisscom.kafka.serializers.yang.json.KafkaYangJsonSchemaSerializerConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataDocumentJsonParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class JsonProducerProvenance {

    public static String KAFKA_TOPIC = "yang.tests";

    public static void main(String[] args) throws IOException, YangParserException, DocumentException {
        System.out.println("Starting Producer");

        Properties producerConfig = new Properties();

        producerConfig.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        producerConfig.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
        producerConfig.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaYangJsonSchemaSerializer.class.getName());
        producerConfig.setProperty(KafkaYangJsonSchemaSerializerConfig.YANG_JSON_FAIL_INVALID_SCHEMA, "true");
        producerConfig.setProperty(KafkaYangJsonSchemaSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class.getName());
        producerConfig.setProperty("schema.registry.url", "http://127.0.0.1:8081");

        // Creating producer
        KafkaProducer<String, YangDataDocument> producer = new KafkaProducer<>(producerConfig);

        // Parsing YANGs
        YangSchemaContext schemaContext = YangYinParser.parse(JsonProducerProvenance.class.getClassLoader().getResource("provenance/yang").getFile());
        ValidatorResult validatorResult = schemaContext.validate();
        System.out.println("YANG modules valid? " + validatorResult.isOk());

        // Parsing JSON
        JsonNode jsonNode = new ObjectMapper().readTree(new File(JsonProducerProvenance.class.getClassLoader().getResource("provenance/json/valid.json").getFile()));
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument doc = new YangDataDocumentJsonParser(schemaContext).parse(jsonNode, validatorResultBuilder);
        doc.update();
        ValidatorResult validatorResult1 = validatorResultBuilder.build();
        System.out.println("JSON valid? " + validatorResult1.isOk());

        ValidatorResult validatorResult2 = doc.validate();
        System.out.println("JSON valid? " + validatorResult2.isOk());

        String key = "key1";
        String topic = KAFKA_TOPIC;

        ProducerRecord<String, YangDataDocument> record = new ProducerRecord<>(topic, key, doc);
        try {
            producer.send(record);
        } catch (SerializationException e) {
            System.out.println(e.getMessage());
        } finally {
            producer.flush();
            producer.close();
        }

        System.out.println("Sent message " + jsonNode);
        System.out.println("End producer !");
    }
}
