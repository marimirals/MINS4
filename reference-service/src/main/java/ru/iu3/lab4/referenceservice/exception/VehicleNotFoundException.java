package ru.iu3.lab4.referenceservice.exception;

public class VehicleNotFoundException extends TransportCompanyException {
    public VehicleNotFoundException(String id) { super("Vehicle not found: " + id); }
}