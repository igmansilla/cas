package com.cas.login.dto;

import com.cas.login.model.Acampante;
import com.cas.login.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for an Acampante, including details from their linked User account.")
public class AcampanteResponse {

    @Schema(description = "ID of the Acampante entity.", example = "1")
    private Long id;

    @Schema(description = "Full name of the Acampante.", example = "Juan Pérez")
    private String nombreCompleto;

    @Schema(description = "Age of the Acampante.", example = "12")
    private int edad;

    @Schema(description = "Name of the emergency contact.", example = "Ana García")
    private String contactoEmergenciaNombre;

    @Schema(description = "Phone number of the emergency contact.", example = "+1234567890")
    private String contactoEmergenciaTelefono;

    @Schema(description = "Username of the linked User account.", example = "juan.perez")
    private String username;

    @Schema(description = "Set of roles assigned to the linked User account.", example = "[\"ROLE_ACAMPANTE\"]")
    private Set<String> roles;

    public static AcampanteResponse fromEntity(Acampante acampante) {
        AcampanteResponse dto = new AcampanteResponse();
        dto.id = acampante.getId();
        dto.nombreCompleto = acampante.getNombreCompleto();
        dto.edad = acampante.getEdad();
        dto.contactoEmergenciaNombre = acampante.getContactoEmergenciaNombre();
        dto.contactoEmergenciaTelefono = acampante.getContactoEmergenciaTelefono();

        if (acampante.getUserAccount() != null) {
            dto.username = acampante.getUserAccount().getUsername();
            if (acampante.getUserAccount().getRoles() != null) {
                dto.roles = acampante.getUserAccount().getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet());
            }
        }
        return dto;
    }
}
