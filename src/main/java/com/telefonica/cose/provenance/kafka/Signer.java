package com.telefonica.cose.provenance.kafka;

import com.telefonica.cose.provenance.*;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

public class Signer {

    static {
        org.apache.xml.security.Init.init();
    }

    public void process(String value) throws Exception {


        SignatureInterface sign = new Signature();
        EnclosingMethodInterface enc = new EnclosingMethods();
        Parameters params = new Parameters();

        String signature = sign.signing(value, params.getProperty("kid"));

        Document doc = loadXML(value);
        Document provenanceXML = enc.enclosingMethod(doc, signature);

        sign.saveXMLDocument(provenanceXML,"./test.xml");

        String signedXmlContent = new org.jdom2.output.XMLOutputter().outputString(provenanceXML);

        System.out.println("Signed Value: " + signedXmlContent);


    }

    private static Document loadXML(String xmlContent) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(new java.io.StringReader(xmlContent));
    }

}
