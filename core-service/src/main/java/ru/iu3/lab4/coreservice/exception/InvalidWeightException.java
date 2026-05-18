package ru.iu3.lab4.coreservice.exception;

public class InvalidWeightException extends TransportCompanyException {
    public InvalidWeightException(double weight) {
        super("Недопустимый вес: " + weight + " кг. Вес заказа должен быть от 1 до 1000 кг.");
    }
}