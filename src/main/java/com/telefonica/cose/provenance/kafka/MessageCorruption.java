package com.telefonica.cose.provenance.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class MessageCorruption {

    private static final Logger log = LoggerFactory.getLogger(MessageCorruption.class);

    public static JsonNode tamperAtSecondLevel(JsonNode originalJson) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Clonar bien el JSON para evitar mutaciones destructivas
            ObjectNode rootCopy = (ObjectNode) mapper.readTree(originalJson.toString());

            Iterator<Map.Entry<String, JsonNode>> fields = rootCopy.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode secondLevelNode = entry.getValue();

                if (secondLevelNode != null && secondLevelNode.isObject()) {
                    // Agregar campo "tampered" sin borrar nada
                    ObjectNode mutableSecondLevel = (ObjectNode) secondLevelNode;
                    mutableSecondLevel.put("tampered", true);

                    // Reinserta el objeto modificado
                    rootCopy.set(entry.getKey(), mutableSecondLevel);

                    log.info("Altered node: {}", entry.getKey());
                    break;
                }
            }

            return rootCopy;
        } catch (Exception e) {
            log.error("Error tampering JSON at second level", e);
            return originalJson;
        }
    }

}

