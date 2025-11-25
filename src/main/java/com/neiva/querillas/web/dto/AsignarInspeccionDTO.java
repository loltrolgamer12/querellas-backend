package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor

public class AsignarInspeccionDTO {
  
  @NotNull(message = "inspeccionId es obligatorio")
  private Long inspeccionId;
}
