package com.insa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.NotificationMessage;
import org.yangcentral.yangkit.data.codec.json.NotificationMessageJsonCodec;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

public class ValidationNotification {

    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        URL yangUrl = ValidationNotification.class.getClassLoader().getResource("notification/yang");
        String yangDir = yangUrl.getFile();
        YangSchemaContext schemaContext = YangYinParser.parse(yangDir);
        ValidatorResult validatorResult = schemaContext.validate();

        InputStream jsonInputStreamValid = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/notification.json");
        Reader jsonReaderValid = new InputStreamReader(jsonInputStreamValid);
        ObjectMapper objectMapperValid = new ObjectMapper();
        JsonNode jsonNodeValid = objectMapperValid.readTree(jsonReaderValid);

        InputStream jsonInputStreamInvalid = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/invalid.json");
        Reader jsonReaderInvalid = new InputStreamReader(jsonInputStreamInvalid);
        ObjectMapper objectMapperInvalid = new ObjectMapper();
        JsonNode jsonNodeInvalid = objectMapperInvalid.readTree(jsonReaderInvalid);

        InputStream jsonInputStreamMissing = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/missing.json");
        Reader jsonReaderMissing = new InputStreamReader(jsonInputStreamMissing);
        ObjectMapper objectMapperMissing = new ObjectMapper();
        JsonNode jsonNodeMissing = objectMapperMissing.readTree(jsonReaderMissing);

        InputStream jsonInputStreamType = ValidationNotification.class.getClassLoader().getResourceAsStream("notification/json/incorrecttype.json");
        Reader jsonReaderType = new InputStreamReader(jsonInputStreamType);
        ObjectMapper objectMapperType = new ObjectMapper();
        JsonNode jsonNodeType = objectMapperType.readTree(jsonReaderType);

        ValidatorResultBuilder validatorResultBuilderValid = new ValidatorResultBuilder();
        NotificationMessageJsonCodec notificationMessageJsonCodecValid = new NotificationMessageJsonCodec(schemaContext);
        NotificationMessage message = notificationMessageJsonCodecValid.deserialize(jsonNodeValid, validatorResultBuilderValid);
        validatorResult = validatorResultBuilderValid.build();
        System.out.println("build(valid) -> " + validatorResult);
        validatorResult = message.validate();
        System.out.println("validate(valid) -> " + validatorResult);

        ValidatorResultBuilder validatorResultBuilderInvalid = new ValidatorResultBuilder();
        NotificationMessageJsonCodec notificationMessageJsonCodecInvalid = new NotificationMessageJsonCodec(schemaContext);
        NotificationMessage messageInvalid = notificationMessageJsonCodecInvalid.deserialize(jsonNodeInvalid, validatorResultBuilderInvalid);
        validatorResult = validatorResultBuilderInvalid.build();
        System.out.println("build(invalid) -> " + validatorResult);
        validatorResult = messageInvalid.validate();
        System.out.println("validate(invalid) -> " + validatorResult);

        ValidatorResultBuilder validatorResultBuilderMissing = new ValidatorResultBuilder();
        NotificationMessageJsonCodec notificationMessageJsonCodecMissing = new NotificationMessageJsonCodec(schemaContext);
        NotificationMessage messageMissing = notificationMessageJsonCodecMissing.deserialize(jsonNodeMissing, validatorResultBuilderMissing);
        validatorResult = validatorResultBuilderMissing.build();
        System.out.println("build(missing) -> " + validatorResult);
        validatorResult = messageMissing.validate();
        System.out.println("validate(missing) -> " + validatorResult);

        ValidatorResultBuilder validatorResultBuilderType = new ValidatorResultBuilder();
        NotificationMessageJsonCodec notificationMessageJsonCodecType = new NotificationMessageJsonCodec(schemaContext);
        NotificationMessage messageType = notificationMessageJsonCodecType.deserialize(jsonNodeType, validatorResultBuilderType);
        validatorResult = validatorResultBuilderType.build();
        System.out.println("build(type) -> " + validatorResult);
        validatorResult = messageType.validate();
        System.out.println("validate(type) -> " + validatorResult);
    }
}
