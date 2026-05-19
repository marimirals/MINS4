package ru.iu3.lab4.coreservice.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.iu3.lab4.coreservice.exception.*;
import ru.iu3.lab4.coreservice.model.Order;
import ru.iu3.lab4.coreservice.model.OrderStatus;
import ru.iu3.lab4.coreservice.observer.OrderObserver;
import ru.iu3.lab4.coreservice.pricing.PriorityPricingStrategy;
import ru.iu3.lab4.coreservice.pricing.WeightBasedPricingStrategy;
import ru.iu3.lab4.coreservice.service.OrderService;
import ru.iu3.lab4.coreservice.grpc.ReferenceGrpcClient;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Scanner;

@Component
@Profile("!test")
public class ConsoleRunner implements CommandLineRunner {
    private final OrderService orderService;
    private final ReferenceGrpcClient referenceClient;
    private final Scanner scanner = new Scanner(System.in);

    private final WeightBasedPricingStrategy weightStrategy;
    private final PriorityPricingStrategy priorityStrategy;

    public ConsoleRunner(OrderService orderService,
                         ReferenceGrpcClient referenceClient,
                         WeightBasedPricingStrategy weightStrategy,
                         PriorityPricingStrategy priorityStrategy) {
        this.orderService = orderService;
        this.referenceClient = referenceClient;
        this.weightStrategy = weightStrategy;
        this.priorityStrategy = priorityStrategy;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Транспортная компания ===");
        while (true) {
            printMenu();
            int choice = getIntInput("Выбор: ");
            if (!handleChoice(choice)) break;
        }
    }

    private void printMenu() {
        System.out.println("\n1. Создать заказ");
        System.out.println("2. Посчитать стоимость заказа");
        System.out.println("3. Назначить транспорт");
        System.out.println("4. Изменить статус заказа");
        System.out.println("5. Показать все заказы");
        System.out.println("6. Показать весь транспорт");
        System.out.println("7. Отменить заказ");
        System.out.println("8. Сменить стратегию расчета");
        System.out.println("9. Показать подписчиков заказа");
        System.out.println("0. Выход");
    }

    private boolean handleChoice(int choice) {
        switch (choice) {
            case 1 -> createOrder();
            case 2 -> calculatePrice();
            case 3 -> assignVehicle();
            case 4 -> updateStatus();
            case 5 -> showAllOrders();
            case 6 -> showAllVehicles();
            case 7 -> cancelOrder();
            case 8 -> changePricingStrategy();
            case 9 -> showOrderObservers();
            case 0 -> { System.out.println("Пока!"); return false; }
            default -> System.out.println("Неверный выбор!");
        }
        return true;
    }

    private void createOrder() {
        System.out.print("Откуда: "); String from = scanner.nextLine();
        System.out.print("Куда: "); String to = scanner.nextLine();
        double weight = getDoubleInput("Вес (кг): ");
        try {
            Order order = orderService.createOrder(from, to, weight);
            System.out.println("Заказ создан! ID: " + order.getId());
        } catch (TransportCompanyException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void calculatePrice() {
        String id = getInput("ID заказа: ");
        try {
            double price = orderService.calculatePrice(id);
            System.out.println("Стоимость: " + price + " руб.");
        } catch (Exception e) { System.out.println( e.getMessage()); }
    }

    private void assignVehicle() {
        String orderId = getInput("ID заказа: ");
        showAllVehicles();
        String vehicleId = getInput("ID транспорта: ");
        try {
            orderService.assignVehicle(orderId, vehicleId);
            System.out.println("Транспорт назначен");
        } catch (TransportCompanyException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateStatus() {
        String orderId = getInput("ID заказа: ");
        System.out.println("Доступные статусы: NEW, IN_PROGRESS, DELIVERED, CANCELLED");
        String statusStr = getInput("Новый статус: ");
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.trim().toUpperCase());
            orderService.updateStatus(orderId, status);
            System.out.println("Статус обновлен");
        } catch (IllegalArgumentException e) {
            System.out.println("Неверный статус! Доступны: NEW, IN_PROGRESS, DELIVERED, CANCELLED");
        } catch (TransportCompanyException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void showAllOrders() {
        System.out.println("\n=== ВСЕ ЗАКАЗЫ ===");
        try {
            List<Order> orders = orderService.getAllOrders();
            if (orders.isEmpty()) { System.out.println("Заказов нет"); return; }
            for (Order o : orders) {
                System.out.printf("ID: %s | %s → %s | %.2f кг | %s | Транспорт: %s | Цена: %.2f%n",
                        o.getId(), o.getFrom(), o.getTo(), o.getWeight(),
                        o.getStatus(), o.getVehicleId() != null ? o.getVehicleId() : "не назначен", o.getPrice());
            }
        } catch (TransportCompanyException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void showAllVehicles() {
        System.out.println("\nДоступный транспорт:");
        try {
            var vehicles = referenceClient.getAllVehiclesSafe();
            if (vehicles.isEmpty()) {
                System.out.println("⚠Не удалось получить список транспорта (справочник недоступен)");
                return;
            }
            vehicles.forEach(v -> System.out.println(v.id() + " - " + v.type()));
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private String getInput(String prompt) { System.out.print(prompt); return scanner.nextLine(); }

    private int getIntInput(String prompt) {
        while (true) {
            try { System.out.print(prompt); return Integer.parseInt(scanner.nextLine()); }
            catch (NumberFormatException e) { System.out.println("Введите число!"); }
        }
    }
    private double getDoubleInput(String prompt) {
        while (true) {
            try { System.out.print(prompt); return Double.parseDouble(scanner.nextLine()); }
            catch (NumberFormatException e) { System.out.println("Введите число!"); }
        }
    }

    private void cancelOrder() {
        String orderId = getInput("ID заказа для отмены: ");
        try {
            orderService.cancelOrder(orderId);
            System.out.println("Заказ отменен");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void changePricingStrategy() {
        System.out.println("1. По весу (10 руб/кг) [ТЕКУЩАЯ]");
        System.out.println("2. Приоритетная (x1.5)");
        int choice = getIntInput("Выбор: ");
        try {
            if (choice == 1) {
                orderService.setPricingStrategy(weightStrategy);
                System.out.println("Установлена стратегия: По весу");
            } else if (choice == 2) {
                orderService.setPricingStrategy(priorityStrategy);
                System.out.println("Установлена стратегия: Приоритетная");
            } else {
                System.out.println("Неверный выбор");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void showOrderObservers() {
        String orderId = getInput("ID заказа: ");
        try {
            Order order = orderService.getAllOrders().stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst()
                    .orElseThrow(() -> new OrderNotFoundException(orderId));

            System.out.println("\n=== Подписчики заказа " + orderId + " ===");
            if (order.getObservers().isEmpty()) {
                System.out.println("Нет подписчиков");
            } else {
                for (OrderObserver observer : order.getObservers()) {
                    System.out.println("- " + observer.getNotifierType());
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}