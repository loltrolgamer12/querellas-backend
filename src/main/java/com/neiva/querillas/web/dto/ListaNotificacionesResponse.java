package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListaNotificacionesResponse {

    private List<NotificacionResponse> items;
    private long total;
    private long noLeidas;
}
