package ru.iu3.lab4.referenceservice.service;

import org.springframework.stereotype.Service;
import ru.iu3.lab4.referenceservice.exception.VehicleNotFoundException;
import ru.iu3.lab4.referenceservice.model.Vehicle;
import ru.iu3.lab4.referenceservice.repository.VehicleRepository;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle getVehicleById(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id)); // ← вот он!
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

}