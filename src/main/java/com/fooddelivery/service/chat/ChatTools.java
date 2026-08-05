package com.fooddelivery.service.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fooddelivery.dto.MenuItemResponse;
import com.fooddelivery.dto.OrderItemRequest;
import com.fooddelivery.dto.OrderRequest;
import com.fooddelivery.dto.OrderResponse;
import com.fooddelivery.service.MenuItemService;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.RestaurantService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatTools {

    private final ObjectMapper mapper = new ObjectMapper();

    private final MenuItemService menuItemService;
    private final OrderService orderService;
    private final RestaurantService restaurantService;

    public ChatTools(MenuItemService menuItemService, OrderService orderService, RestaurantService restaurantService) {
        this.menuItemService = menuItemService;
        this.orderService = orderService;
        this.restaurantService = restaurantService;
    }

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

    /** userEmail is the authenticated user placing the request — needed for placeOrder. */
    public JsonNode executeTool(String toolName, JsonNode args, String userEmail) {
        ObjectNode result = mapper.createObjectNode();
        try {
            switch (toolName) {
                case "searchMenuItems" -> handleSearchMenuItems(args, result);
                case "placeOrder" -> handlePlaceOrder(args, userEmail, result);
                case "getOrderStatus" -> handleGetOrderStatus(args, result);
                default -> result.put("error", "Unknown tool: " + toolName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", e.getMessage() != null ? e.getMessage() : "Something went wrong");
        }
        return result;
    }

    private void handleSearchMenuItems(JsonNode args, ObjectNode result) {
        String query = args.path("query").asText("").toLowerCase();
        Double maxPrice = args.hasNonNull("maxPrice") ? args.get("maxPrice").asDouble() : null;

        List<MenuItemResponse> candidates;
        if (args.hasNonNull("restaurantId")) {
            candidates = menuItemService.getByRestaurant(args.get("restaurantId").asLong());
        } else {
            candidates = restaurantService.getAll().stream()
                    .flatMap(r -> menuItemService.getByRestaurant(r.getId()).stream())
                    .collect(Collectors.toList());
        }

        List<MenuItemResponse> matches = candidates.stream()
                .filter(mi -> query.isEmpty()
                        || mi.getName().toLowerCase().contains(query)
                        || (mi.getDescription() != null && mi.getDescription().toLowerCase().contains(query)))
                .filter(mi -> maxPrice == null || mi.getPrice().doubleValue() <= maxPrice)
                .limit(10)
                .collect(Collectors.toList());

        ArrayNode itemsArray = mapper.createArrayNode();
        for (MenuItemResponse mi : matches) {
            ObjectNode node = mapper.createObjectNode();
            node.put("menuItemId", mi.getId());
            node.put("name", mi.getName());
            node.put("price", mi.getPrice());
            node.put("restaurantId", mi.getRestaurantId());
            node.put("restaurantName", mi.getRestaurantName());
            itemsArray.add(node);
        }
        result.set("results", itemsArray);
        result.put("count", matches.size());
    }

    private void handlePlaceOrder(JsonNode args, String userEmail, ObjectNode result) {
        long restaurantId = args.path("restaurantId").asLong();
        List<OrderItemRequest> orderItems = new ArrayList<>();
        for (JsonNode itemNode : args.path("items")) {
            OrderItemRequest item = new OrderItemRequest();
            item.setMenuItemId(itemNode.path("menuItemId").asLong());
            item.setQuantity(itemNode.path("quantity").asInt());
            orderItems.add(item);
        }

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setRestaurantId(restaurantId);
        orderRequest.setItems(orderItems);

        OrderResponse response = orderService.createOrder(userEmail, orderRequest);
        result.put("orderId", response.getId());
        result.put("totalAmount", response.getTotalAmount());
        result.put("status", response.getStatus());
    }

    private void handleGetOrderStatus(JsonNode args, ObjectNode result) {
        long orderId = args.path("orderId").asLong();
        OrderResponse response = orderService.getById(orderId);
        result.put("orderId", response.getId());
        result.put("status", response.getStatus());
        result.put("totalAmount", response.getTotalAmount());
    }

    private static final String SEARCH_MENU_ITEMS_SCHEMA = """
        {
          "name": "searchMenuItems",
          "description": "Search menu items by name, description keyword, or price, optionally scoped to one restaurant. Use this whenever the user wants to browse or find food.",
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