package com.telefonica.cose.provenance.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telefonica.cose.provenance.VerifyHackathon;
import com.telefonica.cose.provenance.exception.COSESignatureException;

import java.io.StringReader;
import java.security.Security;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class VerifierHackathonConsumer {

    private static final Logger log = LoggerFactory.getLogger(VerifierConsumer.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
        org.apache.xml.security.Init.init();
    }

    public static void main(String[] args) {

        log.info("Starting VerifierConsumer (log only)...");

        String topic = "signed_messages";
        String bootstrapServers = "localhost:9092";

        // Ruta a tu clave pública
        String pemPath = "pubkey.pem";

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", bootstrapServers);
        props.setProperty("key.deserializer", StringDeserializer.class.getName());
        props.setProperty("value.deserializer", StringDeserializer.class.getName());
        props.setProperty("group.id", "verifier-group");
        props.setProperty("auto.offset.reset", "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        VerifyHackathon verifier = new VerifyHackathon();

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));

                for (ConsumerRecord<String, String> record : records) {

                    String message = record.value();

                    log.info("--------------------------------------------------");
                    log.info("Received message (offset={}):", record.offset());
                    log.debug("Payload:\n{}", message);

                    try {
                        SAXBuilder builder = new SAXBuilder();
                        Document doc = builder.build(new StringReader(message));

                        boolean valid = verifier.verify(doc, pemPath);

                        if (valid) {
                            log.info("VERIFICATION RESULT: VALID");
                        } else {
                            log.warn("VERIFICATION RESULT: INVALID");
                        }

                    } catch (COSESignatureException e) {
                        log.error("VERIFICATION ERROR (COSE): {}", e.getMessage());
                    } catch (Exception e) {
                        log.error("ERROR parsing/verifying message", e);
                    }
                }
            }

        } finally {
            consumer.close();
        }
    }
}