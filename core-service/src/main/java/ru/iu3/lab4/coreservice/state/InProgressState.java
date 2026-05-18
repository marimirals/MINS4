package ru.iu3.lab4.coreservice.state;

import ru.iu3.lab4.coreservice.model.Order;
import ru.iu3.lab4.coreservice.model.OrderStatus;

public class InProgressState implements OrderState {

    @Override
    public boolean canAssignVehicle() {
        return false; // Транспорт уже назначен
    }

    @Override
    public String getName() {
        return "IN_PROGRESS";
    }

    @Override
    public void next(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        order.setState(new DeliveredState());

        order.notifyObservers("Статус изменён: IN_PROGRESS → DELIVERED");

        System.out.println("Заказ доставлен!");
    }

    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setState(new CancelledState());

        order.notifyObservers("Статус изменён: IN_PROGRESS → CANCELLED");

        System.out.println("Заказ отменен");
    }
}