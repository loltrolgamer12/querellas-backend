package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemSimpleDTO {
    private Object value;
    private String label;
}
