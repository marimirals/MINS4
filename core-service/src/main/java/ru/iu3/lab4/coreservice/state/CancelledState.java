package ru.iu3.lab4.coreservice.state;

import ru.iu3.lab4.coreservice.model.Order;

public class CancelledState implements OrderState {

    @Override
    public boolean canAssignVehicle() {
        return false; // Отменённый заказ — нельзя назначать транспорт
    }

    @Override
    public void next(Order order) {
        throw new IllegalStateException("Отмененный заказ нельзя изменить.");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Заказ уже отменен.");
    }

    @Override
    public String getName() {
        return "CANCELLED";
    }

}