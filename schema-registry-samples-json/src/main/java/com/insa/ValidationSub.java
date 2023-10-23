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

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ValidationSub {
    public static void check(String type, String expected, URL yangUrl, String jsonPath) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TYPE :" + type + " -------");
        String yangDir = yangUrl.getFile();
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
        validator = yangDataDocument.validate();
        validatorResultBuilder.merge(validator);
        System.out.println("JSON : " + element);
        System.out.println("Validator Result Builder build (expected:"+ expected + ") : " + validatorResultBuilder.build());
        System.out.println();
    }

    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        URL yangUrl = ValidationSub.class.getClassLoader().getResource("jsonsub/yang");

        String jsonFileValid = ValidationSub.class.getClassLoader().getResource("jsonsub/valid.json").getFile();

        check("VALID", "true", yangUrl, jsonFileValid);
        // chercher où se passe la validation
        // faire les modifications pour que yangkit fasse
        // regarder les pattern
        // regarder comment se passe les imports de modules
    }
}
