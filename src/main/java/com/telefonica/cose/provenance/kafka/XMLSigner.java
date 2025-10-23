package com.telefonica.cose.provenance.kafka;

import com.telefonica.cose.provenance.*;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLSigner {

    static {
        org.apache.xml.security.Init.init();
    }

    public String process(String value) throws Exception {

        String yangSubject = "interfaces-provenance-augmented";
        YANGprocessor processor = new YANGprocessor("http://localhost:8081");
        String yangModule = processor.getLatestYangSchema(yangSubject);

        // Guardar temporalmente para parsear
        Path tempFile = Files.createTempFile("yang-module-", ".yang");
        Files.writeString(tempFile, yangModule);
        File yangFile = tempFile.toFile();

        //Extraer moduleName y leafName del módulo
        YANGMetadata metadata = YANGModuleProcessor.extractSignatureMetadata(yangFile);
        String namespaceName = metadata.getNamespace();
        String leafName = metadata.getLeafName();
        XMLSignatureInterface sign = new XMLSignature();
        XMLEnclosingMethodInterface enc = new XMLEnclosingMethods();
        Parameters params = new Parameters();

        String signature = sign.signing(value, params.getProperty("kid"));

        Document doc = loadXML(value);
       // Document provenanceXML = enc.enclosingMethod(doc, signature);

        Document provenanceXML = enc.enclosingMethodParam(doc, signature, leafName, namespaceName);


        sign.saveXMLDocument(provenanceXML,"./test.xml");

        String signedXmlContent = new org.jdom2.output.XMLOutputter().outputString(provenanceXML);

        return signedXmlContent;


    }

    private static Document loadXML(String xmlContent) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(new java.io.StringReader(xmlContent));
    }

}
