package com.cas.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// We are not using java.util.Set here currently.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new Acampante linked to a User account by a Dirigente.")
public class AcampanteCreateRequest {

    @Schema(description = "Username for the new Acampante's user account. Must be unique.", example = "juan.perez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Password for the new Acampante's user account. If left empty or not provided, the backend may generate one.", example = "securePassword123")
    private String password;

    @Schema(description = "Full name of the Acampante.", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombreCompleto;

    @Schema(description = "Age of the Acampante.", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private int edad;

    @Schema(description = "Name of the emergency contact for the Acampante.", example = "Ana García", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactoEmergenciaNombre;

    @Schema(description = "Phone number of the emergency contact for the Acampante.", example = "+1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contactoEmergenciaTelefono;

    // Roles are not part of the request as it's fixed to ROLE_ACAMPANTE by the service.
}
