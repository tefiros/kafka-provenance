package com.telefonica.cose.provenance.kafka;

import com.telefonica.cose.provenance.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONSigner {

    public void process(String value) throws Exception {


        JSONSignatureInterface sign = new JSONSignature();
        JSONEnclMethodInterface enc = new JSONEnclosingMethods();
        Parameters params = new Parameters();

        String signature = sign.signing(value, params.getProperty("kid"));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode doc = mapper.readTree(value);
        JsonNode provenanceJSON = enc.enclosingMethodJSON(doc, signature);

        sign.saveJSONnode(provenanceJSON, "./JSONtest.json");

        String signedJSONContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(provenanceJSON);

        System.out.println("Signed Value: " + signedJSONContent);


    }
}
