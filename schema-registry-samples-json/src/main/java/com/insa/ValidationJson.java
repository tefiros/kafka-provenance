package com.insa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.json.YangDataParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.*;

public class ValidationJson {

    public static void check(String type, String expected, String yangPath, String jsonPath) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TYPE :" + type + " -------");
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
        validator = yangDataDocument.validate();
        validatorResultBuilder.merge(validator);
        System.out.println("JSON : " + element);
        System.out.println("Validator Result Builder build (expected:"+ expected + ") : " + validatorResultBuilder.build());
        System.out.println();
    }
    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        System.out.println("Lancement du programme !");

        String jsonFileValid = ValidationJson.class.getClassLoader().getResource("json/valid.json").getFile();
        String jsonFileInvalid = ValidationJson.class.getClassLoader().getResource("json/invalid.json").getFile();
        String jsonFileMissing = ValidationJson.class.getClassLoader().getResource("json/missing.json").getFile();
        String jsonFileIncorrect = ValidationJson.class.getClassLoader().getResource("json/incorrect.json").getFile();
        String yangPath = ValidationJson.class.getClassLoader().getResource("json/test.yang").getFile();

        check("VALID", "true", yangPath, jsonFileValid);
        check("INVALID", "false", yangPath, jsonFileInvalid);
        check("INCORRECT", "false", yangPath, jsonFileIncorrect);
        check("MISSING", "false", yangPath, jsonFileMissing);

        System.out.println("Fin du programme !");
    }
}