package ru.iu3.lab4.coreservice.repository;

import ru.iu3.lab4.coreservice.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String id);
    List<Order> findAll();
}