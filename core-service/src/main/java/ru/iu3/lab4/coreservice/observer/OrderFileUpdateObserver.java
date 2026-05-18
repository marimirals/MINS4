package ru.iu3.lab4.coreservice.observer;

import org.springframework.stereotype.Component;
import ru.iu3.lab4.coreservice.model.Order;
import ru.iu3.lab4.coreservice.repository.OrderRepository;

@Component
public class OrderFileUpdateObserver implements OrderObserver {

    private final OrderRepository orderRepository;

    public OrderFileUpdateObserver(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void update(Order order, String message) {
        orderRepository.save(order);
        System.out.println("[OBSERVER] Заказ " + order.getId() +
                " обновлён в файле. Новый статус: " + order.getStatus());
    }

    @Override
    public String getNotifierType() {
        return "FILE_UPDATE";
    }
}