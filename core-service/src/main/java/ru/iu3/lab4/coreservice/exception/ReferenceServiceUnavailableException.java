package ru.iu3.lab4.coreservice.exception;

public class ReferenceServiceUnavailableException extends RuntimeException {
    public ReferenceServiceUnavailableException(String message) {
        super(message);
    }
}