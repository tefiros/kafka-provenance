package com.telefonica.cose.provenance.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.upokecenter.cbor.CBORObject;

import com.telefonica.cose.provenance.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class CBORSigner {

    public static String TOPIC = "yang.tests.cbor";
    public static String SIGNED_TOPIC = "yang.tests.cbor.signed";

    public static void main(String[] args) {

        // -------------------------------
        // CONSUMER CONFIG
        // -------------------------------
        Properties consumerConfig = new Properties();
        consumerConfig.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        consumerConfig.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cbor-byte-consumer");
        consumerConfig.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        consumerConfig.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerConfig);
        consumer.subscribe(Collections.singletonList(TOPIC));

        // -------------------------------
        // PRODUCER CONFIG
        // -------------------------------
        Properties producerConfig = new Properties();
        producerConfig.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        producerConfig.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        producerConfig.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(producerConfig);



        // -------------------------------
        // CBOR mapper
        // -------------------------------
        ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());

        // -------------------------------
        // LOOP
        // -------------------------------
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, byte[]> record : records) {
                try {
                    String key = record.key();
                    byte[] cborBytes = record.value();

                    CBORSignatureInterface sign = new CBORSignature();
                    CBOREnclosingMethodsInterface enclose = new CBOREnclosingMethods();
                    Parameters param = new Parameters();

                    // Decodificar CBOR a JSON
                    CBORObject cborObject = CBORObject.DecodeFromBytes(cborBytes);


                    byte[] signature = sign.signingCBOR(cborObject,param.getProperty("kid"));


                    CBORObject provenanceCBOR = enclose.enclosingMethodCBOR(cborObject, signature);

                    String provenanceLegible = provenanceCBOR.toString();

                    System.out.println(provenanceLegible);

                    byte[] signedBytes = provenanceCBOR.EncodeToBytes();

                    ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(SIGNED_TOPIC, key, signedBytes);

                    producer.send(producerRecord);

                    System.out.println("Message sent to topic: " + SIGNED_TOPIC);
                    System.out.println("--------");


                } catch (Exception e) {
                    System.err.println("Error decoding CBOR message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

}
