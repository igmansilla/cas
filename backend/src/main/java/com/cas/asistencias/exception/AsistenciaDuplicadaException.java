package com.cas.asistencias.exception;

public class AsistenciaDuplicadaException extends RuntimeException {
    
    public AsistenciaDuplicadaException(String message) {
        super(message);
    }
    
    public AsistenciaDuplicadaException(Long reunionId, Long usuarioId) {
        super("Ya existe un registro de asistencia para la reunión ID: " + reunionId + 
              " y usuario ID: " + usuarioId);
    }
}
