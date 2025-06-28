package com.cas.asistencias.exception;

public class ReunionNotFoundException extends RuntimeException {
    
    public ReunionNotFoundException(String message) {
        super(message);
    }
    
    public ReunionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ReunionNotFoundException(Long id) {
        super("Reunión no encontrada con ID: " + id);
    }
}
