package com.telefonica.cose.provenance.kafka;

import com.fasterxml.jackson.databind.node.ObjectNode;
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

        // Guardar temporalmente para parsear
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

    public String processModuleLeaf (String value, String moduleName, String leafName) throws Exception {

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
    public String addCounterSignature(String value,
                                      String moduleName,
                                      String leafName,
                                      String counterKid) throws Exception {

        JSONSignature signer = new JSONSignature();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(value);
        ObjectNode rootObj = (ObjectNode) root;

        // 🔍 encontrar container dinámico
        String containerName = root.fieldNames().next();
        ObjectNode container = (ObjectNode) root.get(containerName);

        if (container == null || !container.has(leafName)) {
            throw new RuntimeException("Signature not found inside container");
        }

        // ✅ 1. extraer firma actual
        String existingSignature = container.get(leafName).asText();

        // ✅ 2. copiarla a root (hack necesario para la librería)
        rootObj.put(leafName, existingSignature);

        // ✅ 3. generar JSON con firma en root
        String jsonForSign = mapper.writeValueAsString(rootObj);

        // ✅ 4. countersign
        String counterSignature = signer.addCounterSign(
                jsonForSign,
                counterKid,
                leafName
        );

        // ✅ 5. quitar firma temporal de root
        rootObj.remove(leafName);

        // ✅ 6. volver a meterla en el container (correcto YANG)
        container.put(leafName, counterSignature);

        signer.saveJSONnode(rootObj, "./JSONtest.json");

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootObj);
    }

    private JsonNode upsertSignature(JsonNode json, String signature, String signatureElement) {

        if (json == null || !json.isObject()) {
            throw new IllegalArgumentException("El JSON root debe ser un objeto");
        }

        ObjectNode root = (ObjectNode) json.deepCopy();

        // buscar el container (primer nodo)
        String containerName = root.fieldNames().next();
        JsonNode container = root.get(containerName);

        if (container != null && container.isObject()) {
            ObjectNode containerObj = (ObjectNode) container;

            // actualizar dentro del container (YANG correcto)
            containerObj.put(signatureElement, signature);
        } else {
            // fallback (por si algún JSON no es YANG)
            root.put(signatureElement, signature);
        }

        return root;
    }


}
