package ru.iu3.lab4.referenceservice.repository;

import org.springframework.stereotype.Repository;
import ru.iu3.lab4.referenceservice.exception.StorageException;
import ru.iu3.lab4.referenceservice.model.Vehicle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CsvVehicleRepository implements VehicleRepository {
    private final Path FILE = Paths.get("vehicles.csv");

    public CsvVehicleRepository() { initFile(); }

    private void initFile() {
        try {
            if (!Files.exists(FILE)) {
                Files.createFile(FILE);
                save(new Vehicle("v1", "TRUCK"));
                save(new Vehicle("v2", "VAN"));
                save(new Vehicle("v3", "CAR"));
            }
        } catch (IOException e) {
            throw new StorageException("Не удалось инициализировать файл vehicles.csv", e);
        }
    }

    @Override
    public void save(Vehicle vehicle) {
        List<String> lines = readAllLines();
        String newLine = vehicle.getId() + "," + vehicle.getType();
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).split(",")[0].equals(vehicle.getId())) {
                lines.set(i, newLine);
                writeAllLines(lines);
                return;
            }
        }
        lines.add(newLine);
        writeAllLines(lines);
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return readAllLines().stream()
                .map(this::convertFromCsv)
                .filter(v -> v != null && v.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Vehicle> findAll() {
        return readAllLines().stream()
                .map(this::convertFromCsv)
                .collect(Collectors.toList());
    }

    private List<String> readAllLines() {
        try { return Files.readAllLines(FILE); }
        catch (IOException e) { throw new StorageException("Ошибка чтения vehicles.csv", e); }
    }

    private void writeAllLines(List<String> lines) {
        try { Files.write(FILE, lines); }
        catch (IOException e) { throw new StorageException("Ошибка записи vehicles.csv", e); }
    }

    private Vehicle convertFromCsv(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",");
        return new Vehicle(parts[0].trim(), parts[1].trim());
    }

}