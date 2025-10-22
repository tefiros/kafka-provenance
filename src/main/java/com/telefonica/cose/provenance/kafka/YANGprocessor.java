package com.telefonica.cose.provenance.kafka;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.ParsedSchema;


public class YANGprocessor {

    private final SchemaRegistryClient client;

    public YANGprocessor(String registryUrl) {
        // El segundo parámetro es el tamaño de cache local
        this.client = new CachedSchemaRegistryClient(registryUrl, 10);
    }

    public String getLatestYangSchema(String subject) throws Exception {
        // Obtiene directamente el esquema como texto
        SchemaMetadata metadata = client.getLatestSchemaMetadata(subject);
        return metadata.getSchema(); // ← este string contiene el módulo YANG completo
    }


    public static void main(String[] args) {
        try {

            String registryUrl = "http://localhost:8081";
            String subject = "interfaces-provenance-augmented";

            YANGprocessor processor = new YANGprocessor(registryUrl);
            String yangModule = processor.getLatestYangSchema(subject);

            // 🖨️ Lo mostramos por consola
            System.out.println("Módulo YANG obtenido desde el Schema Registry:");
            System.out.println("-------------------------------------------------");
            System.out.println(yangModule);
            System.out.println("-------------------------------------------------");

        } catch (Exception e) {
            System.err.println("❌ Error al obtener el módulo YANG: " + e.getMessage());
            e.printStackTrace();
        }
    }

}








