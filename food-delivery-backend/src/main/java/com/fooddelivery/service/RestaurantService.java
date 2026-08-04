package com.fooddelivery.service;

import com.fooddelivery.dto.RestaurantRequest;
import com.fooddelivery.dto.RestaurantResponse;

import java.util.List;

public interface RestaurantService {
    RestaurantResponse create(RestaurantRequest request);
    RestaurantResponse getById(Long id);
    List<RestaurantResponse> getAll();
    RestaurantResponse update(Long id, RestaurantRequest request);
    void delete(Long id);
}
