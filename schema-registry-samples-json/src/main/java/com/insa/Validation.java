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
import org.yangcentral.yangkit.data.codec.json.YangDataWriterJson;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.*;

public class Validation {
    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        System.out.println("Lancement du programme !");

        String jsonFileValid = Validation.class.getClassLoader().getResource("validation/valid.json").getFile();
        String jsonFileInvalid = Validation.class.getClassLoader().getResource("validation/invalid.json").getFile();
        String jsonFileMissing = Validation.class.getClassLoader().getResource("validation/missing.json").getFile();
        String yangPath = Validation.class.getClassLoader().getResource("validation/test.yang").getFile();

        // parse yang file
        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validatorResult = schemaContext.validate();
        System.out.println("yang -> " + validatorResult);

        // parse json file : valid
        JsonNode elementValid = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            elementValid = objectMapper.readTree(new File(jsonFileValid));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse json file : invalid
        JsonNode elementInvalid = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            elementInvalid = objectMapper.readTree(new File(jsonFileInvalid));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // parse json file : missing
        JsonNode elementMissing = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            elementMissing = objectMapper.readTree(new File(jsonFileMissing));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ValidatorResultBuilder validatorResultBuilderValid = new ValidatorResultBuilder();
        YangDataDocument yangDataDocumentValid = new YangDataParser(elementValid, schemaContext, false).parse(validatorResultBuilderValid);

        ValidatorResultBuilder validatorResultBuilderInvalid = new ValidatorResultBuilder();
        YangDataDocument yangDataDocumentInvalid = new YangDataParser(elementInvalid, schemaContext, false).parse(validatorResultBuilderInvalid);

        ValidatorResultBuilder validatorResultBuilderMissing = new ValidatorResultBuilder();
        YangDataDocument yangDataDocumentMissing = new YangDataParser(elementMissing, schemaContext, false).parse(validatorResultBuilderMissing);

        yangDataDocumentInvalid.update();
        validatorResult = yangDataDocumentInvalid.validate();
        validatorResultBuilderInvalid.merge(validatorResult);
        System.out.println("yang data document invalid (false?)-> " + validatorResultBuilderInvalid.build());

        yangDataDocumentValid.update();
        validatorResult = yangDataDocumentValid.validate();
        validatorResultBuilderValid.merge(validatorResult);
        System.out.println("yang data document valid (true?)-> " + validatorResultBuilderValid.build());

        yangDataDocumentMissing.update();
        validatorResult = yangDataDocumentMissing.validate();
        validatorResultBuilderMissing.merge(validatorResult);
        System.out.println("yang data document missing (false?)-> " + validatorResultBuilderMissing.build());

        System.out.println("Fin du programme !");
    }
}