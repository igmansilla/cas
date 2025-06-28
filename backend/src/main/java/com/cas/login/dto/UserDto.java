package com.cas.login.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private Set<String> roles;
    // Puedes añadir más campos aquí que sean seguros de exponer,
    // por ejemplo, email, nombre completo si los tuvieras en la entidad User.
    // No incluyas la contraseña u otros datos sensibles.

    // Ejemplo de campos adicionales que podrían ser útiles:
    // private String fullName;
    // private String email;
}
