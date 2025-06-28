package com.cas.asistencias.exception;

public class AsistenciaNotFoundException extends RuntimeException {
    
    public AsistenciaNotFoundException(String message) {
        super(message);
    }
    
    public AsistenciaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AsistenciaNotFoundException(Long id) {
        super("Asistencia no encontrada con ID: " + id);
    }
    
    public AsistenciaNotFoundException(Long reunionId, Long usuarioId) {
        super("Asistencia no encontrada para reunión ID: " + reunionId + " y usuario ID: " + usuarioId);
    }
}
