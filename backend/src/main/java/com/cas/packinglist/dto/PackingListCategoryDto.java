package com.cas.packinglist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackingListCategoryDto {
    private Long id;
    private String title;
    private int displayOrder;
    private List<PackingListItemDto> items = new ArrayList<>();
}
