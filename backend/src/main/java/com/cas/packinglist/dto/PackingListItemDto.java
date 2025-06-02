package com.cas.packinglist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackingListItemDto {
    private Long id;
    private String text;
    private boolean isChecked;
    private int displayOrder;
}
