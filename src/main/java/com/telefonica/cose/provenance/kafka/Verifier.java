package com.telefonica.cose.provenance.kafka;

import COSE.CoseException;
import com.telefonica.cose.provenance.XMLVerification;
import com.telefonica.cose.provenance.XMLVerificationInterface;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom2.Document;

import java.security.Security;

public class Verifier {

    static {
        // Register BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());
    }

    static {
        org.apache.xml.security.Init.init();
    }

    public static void main(String[] args) throws Exception {
//        String filepath = "./JSONtest.json";
//        JSONVerificationInterface ver = new JSONVerification();
//
//        JsonNode doc = ver.loadJSONDocument(filepath);

        String filepath = "./test.xml";
        XMLVerificationInterface ver = new XMLVerification();

        Document doc = ver.loadXMLDocument(filepath);

        // Verify COSE signature and content
        try {
            if (ver.verify(doc)) {
                System.out.println("\033[1m" + "Signature verified");
            } else {
                System.err.println("\033[1m" + "Invalid signature.");
            }
        } catch (CoseException e) {
            e.printStackTrace();
        }

    }
}
