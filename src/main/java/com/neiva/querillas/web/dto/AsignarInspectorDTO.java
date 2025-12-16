package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignarInspectorDTO {

  @NotNull(message = "inspectorId es obligatorio")
  private Long inspectorId;
}
