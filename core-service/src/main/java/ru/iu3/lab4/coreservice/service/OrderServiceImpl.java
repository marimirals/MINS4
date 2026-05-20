package ru.iu3.lab4.coreservice.service;

import org.springframework.stereotype.Service;
import ru.iu3.lab4.coreservice.exception.InvalidWeightException;
import ru.iu3.lab4.coreservice.exception.OrderNotFoundException;
import ru.iu3.lab4.coreservice.exception.ReferenceServiceUnavailableException;
import ru.iu3.lab4.coreservice.grpc.ReferenceGrpcClient;
import ru.iu3.lab4.coreservice.model.Order;
import ru.iu3.lab4.coreservice.model.OrderStatus;
import ru.iu3.lab4.coreservice.pricing.PricingStrategy;
import ru.iu3.lab4.coreservice.pricing.WeightBasedPricingStrategy;
import ru.iu3.lab4.coreservice.repository.OrderRepository;
import ru.iu3.lab4.coreservice.state.CancelledState;
import ru.iu3.lab4.coreservice.state.DeliveredState;
import ru.iu3.lab4.coreservice.state.InProgressState;
import ru.iu3.lab4.coreservice.state.NewOrderState;
import ru.iu3.lab4.coreservice.observer.OrderFileUpdateObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private PricingStrategy currentStrategy;
    private final ReferenceGrpcClient referenceClient;
    private final OrderFileUpdateObserver fileUpdateObserver;

    public OrderServiceImpl(OrderRepository orderRepository,
                            WeightBasedPricingStrategy defaultStrategy,
                            ReferenceGrpcClient referenceClient,
                            OrderFileUpdateObserver fileUpdateObserver) {
        this.orderRepository = orderRepository;
        this.currentStrategy = defaultStrategy;
        this.referenceClient = referenceClient;
        this.fileUpdateObserver = fileUpdateObserver;
    }

    @Override
    public Order createOrder(String from, String to, double weight) {

        if (weight <= 0 || weight > 1000) {
            throw new InvalidWeightException(weight);
        }

        Order order = new Order(UUID.randomUUID().toString(), from, to, weight, OrderStatus.NEW, null, 0, new NewOrderState(), new ArrayList<>());

        order.setPrice(currentStrategy.calculate(order));

        order.attachObserver(fileUpdateObserver);

        order.setPrice(currentStrategy.calculate(order));

        orderRepository.save(order);

        return order;
    }

    @Override
    public void assignVehicle(String orderId, String vehicleId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        attachFileObserverIfMissing(order);

        referenceClient.validateVehicleOrThrow(vehicleId);

        if (!order.getState().canAssignVehicle()) {
            throw new IllegalStateException("Нельзя назначить транспорт в состоянии: " + order.getState().getName());
        }

        order.setVehicleId(vehicleId);
        order.getState().next(order);
        orderRepository.save(order);
    }

    @Override
    public void updateStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        attachFileObserverIfMissing(order);

        // Используем паттерн State
        if (order.getState() == null) {
            initializeState(order);
        }

        // Определяем, какое действие выполнить
        if (status == OrderStatus.CANCELLED) {
            order.getState().cancel(order);
        } else if (status == OrderStatus.IN_PROGRESS && order.getStatus() == OrderStatus.NEW) {
            order.getState().next(order);
        } else if (status == OrderStatus.DELIVERED && order.getStatus() == OrderStatus.IN_PROGRESS) {
            order.getState().next(order);
        } else if (status != order.getStatus()) {
            throw new IllegalStateException("Недопустимый переход из " + order.getStatus() + " в " + status);
        }

        orderRepository.save(order);
    }

    @Override
    public double calculatePrice(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return order.getPrice();
    }

    public void setPricingStrategy(PricingStrategy strategy) {
        this.currentStrategy = strategy;
    }

    @Override
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        attachFileObserverIfMissing(order);

        order.getState().cancel(order);
        orderRepository.save(order);
    }

    private void initializeState(Order order) {
        if (order.getStatus() == null) {
            order.setState(new NewOrderState());
        } else {
            switch (order.getStatus()) {
                case NEW -> order.setState(new NewOrderState());
                case IN_PROGRESS -> order.setState(new InProgressState());
                case DELIVERED -> order.setState(new DeliveredState());
                case CANCELLED -> order.setState(new CancelledState());
            }
        }
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private void attachFileObserverIfMissing(Order order) {
        // Прикрепляем, только если ещё не прикреплён
        boolean alreadyAttached = order.getObservers().stream()
                .anyMatch(o -> o instanceof OrderFileUpdateObserver);

        if (!alreadyAttached) {
            order.attachObserver(fileUpdateObserver);
        }
    }
}
