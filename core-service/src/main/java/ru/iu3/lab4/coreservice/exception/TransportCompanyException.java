package ru.iu3.lab4.coreservice.exception;

public class TransportCompanyException extends RuntimeException {
    public TransportCompanyException(String message) {
        super(message);
    }

    public TransportCompanyException(String message, Throwable cause) {
        super(message, cause);
    }
}