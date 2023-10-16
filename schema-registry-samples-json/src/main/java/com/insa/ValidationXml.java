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
    public static void main(String[] args) throws DocumentException, IOException, YangParserException {
        System.out.println("Lancement du programme !");

        String yangPath = Validation.class.getClassLoader().getResource("validation/test.yang").getFile();
        String xmlPath = Validation.class.getClassLoader().getResource("validation/test.xml").getFile();


        YangSchemaContext schemaContext = YangYinParser.parse(yangPath);
        ValidatorResult validatorResult = schemaContext.validate();

        SAXReader reader = new SAXReader();
        Document document;
        File xmlFile = new File(xmlPath);
        document = reader.read(xmlFile);

        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        YangDataDocument yangDataDocument = new YangDataParser(document, schemaContext, true).parse(validatorResultBuilder);
        yangDataDocument.update();
        validatorResult = yangDataDocument.validate();
        validatorResultBuilder.merge(validatorResult);
        System.out.println(validatorResultBuilder.build());

        System.out.println("Fin du programme !");
    }
}