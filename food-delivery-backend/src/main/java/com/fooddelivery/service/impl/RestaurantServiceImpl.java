package com.fooddelivery.service.impl;

import com.fooddelivery.dto.RestaurantRequest;
import com.fooddelivery.dto.RestaurantResponse;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public RestaurantResponse create(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .ownerId(request.getOwnerId())
                .active(true)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponse(saved);
    }

    @Override
    public RestaurantResponse getById(Long id) {
        Restaurant restaurant = findByIdOrThrow(id);
        return toResponse(restaurant);
    }

    @Override
    public List<RestaurantResponse> getAll() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RestaurantResponse update(Long id, RestaurantRequest request) {
        Restaurant restaurant = findByIdOrThrow(id);
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        if (request.getOwnerId() != null) {
            restaurant.setOwnerId(request.getOwnerId());
        }
        return toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public void delete(Long id) {
        Restaurant restaurant = findByIdOrThrow(id);
        restaurantRepository.delete(restaurant);
    }

    private Restaurant findByIdOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .ownerId(restaurant.getOwnerId())
                .active(restaurant.isActive())
                .menuItemCount(restaurant.getMenuItems() == null ? 0 : restaurant.getMenuItems().size())
                .build();
    }
}