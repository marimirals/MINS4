package ru.iu3.lab4.coreservice.pricing;

import org.springframework.stereotype.Component;
import ru.iu3.lab4.coreservice.model.Order;

@Component
public class PriorityPricingStrategy implements PricingStrategy {
    private static final double PRIORITY_COEFFICIENT = 1.5;

    @Override
    public double calculate(Order order) {
        // Срочный заказ: базовая цена * 1.5
        double basePrice = order.getWeight() * 10;
        return basePrice * PRIORITY_COEFFICIENT;
    }
}