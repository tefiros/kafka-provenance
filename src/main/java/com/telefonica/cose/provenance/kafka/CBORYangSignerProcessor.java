package com.telefonica.cose.provenance.kafka;

import com.telefonica.cose.provenance.*;
import com.upokecenter.cbor.CBORObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class CBORYangSignerProcessor {

    public CBORObject process(byte[] cborBytes) throws Exception {

        String yangSubject = "interfaces-provenance-augmented";

        // obtener YANG desde schema registry
        YANGprocessor processor = new YANGprocessor("http://localhost:8081");
        String yangModule = processor.getLatestYangSchema(yangSubject);

        // guardar temporalmente para parsearlo
        Path tempFile = Files.createTempFile("yang-module-", ".yang");
        Files.writeString(tempFile, yangModule);
        File yangFile = tempFile.toFile();

        // extraer metadata
        YANGMetadata metadata = YANGModuleProcessor.extractSignatureMetadata(yangFile);
        String moduleName = YANGModuleProcessor.extractModuleName(yangFile);
        String leafName = metadata.getLeafName();

        // CBOR original
        CBORObject cborObject = CBORObject.DecodeFromBytes(cborBytes);

        CBORSignatureInterface sign = new CBORSignature();
        CBOREnclosingMethodsInterface enc = new CBOREnclosingMethods();
        Parameters params = new Parameters();

        // firmar
        byte[] signature = sign.signingCBOR(cborObject, params.getProperty("kid"));

        // enclosing con metadata YANG
        CBORObject provenanceCBOR = enc.enclosingMethodParamCBOR(cborObject, signature, moduleName, leafName);

        return provenanceCBOR;
    }
}
