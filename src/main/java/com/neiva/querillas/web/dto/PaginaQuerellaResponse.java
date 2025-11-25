package com.neiva.querillas.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginaQuerellaResponse {

    // la lista de filas ya mapeadas a DTO
    private List<QuerellaResponse> items;

    // paging
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;

    // metadata de orden actual
    private String sort;
}
