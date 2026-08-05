package com.fooddelivery.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public GeminiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public JsonNode generateContent(ArrayNode contents, ArrayNode toolDeclarations) {
        ObjectNode body = mapper.createObjectNode();
        body.set("contents", contents);

        if (toolDeclarations != null && !toolDeclarations.isEmpty()) {
            ArrayNode tools = mapper.createArrayNode();
            ObjectNode toolWrapper = mapper.createObjectNode();
            toolWrapper.set("functionDeclarations", toolDeclarations);
            tools.add(toolWrapper);
            body.set("tools", tools);
        }

        String responseJson = restClient.post()
                .uri("/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString())
                .retrieve()
                .body(String.class);

        try {
            return mapper.readTree(responseJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + responseJson, e);
        }
    }
}