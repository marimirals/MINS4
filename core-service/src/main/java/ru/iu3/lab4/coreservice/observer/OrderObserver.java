package ru.iu3.lab4.coreservice.observer;

import ru.iu3.lab4.coreservice.model.Order;

public interface OrderObserver {
    void update(Order order, String message);
    String getNotifierType();

    // OOOOO - open/closed - открыт к расширению, закрыт к изменениям
    // меняется род класс вместо наследников -> изменяется а не расширяется
    /*
    void update(Order order, String message) {
        String type = getNotifierType();

        if ("EMAIL".equals(type)) {
            System.out.println("[EMAIL] Отправка на email:");
            System.out.println("   Заказ ID: " + order.getId());
            System.out.println("   Сообщение: " + message);
            System.out.println("   Маршрут: " + order.getFrom() + " → " + order.getTo());
        } else if ("SMS".equals(type)) {
            if (order.getStatus() == ru.iu3.lab1.transportcompany.model.OrderStatus.DELIVERED ||
                order.getStatus() == ru.iu3.lab1.transportcompany.model.OrderStatus.CANCELLED) {
                System.out.println("[SMS] Отправка SMS:");
                System.out.println("   Заказ ID: " + order.getId());
                System.out.println("   Текст: " + message);
            } else {
                System.out.println(" [SMS] Пропущено (не критичное изменение)");
            }
        } else {
            System.out.println("Неизвестный тип уведомлений: " + type);
        }
    }
    */
}