package com.neiva.querillas.web.dto;

import com.neiva.querillas.domain.model.Naturaleza;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuerellaCreateDTO {

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 255, message = "La direccion es demasiado larga")
    private String direccion;

    @NotBlank(message = "La descripcion es obligatoria")
    private String descripcion;

    @NotNull(message = "La naturaleza es obligatoria")
    private Naturaleza naturaleza;   // ← enum fuerte

    private Long temaId;              // opcional
    private Long comunaId;            // opcional
    private Long inspectorAsignadoId; // opcional
    private Long asignadoPorId;       // opcional - usuario que asigna


    
    @Size(max = 255, message = "El barrio es demasiado largo")
    private String barrio;

    @Size(max = 20, message = "El género del querellante es demasiado largo")
    private String generoQuerellante;  // "M", "F", "OTRO", etc.

    @Size(max = 20, message = "El género del querellado es demasiado largo")
    private String generoQuerellado;

    @Size(max = 1024, message = "Las normas aplicables son demasiado largas")
    private String normasAplicables;

    @Size(max = 2048, message = "Las observaciones son demasiado largas")
    private String observaciones;
}
