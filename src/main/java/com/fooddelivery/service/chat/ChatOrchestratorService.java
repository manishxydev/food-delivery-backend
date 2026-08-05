package com.fooddelivery.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatOrchestratorService {

    private static final int MAX_TOOL_ROUNDTRIPS = 5;

    private final GeminiClient geminiClient;
    private final ChatTools chatTools;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, ArrayNode> conversations = new ConcurrentHashMap<>();

    public ChatOrchestratorService(GeminiClient geminiClient, ChatTools chatTools) {
        this.geminiClient = geminiClient;
        this.chatTools = chatTools;
    }

    public String chat(String userId, String userMessage) {
        ArrayNode history = conversations.computeIfAbsent(userId, k -> mapper.createArrayNode());
        history.add(userTurn(userMessage));

        for (int i = 0; i < MAX_TOOL_ROUNDTRIPS; i++) {
            JsonNode response = geminiClient.generateContent(history, chatTools.getToolDeclarations());
            JsonNode candidateContent = response.at("/candidates/0/content");
            JsonNode parts = candidateContent.path("parts");

            JsonNode functionCall = findFunctionCall(parts);
            if (functionCall == null) {
                String text = extractText(parts);
                history.add(modelTextTurn(text));
                return text;
            }

            String toolName = functionCall.path("name").asText();
            JsonNode args = functionCall.path("args");
            String callId = functionCall.has("id") ? functionCall.get("id").asText() : null;

            // Echo back exactly what the model sent (preserves id, thought signatures, etc.)
            history.add(candidateContent);

            JsonNode toolResult = chatTools.executeTool(toolName, args , userId);
            history.add(functionResponseTurn(toolName, toolResult, callId));
        }

        return "Sorry, I'm having trouble completing that request right now — could you try rephrasing?";
    }

    public void resetConversation(String userId) {
        conversations.remove(userId);
    }

    private JsonNode findFunctionCall(JsonNode parts) {
        for (JsonNode part : parts) {
            if (part.has("functionCall")) return part.get("functionCall");
        }
        return null;
    }

    private String extractText(JsonNode parts) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode part : parts) {
            if (part.has("text")) sb.append(part.get("text").asText());
        }
        return sb.toString();
    }

    private ObjectNode userTurn(String message) {
        ObjectNode turn = mapper.createObjectNode();
        turn.put("role", "user");
        ArrayNode parts = mapper.createArrayNode();
        parts.add(mapper.createObjectNode().put("text", message));
        turn.set("parts", parts);
        return turn;
    }

    private ObjectNode modelTextTurn(String text) {
        ObjectNode turn = mapper.createObjectNode();
        turn.put("role", "model");
        ArrayNode parts = mapper.createArrayNode();
        parts.add(mapper.createObjectNode().put("text", text));
        turn.set("parts", parts);
        return turn;
    }

    private ObjectNode functionResponseTurn(String name, JsonNode result, String callId) {
        ObjectNode turn = mapper.createObjectNode();
        turn.put("role", "user");
        ArrayNode parts = mapper.createArrayNode();
        ObjectNode fr = mapper.createObjectNode();
        fr.put("name", name);
        if (callId != null) {
            fr.put("id", callId);
        }
        fr.set("response", result);
        ObjectNode part = mapper.createObjectNode();
        part.set("functionResponse", fr);
        parts.add(part);
        turn.set("parts", parts);
        return turn;
    }
}