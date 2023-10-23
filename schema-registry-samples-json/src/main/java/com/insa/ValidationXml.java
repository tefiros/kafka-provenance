package com.insa;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.codec.xml.YangDataParser;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.parser.YangParserException;
import org.yangcentral.yangkit.parser.YangYinParser;

import java.io.File;
import java.io.IOException;

public class ValidationXml {

    public static void check(String type, String expected, String yangPath, String xmlPath) throws DocumentException, IOException, YangParserException {
        System.out.println("------- TYPE : " + type + " -------");
        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validator = schemaContext.validate();
        System.out.println("Schema context : " + validator);
        SAXReader reader = new SAXReader();
        Document document;
        File xmlFile = new File(xmlPath);
        document = reader.read(xmlFile);
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(document, schemaContext, true).parse(validatorResultBuilder);
        yangDataDocument.update();
        validator = yangDataDocument.validate();
        validatorResultBuilder.merge(validator);
        System.out.println("XML :\n" + document.asXML() + "\n");
        System.out.println("Validator Result Builder build (expected:"+ expected + ") : " + validatorResultBuilder.build());
        System.out.println();
    }
    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        System.out.println("Lancement du programme !");

        String yangPath = ValidationJson.class.getClassLoader().getResource("xml/test.yang").getFile();

        String validXml = ValidationJson.class.getClassLoader().getResource("xml/valid.xml").getFile();
        String invalidXml = ValidationJson.class.getClassLoader().getResource("xml/invalid.xml").getFile();
        String incorrectXml = ValidationJson.class.getClassLoader().getResource("xml/incorrect.xml").getFile();
        String missingXml = ValidationJson.class.getClassLoader().getResource("xml/missing.xml").getFile();

        check("VALID", "true", yangPath, validXml);
        check("INVALID", "false", yangPath, invalidXml);
        check("INCORRECT", "false", yangPath, incorrectXml);
        check("MISSING", "false", yangPath, missingXml);

        System.out.println("Fin du programme !");
    }
}