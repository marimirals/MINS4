package ru.iu3.lab4.referenceservice.service;

import ru.iu3.lab4.referenceservice.model.Vehicle;
import java.util.List;

public interface VehicleService {
    List<Vehicle> getAllVehicles();
    Vehicle getVehicleById(String id);
}