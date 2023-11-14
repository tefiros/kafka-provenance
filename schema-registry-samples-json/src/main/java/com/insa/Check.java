package com.insa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.NotificationMessageJsonCodec;
import org.yangcentral.yangkit.data.codec.json.YangDataParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.*;
import java.net.URL;

public class Check {

    public static void checkJSON(String type, String expected, String yangPath, String jsonPath) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TEST JSON : " + type + " -------");
        System.out.println("------- TYPE : " + type + " -------");
        yangPath = Check.class.getClassLoader().getResource(yangPath).getFile();
        jsonPath = Check.class.getClassLoader().getResource(jsonPath).getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validator = schemaContext.validate();
        System.out.println("Schema context : " + validator);
        JsonNode element = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            element = objectMapper.readTree(new File(jsonPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(element, schemaContext, false).parse(validatorResultBuilder);
        yangDataDocument.update();
        validator = yangDataDocument.validate(); // validation du document
        validatorResultBuilder.merge(validator); // ajout du résultat
        System.out.println("JSON : " + element);
        System.out.println("Validator Result Builder build (expected:"+ expected + ") : " + validatorResultBuilder.build()); // build
        System.out.println();
    }

    public static void checkJSON2(String type, String expected, String yangPath, String jsonPath) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TEST JSON : " + type + " -------");
        System.out.println("------- TYPE : " + type + " -------");
        URL yangUrl = Check.class.getClassLoader().getResource(yangPath);
        String yangDir = yangUrl.getFile();
        jsonPath = Check.class.getClassLoader().getResource(jsonPath).getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult validator = schemaContext.validate();
        System.out.println("Schema context : " + validator);
        JsonNode element = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            element = objectMapper.readTree(new File(jsonPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(element, schemaContext, false).parse(validatorResultBuilder);
        yangDataDocument.update();
        validator = yangDataDocument.validate(); // validation du document
        validatorResultBuilder.merge(validator); // ajout du résultat
        System.out.println("JSON : " + element);
        System.out.println("Validator Result Builder build (expected:"+ expected + ") : " + validatorResultBuilder.build()); // build
        System.out.println();
    }

    public static void checkNotification(String type, String expected, String yangPath, String notifPath) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TEST NOTIFICATION : " + type + " -------");
        System.out.println("------- TYPE :" + type + " -------");
        URL yangUrl = Check.class.getClassLoader().getResource(yangPath);
        InputStream notifInputStream = Check.class.getClassLoader().getResourceAsStream(notifPath);
        String yangDir = yangUrl.getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult validator = schemaContext.validate();

        Reader jsonReader = new InputStreamReader(notifInputStream);
        ObjectMapper objectMapperValid = new ObjectMapper();
        JsonNode jsonNode = objectMapperValid.readTree(jsonReader);
        System.out.println("JSON :\n" + jsonNode + "\n");

        ValidatorResultBuilder validatorResultBuilderValid = new ValidatorResultBuilder();
        NotificationMessageJsonCodec notificationMessageJsonCodecValid = new NotificationMessageJsonCodec(schemaContext);
        NotificationMessage message = notificationMessageJsonCodecValid.deserialize(jsonNode, validatorResultBuilderValid);
        validator = validatorResultBuilderValid.build();
        System.out.println("Validator Result Builder build (expected:"+ expected + ") : " + validator);
        validator = message.validate();
        System.out.println("Validator Result Builder validate (expected:"+ expected + ") : " + validator);
    }

    public static void checkXML(String type, String expected, String yangPath, String xmlPath) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TEST XML : " + type + " -------");
        System.out.println("------- TYPE : " + type + " -------");
        yangPath = Check.class.getClassLoader().getResource(yangPath).getFile();
        xmlPath = Check.class.getClassLoader().getResource(xmlPath).getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validator = schemaContext.validate();
        System.out.println("Schema context : " + validator);
        SAXReader reader = new SAXReader();
        Document document;
        File xmlFile = new File(xmlPath);
        document = reader.read(xmlFile);
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new org.yangcentral.yangkit.data.codec.xml.YangDataParser(document, schemaContext, true).parse(validatorResultBuilder);
        yangDataDocument.update();
        validator = yangDataDocument.validate();
        validatorResultBuilder.merge(validator);
        System.out.println("XML :\n" + document.asXML() + "\n");
        System.out.println("Validator Result Builder build (expected:"+ expected + ") : " + validatorResultBuilder.build());
        System.out.println();
    }
}