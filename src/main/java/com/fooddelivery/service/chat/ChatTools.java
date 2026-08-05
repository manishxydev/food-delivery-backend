package com.fooddelivery.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

@Component
public class ChatTools {

    private final ObjectMapper mapper = new ObjectMapper();

    public ArrayNode getToolDeclarations() {
        ArrayNode tools = mapper.createArrayNode();
        try {
            tools.add(mapper.readTree(SEARCH_MENU_ITEMS_SCHEMA));
            tools.add(mapper.readTree(PLACE_ORDER_SCHEMA));
            tools.add(mapper.readTree(GET_ORDER_STATUS_SCHEMA));
        } catch (Exception e) {
            throw new RuntimeException("Invalid tool schema JSON", e);
        }
        return tools;
    }

    public JsonNode executeTool(String toolName, JsonNode args) {
        ObjectNode result = mapper.createObjectNode();
        try {
            switch (toolName) {
                case "searchMenuItems" -> {
                    String query = args.path("query").asText();
                    result.put("status", "TODO: wire to MenuItemService.search(...)");
                    result.put("query", query);
                }
                case "placeOrder" -> {
                    long restaurantId = args.path("restaurantId").asLong();
                    JsonNode items = args.path("items");
                    result.put("status", "TODO: wire to OrderService.placeOrder(...)");
                    result.put("restaurantId", restaurantId);
                    result.set("items", items);
                }
                case "getOrderStatus" -> {
                    long orderId = args.path("orderId").asLong();
                    result.put("status", "TODO: wire to OrderService.getOrderById(...)");
                    result.put("orderId", orderId);
                }
                default -> result.put("error", "Unknown tool: " + toolName);
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    private static final String SEARCH_MENU_ITEMS_SCHEMA = """
        {
          "name": "searchMenuItems",
          "description": "Search menu items by name, category, or keyword, optionally scoped to one restaurant. Use this whenever the user wants to browse or find food.",
          "parameters": {
            "type": "object",
            "properties": {
              "restaurantId": { "type": "integer", "description": "Optional - restrict the search to this restaurant's ID" },
              "query": { "type": "string", "description": "Keyword to search for, e.g. 'spicy', 'paneer', 'pizza'" },
              "maxPrice": { "type": "number", "description": "Optional upper price limit" }
            },
            "required": ["query"]
          }
        }
        """;

    private static final String PLACE_ORDER_SCHEMA = """
        {
          "name": "placeOrder",
          "description": "Place an order for the authenticated user with a list of menu items and quantities from one restaurant. Only call this after the user has clearly confirmed what they want to order.",
          "parameters": {
            "type": "object",
            "properties": {
              "restaurantId": { "type": "integer", "description": "The restaurant to order from" },
              "items": {
                "type": "array",
                "description": "Items to order",
                "items": {
                  "type": "object",
                  "properties": {
                    "menuItemId": { "type": "integer" },
                    "quantity": { "type": "integer" }
                  },
                  "required": ["menuItemId", "quantity"]
                }
              }
            },
            "required": ["restaurantId", "items"]
          }
        }
        """;

    private static final String GET_ORDER_STATUS_SCHEMA = """
        {
          "name": "getOrderStatus",
          "description": "Look up the current status of an existing order by its order ID.",
          "parameters": {
            "type": "object",
            "properties": {
              "orderId": { "type": "integer", "description": "The order ID to check" }
            },
            "required": ["orderId"]
          }
        }
        """;
}