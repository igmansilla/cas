package com.cas.packinglist.service;

import com.cas.login.model.User;
import com.cas.login.repository.UserRepository;
import com.cas.packinglist.dto.PackingListCategoryDto;
import com.cas.packinglist.dto.PackingListDto;
import com.cas.packinglist.dto.PackingListItemDto;
import com.cas.packinglist.exception.ResourceNotFoundException;
import com.cas.packinglist.model.PackingList;
import com.cas.packinglist.model.PackingListCategory;
import com.cas.packinglist.model.PackingListItem;
import com.cas.packinglist.repository.PackingListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // For constructor injection
public class PackingListService {

    private final PackingListRepository packingListRepository;
    private final UserRepository userRepository; // Assuming this exists

    @Transactional
    public PackingListDto getPackingListForUser(Long userId) {
        Optional<PackingList> packingListOpt = packingListRepository.findByUserId(userId);
        if (packingListOpt.isPresent()) {
            return mapToDto(packingListOpt.get());
        } else {
            // Return a new, empty DTO if no list exists for the user.
            // This represents a transient list that hasn't been saved yet.
            // The user entity itself is not part of this DTO directly.
            PackingListDto newDto = new PackingListDto();
            newDto.setCategories(new ArrayList<>());
            // Set createdAt/updatedAt to now for a new transient DTO? Or leave null?
            // For now, leaving them null as it's not persisted.
            // newDto.setCreatedAt(Instant.now());
            // newDto.setUpdatedAt(Instant.now());
            return newDto;
        }
    }

    @Transactional
    public PackingListDto savePackingListForUser(Long userId, PackingListDto packingListDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        PackingList packingList = packingListRepository.findByUserId(userId)
                .orElseGet(() -> {
                    PackingList newPackingList = new PackingList();
                    newPackingList.setUser(user);
                    return newPackingList;
                });

        mapToEntity(packingListDto, user, packingList);
        PackingList savedPackingList = packingListRepository.save(packingList);
        return mapToDto(savedPackingList);
    }

    // --- Mapper Methods ---

    private PackingListDto mapToDto(PackingList packingList) {
        if (packingList == null) {
            return null;
        }
        PackingListDto dto = new PackingListDto();
        dto.setId(packingList.getId());
        if (packingList.getCategories() != null) {
            dto.setCategories(packingList.getCategories().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setCategories(new ArrayList<>());
        }
        dto.setCreatedAt(packingList.getCreatedAt());
        dto.setUpdatedAt(packingList.getUpdatedAt());
        return dto;
    }

    private PackingListCategoryDto mapToDto(PackingListCategory category) {
        if (category == null) {
            return null;
        }
        PackingListCategoryDto dto = new PackingListCategoryDto();
        dto.setId(category.getId());
        dto.setTitle(category.getTitle());
        dto.setDisplayOrder(category.getDisplayOrder());
        if (category.getItems() != null) {
            dto.setItems(category.getItems().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(new ArrayList<>());
        }
        return dto;
    }

    private PackingListItemDto mapToDto(PackingListItem item) {
        if (item == null) {
            return null;
        }
        PackingListItemDto dto = new PackingListItemDto();
        dto.setId(item.getId());
        dto.setText(item.getText());
        dto.setChecked(item.isChecked());
        dto.setDisplayOrder(item.getDisplayOrder());
        return dto;
    }

    // Maps DTO to an existing or new PackingList entity
    private void mapToEntity(PackingListDto dto, User user, PackingList packingListEntity) {
        // We don't map id, createdAt, updatedAt from DTO for the main list
        // User is already set for new lists or should be correct for existing.
        packingListEntity.setUser(user); // Ensure user is set

        // Clear and repopulate categories
        // This also handles orphanRemoval if configured on the entity
        packingListEntity.getCategories().clear();

        if (dto.getCategories() != null) {
            for (PackingListCategoryDto categoryDto : dto.getCategories()) {
                PackingListCategory categoryEntity = new PackingListCategory();
                // categoryEntity.setId(categoryDto.getId()); // Let JPA handle ID for new entities
                categoryEntity.setTitle(categoryDto.getTitle());
                categoryEntity.setDisplayOrder(categoryDto.getDisplayOrder());
                // categoryEntity.setPackingList(packingListEntity); // Set by helper method

                // Clear and repopulate items for this category
                // categoryEntity.getItems().clear(); // Not needed for new Category entity
                if (categoryDto.getItems() != null) {
                    for (PackingListItemDto itemDto : categoryDto.getItems()) {
                        PackingListItem itemEntity = new PackingListItem();
                        // itemEntity.setId(itemDto.getId()); // Let JPA handle ID
                        itemEntity.setText(itemDto.getText());
                        itemEntity.setChecked(itemDto.isChecked());
                        itemEntity.setDisplayOrder(itemDto.getDisplayOrder());
                        // itemEntity.setCategory(categoryEntity); // Set by helper method
                        categoryEntity.addItem(itemEntity); // Uses helper to set bidirectional link
                    }
                }
                packingListEntity.addCategory(categoryEntity); // Uses helper to set bidirectional link
            }
        }
    }
}
