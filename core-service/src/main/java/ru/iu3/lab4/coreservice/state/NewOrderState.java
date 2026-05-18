package ru.iu3.lab4.coreservice.state;

import ru.iu3.lab4.coreservice.model.Order;
import ru.iu3.lab4.coreservice.model.OrderStatus;

public class NewOrderState implements OrderState {

    @Override
    public boolean canAssignVehicle() {
        return true; // Новый заказ — можно назначать транспорт
    }

    @Override
    public String getName() {
        return "NEW";
    }

    @Override
    public void next(Order order) {
        if (order.getVehicleId() == null || order.getVehicleId().isEmpty()) {
            throw new IllegalStateException("Нельзя перевести заказ в выполнение без назначенного транспорта!");
        }
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setState(new InProgressState());

        order.notifyObservers("Статус изменён: NEW → IN_PROGRESS");

        System.out.println("Заказ перешел в состояние: В ПУТИ");
    }

    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setState(new CancelledState());

        order.notifyObservers("Статус изменён: NEW → CANCELLED");

        System.out.println("Заказ отменен");
    }
}