package ru.iu3.lab4.coreservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.iu3.lab4.coreservice.observer.OrderObserver;
import ru.iu3.lab4.coreservice.state.OrderState;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private String from;
    private String to;
    private double weight;
    private OrderStatus status;
    private String vehicleId;
    private double price;
    private OrderState state;

    private List<OrderObserver> observers = new ArrayList<>();

    public void attachObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void detachObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        for (OrderObserver observer : observers) {
            observer.update(this, message);
        }
    }
}

