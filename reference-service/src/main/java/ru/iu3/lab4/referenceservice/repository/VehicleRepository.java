package ru.iu3.lab4.referenceservice.repository;

import ru.iu3.lab4.referenceservice.model.Vehicle;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    void save(Vehicle vehicle);
    Optional<Vehicle> findById(String id);
    List<Vehicle> findAll();
}