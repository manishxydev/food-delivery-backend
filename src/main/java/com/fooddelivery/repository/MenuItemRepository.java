package com.fooddelivery.repository;

import com.fooddelivery.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Spring Data JPA generates the query from the method name — no SQL needed.
    List<MenuItem> findByRestaurantId(Long restaurantId);
}
