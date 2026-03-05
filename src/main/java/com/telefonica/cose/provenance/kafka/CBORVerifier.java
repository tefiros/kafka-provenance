package com.telefonica.cose.provenance.kafka;

import com.telefonica.cose.provenance.CBORVerification;
import com.telefonica.cose.provenance.CBORVerificationInterface;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import com.upokecenter.cbor.CBORObject;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

public class CBORVerifier {

    public static String SIGNED_TOPIC = "yang.tests.cbor.signed";

    public static void main(String[] args) {

        // -------------------------------
        // CONSUMER CONFIG
        // -------------------------------
        Properties consumerConfig = new Properties();
        consumerConfig.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        consumerConfig.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "cbor-verifier23");
        consumerConfig.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ByteArrayDeserializer.class.getName());
        consumerConfig.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerConfig);

        consumer.subscribe(Collections.singletonList(SIGNED_TOPIC));

        System.out.println("Listening for signed CBOR messages...");

        CBORVerificationInterface verifier = new CBORVerification();

        Random random = new Random();

        // -------------------------------
        // LOOP
        // -------------------------------
        while (true) {

            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, byte[]> record : records) {

                try {

                    String key = record.key();
                    byte[] signedBytes = record.value();

                    System.out.println("Key: " + key);
                    System.out.println("CBOR bytes size: " + signedBytes.length);

                    // Decode CBOR
                    CBORObject cbor = CBORObject.DecodeFromBytes(signedBytes);

                    CBORObject cborToVerify = cbor;

                    System.out.println("Decoded Original CBOR:");
                    System.out.println(cbor);

                    boolean doTamper = random.nextBoolean();

                    if (doTamper) {
                        cborToVerify = MessageCorruption.tamperAtSecondLevelCBOR(cbor);
                        System.out.println("Tampered CBOR applied!");
                    } else {
                        System.out.println("Using original CBOR (no tampering)");
                    }

                    System.out.println("CBOR to verify:");
                    System.out.println(cborToVerify);

                    boolean valid = verifier.verify(cborToVerify);
                    System.out.println("Signature valid? " + valid);


                    if (valid) {
                        System.out.println("SIGNATURE STATUS: VALID");
                    } else {
                        System.out.println("SIGNATURE STATUS: INVALID");
                    }



                    System.out.println("Offset: " + record.offset());
                    System.out.println("--------------------------");

                } catch (Exception e) {
                    System.err.println("Error decoding CBOR message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
