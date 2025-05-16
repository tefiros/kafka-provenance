package com.insa.example;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataManagement {
    /**
     * Returns the value of the "data" field from the given JsonNode.
     * Throws IllegalArgumentException if the "data" field does not exist.
     */
    public static JsonNode unwrapData(JsonNode jsonNode) {
        JsonNode dataNode = jsonNode.get("data");
        if (dataNode == null) {
            throw new IllegalArgumentException("'data' key not found in the JsonNode");
        }
        return dataNode;
    }

    /**
     * Wraps the given JsonNode in a new ObjectNode under the "data" key.
     */
    public static JsonNode wrapInData(JsonNode contentNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode wrapper = mapper.createObjectNode();
        wrapper.set("data", contentNode);
        return wrapper;
    }


}
