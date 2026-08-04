package com.fooddelivery.service;

import com.fooddelivery.dto.MenuItemRequest;
import com.fooddelivery.dto.MenuItemResponse;

import java.util.List;

public interface MenuItemService {
    MenuItemResponse create(MenuItemRequest request);
    MenuItemResponse getById(Long id);
    List<MenuItemResponse> getByRestaurant(Long restaurantId);
    MenuItemResponse update(Long id, MenuItemRequest request);
    void delete(Long id);
}
