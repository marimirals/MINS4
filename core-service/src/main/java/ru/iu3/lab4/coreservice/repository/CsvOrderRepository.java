package ru.iu3.lab4.coreservice.repository;

import org.springframework.stereotype.Repository;
import ru.iu3.lab4.coreservice.model.Order;
import ru.iu3.lab4.coreservice.model.OrderStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CsvOrderRepository implements OrderRepository {
    private final Path FILE = Paths.get("orders.csv");

    public CsvOrderRepository() { initFile(); }

    private void initFile() {
        try {
            if (!Files.exists(FILE)) Files.createFile(FILE);
        } catch (IOException e) {
            throw new StorageException("Не удалось инициализировать файл orders.csv", e);
        }
    }

    @Override
    public void save(Order order) {
        List<String> lines = readAllLines();
        String newLine = convertToCsv(order);
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            if (parts.length > 0 && parts[0].equals(order.getId())) {
                lines.set(i, newLine);
                writeAllLines(lines);
                return;
            }
        }
        lines.add(newLine);
        writeAllLines(lines);
    }

    @Override
    public Optional<Order> findById(String id) {
        return readAllLines().stream()
                .map(this::convertFromCsv)
                .filter(o -> o != null && o.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Order> findAll() {
        return readAllLines().stream()
                .map(this::convertFromCsv)
                .filter(o -> o != null)
                .collect(Collectors.toList());
    }

    private List<String> readAllLines() {
        try { return Files.readAllLines(FILE); }
        catch (IOException e) { throw new StorageException("чтение инвалид ", e); }
    }

    private void writeAllLines(List<String> lines) {
        try { Files.write(FILE, lines); }
        catch (IOException e) { throw new StorageException("запись инвалид", e); }
    }

    private String convertToCsv(Order order) {
        return String.format(Locale.US, "%s,%s,%s,%.2f,%s,%s,%.2f",
                order.getId(),
                order.getFrom() != null ? order.getFrom() : "",
                order.getTo() != null ? order.getTo() : "",
                order.getWeight(),
                order.getStatus().name(),
                order.getVehicleId() != null ? order.getVehicleId() : "",
                order.getPrice());
    }

    private Order convertFromCsv(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",");
        if (parts.length < 6) return null;
        try {
            Order order = new Order();
            order.setId(parts[0].trim());
            order.setFrom(parts[1].trim());
            order.setTo(parts[2].trim());
            order.setWeight(Double.parseDouble(parts[3].trim()));
            order.setStatus(OrderStatus.valueOf(parts[4].trim().toUpperCase()));
            switch (order.getStatus()) {
                case NEW -> order.setState(new ru.iu3.lab1.transportcompany.state.NewOrderState());
                case IN_PROGRESS -> order.setState(new ru.iu3.lab1.transportcompany.state.InProgressState());
                case DELIVERED -> order.setState(new ru.iu3.lab1.transportcompany.state.DeliveredState());
                case CANCELLED -> order.setState(new ru.iu3.lab1.transportcompany.state.CancelledState());
            }
            order.setVehicleId(parts[5].trim());
            order.setPrice(Double.parseDouble(parts[6].trim()));
            return order;
        } catch (Exception e) { return null; }
    }
}