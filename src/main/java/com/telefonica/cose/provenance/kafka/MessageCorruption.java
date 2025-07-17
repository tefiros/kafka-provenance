package com.telefonica.cose.provenance.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.Format;

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

    public static Document tamperAtSecondLevelXML(Document originalXml) {
        try {
            // Clonar el documento para no modificar el original
            Document tamperedDoc = originalXml.clone();
            Element root = tamperedDoc.getRootElement();

            // Buscar el primer hijo que sea un elemento complejo (con hijos)
            for (Element child : root.getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    Element tamperedMarker = new Element("tampered");
                    tamperedMarker.setText("true");
                    child.addContent(tamperedMarker);

                    System.out.println("Altered node: " + child.getName());
                    break;
                }
            }

            return tamperedDoc;
        } catch (Exception e) {
            System.err.println("Error tampering XML: " + e.getMessage());
            return originalXml;
        }
    }

    public static String toPrettyString(Document doc) {
        try {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            return xmlOutputter.outputString(doc);
        } catch (Exception e) {
            return null;
        }
    }

}

