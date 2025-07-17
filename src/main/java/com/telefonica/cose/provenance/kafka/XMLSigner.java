package com.telefonica.cose.provenance.kafka;

import com.telefonica.cose.provenance.*;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

public class XMLSigner {

    static {
        org.apache.xml.security.Init.init();
    }

    public String process(String value) throws Exception {


        XMLSignatureInterface sign = new XMLSignature();
        XMLEnclosingMethodInterface enc = new XMLEnclosingMethods();
        Parameters params = new Parameters();

        String signature = sign.signing(value, params.getProperty("kid"));

        Document doc = loadXML(value);
        Document provenanceXML = enc.enclosingMethod(doc, signature);

        sign.saveXMLDocument(provenanceXML,"./test.xml");

        String signedXmlContent = new org.jdom2.output.XMLOutputter().outputString(provenanceXML);

        return signedXmlContent;


    }

    private static Document loadXML(String xmlContent) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(new java.io.StringReader(xmlContent));
    }

}
