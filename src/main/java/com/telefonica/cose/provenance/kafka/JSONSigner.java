package com.telefonica.cose.provenance.kafka;

import com.telefonica.cose.provenance.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class JSONSigner {

    public String process(String value) throws Exception {

        String yangSubject = "interfaces-provenance-augmented";
        YANGprocessor processor = new YANGprocessor("http://localhost:8081");
        String yangModule = processor.getLatestYangSchema(yangSubject);

        // 2Guardar temporalmente para parsear
        Path tempFile = Files.createTempFile("yang-module-", ".yang");
        Files.writeString(tempFile, yangModule);
        File yangFile = tempFile.toFile();

        //Extraer moduleName y leafName del módulo
        YANGMetadata metadata = YANGModuleProcessor.extractSignatureMetadata(yangFile);
        String moduleName = YANGModuleProcessor.extractModuleName(yangFile);
        String leafName = metadata.getLeafName();

        JSONSignatureInterface sign = new JSONSignature();
        JSONEnclMethodInterface enc = new JSONEnclosingMethods();
        Parameters params = new Parameters();

        String signature = sign.signing(value, params.getProperty("kid"));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode doc = mapper.readTree(value);

        JsonNode provenanceJSON = enc.enclosingMethodParam(doc, signature, moduleName, leafName);


        sign.saveJSONnode(provenanceJSON, "./JSONtest.json");

        String signedJSONContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(provenanceJSON);

        return signedJSONContent;


    }
}
