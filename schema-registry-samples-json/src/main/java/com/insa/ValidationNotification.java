package com.insa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
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

public class ValidationNotification {

    public static void check(String type, String expected,URL yangUrl, InputStream notifInputStream) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TYPE :" + type + " -------");
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

    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        URL yangUrl = ValidationNotification.class.getClassLoader().getResource("notification/yang");

        InputStream jsonInputStreamValid = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/notification.json");
        InputStream jsonInputStreamInvalid = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/invalid.json");
        InputStream jsonInputStreamMissing = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/missing.json");
        InputStream jsonInputStreamIncorrect = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/incorrecttype.json");

        check("VALID", "true", yangUrl, jsonInputStreamValid);
        check("INVALID", "false", yangUrl, jsonInputStreamInvalid);
        check("INCORRECT", "false", yangUrl, jsonInputStreamIncorrect);
        check("MISSING", "false", yangUrl, jsonInputStreamMissing);

    }
}
