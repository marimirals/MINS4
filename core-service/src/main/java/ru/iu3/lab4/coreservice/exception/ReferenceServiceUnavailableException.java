package ru.iu3.lab4.coreservice.exception;

public class ReferenceServiceUnavailableException extends TransportCompanyException {

    public ReferenceServiceUnavailableException(String message) {
        super(message);
    }

    public ReferenceServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}