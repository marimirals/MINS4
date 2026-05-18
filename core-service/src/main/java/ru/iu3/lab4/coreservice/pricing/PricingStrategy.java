package ru.iu3.lab4.coreservice.pricing;

import ru.iu3.lab4.coreservice.model.Order;

public interface PricingStrategy {
    double calculate(Order order);
}
