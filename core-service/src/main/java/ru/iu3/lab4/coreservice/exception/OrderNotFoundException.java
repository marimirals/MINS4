package ru.iu3.lab4.coreservice.exception;

public class OrderNotFoundException extends TransportCompanyException {
    public OrderNotFoundException(String id) { super("Order not found: " + id); }
}