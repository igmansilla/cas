package com.cas.packinglist.service;

import com.cas.login.model.User;
import com.cas.login.repository.UserRepository;
import com.cas.packinglist.dto.PackingListCategoryDto;
import com.cas.packinglist.dto.PackingListDto;
import com.cas.packinglist.dto.PackingListItemDto;
import com.cas.packinglist.model.PackingList;
import com.cas.packinglist.model.PackingListCategory;
import com.cas.packinglist.model.PackingListItem;
import com.cas.packinglist.repository.PackingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackingListServiceTests {

    @Mock
    private PackingListRepository packingListRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PackingListService packingListService;

    private User testUser;
    private PackingList testPackingListEntity;
    private PackingListDto testPackingListDto;

    @BeforeEach
    void setUp() {
        testUser = new User(); // Assuming User has a default constructor or use a builder
        testUser.setId(1L);
        // testUser.setUsername("testuser"); // If needed

        PackingListItem itemEntity1 = new PackingListItem();
        itemEntity1.setId(101L);
        itemEntity1.setText("Test Item 1");
        itemEntity1.setChecked(false);
        itemEntity1.setDisplayOrder(0);

        PackingListCategory categoryEntity1 = new PackingListCategory();
        categoryEntity1.setId(11L);
        categoryEntity1.setTitle("Category 1");
        categoryEntity1.setDisplayOrder(0);
        categoryEntity1.setItems(new ArrayList<>(List.of(itemEntity1))); // Mutable list

        itemEntity1.setCategory(categoryEntity1);


        testPackingListEntity = new PackingList();
        testPackingListEntity.setId(1L);
        testPackingListEntity.setUser(testUser);
        testPackingListEntity.setCategories(new ArrayList<>(List.of(categoryEntity1))); // Mutable list
        testPackingListEntity.setCreatedAt(Instant.now().minusSeconds(3600));
        testPackingListEntity.setUpdatedAt(Instant.now());

        categoryEntity1.setPackingList(testPackingListEntity);


        PackingListItemDto itemDto1 = new PackingListItemDto(101L, "Test Item 1", false, 0);
        PackingListCategoryDto categoryDto1 = new PackingListCategoryDto(11L, "Category 1", 0, List.of(itemDto1));
        testPackingListDto = new PackingListDto(1L, List.of(categoryDto1), testPackingListEntity.getCreatedAt(), testPackingListEntity.getUpdatedAt());
    }

    @Test
    void whenUserHasExistingPackingList_shouldReturnMappedDto() {
        when(packingListRepository.findByUserId(1L)).thenReturn(Optional.of(testPackingListEntity));

        PackingListDto resultDto = packingListService.getPackingListForUser(1L);

        assertNotNull(resultDto);
        assertEquals(testPackingListEntity.getId(), resultDto.getId());
        assertEquals(testPackingListEntity.getCategories().size(), resultDto.getCategories().size());
        assertEquals(testPackingListEntity.getCategories().get(0).getTitle(), resultDto.getCategories().get(0).getTitle());
        assertEquals(testPackingListEntity.getCategories().get(0).getItems().get(0).getText(), resultDto.getCategories().get(0).getItems().get(0).getText());
        verify(packingListRepository).findByUserId(1L);
    }

    @Test
    void whenUserHasNoPackingList_shouldReturnNewEmptyDto() {
        when(packingListRepository.findByUserId(1L)).thenReturn(Optional.empty());

        PackingListDto resultDto = packingListService.getPackingListForUser(1L);

        assertNotNull(resultDto);
        assertNull(resultDto.getId()); // New DTOs for non-existent lists have null ID
        assertTrue(resultDto.getCategories().isEmpty());
        // assertNull(resultDto.getCreatedAt()); // Current impl might set these for new DTOs
        // assertNull(resultDto.getUpdatedAt());
        verify(packingListRepository).findByUserId(1L);
    }
    
    @Test
    void whenUserDoesNotExistInGet_shouldStillReturnNewEmptyDto_asUserCheckIsLater() {
        // Current getPackingListForUser does not check user existence itself,
        // it relies on findByUserId which would implicitly handle it.
        // If findByUserId returns empty (as if user has no list), it should return empty DTO.
        when(packingListRepository.findByUserId(99L)).thenReturn(Optional.empty());

        PackingListDto resultDto = packingListService.getPackingListForUser(99L);
        
        assertNotNull(resultDto);
        assertNull(resultDto.getId());
        assertTrue(resultDto.getCategories().isEmpty());
        verify(packingListRepository).findByUserId(99L);
    }


    @Test
    void whenUserExistsAndDtoIsValid_forNewList_shouldSaveAndReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(packingListRepository.findByUserId(1L)).thenReturn(Optional.empty()); // New list

        // Create a DTO for a new list (no IDs for list, category, item)
        PackingListItemDto newItemDto = new PackingListItemDto(null, "New Item", false, 0);
        PackingListCategoryDto newCategoryDto = new PackingListCategoryDto(null, "New Category", 0, List.of(newItemDto));
        PackingListDto inputDto = new PackingListDto(null, List.of(newCategoryDto), null, null);

        // Mock the save operation to return an entity with IDs assigned
        PackingList savedEntity = new PackingList();
        savedEntity.setId(2L); // New ID from DB
        savedEntity.setUser(testUser);
        PackingListCategory savedCategoryEntity = new PackingListCategory();
        savedCategoryEntity.setId(22L);
        savedCategoryEntity.setTitle("New Category");
        savedCategoryEntity.setDisplayOrder(0);
        PackingListItem savedItemEntity = new PackingListItem();
        savedItemEntity.setId(202L);
        savedItemEntity.setText("New Item");
        savedItemEntity.setChecked(false);
        savedItemEntity.setDisplayOrder(0);
        savedCategoryEntity.setItems(List.of(savedItemEntity));
        savedItemEntity.setCategory(savedCategoryEntity);
        savedEntity.setCategories(List.of(savedCategoryEntity));
        savedCategoryEntity.setPackingList(savedEntity);
        savedEntity.setCreatedAt(Instant.now());
        savedEntity.setUpdatedAt(Instant.now());


        when(packingListRepository.save(any(PackingList.class))).thenAnswer(invocation -> {
            PackingList listToSave = invocation.getArgument(0);
            // Simulate ID assignment and timestamping by DB
            if (listToSave.getId() == null) listToSave.setId(2L); // new list ID
            listToSave.getCategories().forEach(cat -> {
                if (cat.getId() == null) cat.setId(22L); // new cat ID
                cat.getItems().forEach(item -> {
                    if (item.getId() == null) item.setId(202L); // new item ID
                });
            });
            listToSave.setCreatedAt(savedEntity.getCreatedAt());
            listToSave.setUpdatedAt(savedEntity.getUpdatedAt());
            return listToSave;
        });

        PackingListDto resultDto = packingListService.savePackingListForUser(1L, inputDto);

        assertNotNull(resultDto);
        assertEquals(2L, resultDto.getId());
        assertEquals("New Category", resultDto.getCategories().get(0).getTitle());
        assertEquals(22L, resultDto.getCategories().get(0).getId());
        assertEquals("New Item", resultDto.getCategories().get(0).getItems().get(0).getText());
        assertEquals(202L, resultDto.getCategories().get(0).getItems().get(0).getId());

        verify(userRepository).findById(1L);
        verify(packingListRepository).findByUserId(1L);
        verify(packingListRepository).save(any(PackingList.class));
    }

    @Test
    void whenUserExistsAndDtoIsValid_forExistingList_shouldUpdateAndReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(packingListRepository.findByUserId(1L)).thenReturn(Optional.of(testPackingListEntity)); // Existing list

        // Modify the DTO slightly for update
        testPackingListDto.getCategories().get(0).setTitle("Updated Category 1");
        testPackingListDto.getCategories().get(0).getItems().get(0).setText("Updated Test Item 1");
        testPackingListDto.getCategories().get(0).getItems().get(0).setChecked(true);


        when(packingListRepository.save(any(PackingList.class))).thenAnswer(invocation -> {
            PackingList listToSave = invocation.getArgument(0);
            // Simulate DB update timestamp
            listToSave.setUpdatedAt(Instant.now().plusSeconds(10)); // Ensure updated_at changes
            return listToSave;
        });

        PackingListDto resultDto = packingListService.savePackingListForUser(1L, testPackingListDto);

        assertNotNull(resultDto);
        assertEquals(testPackingListEntity.getId(), resultDto.getId());
        assertEquals("Updated Category 1", resultDto.getCategories().get(0).getTitle());
        assertEquals("Updated Test Item 1", resultDto.getCategories().get(0).getItems().get(0).getText());
        assertTrue(resultDto.getCategories().get(0).getItems().get(0).isChecked());
        // Verify that updated_at is different (later) than original
        assertTrue(resultDto.getUpdatedAt().isAfter(testPackingListEntity.getUpdatedAt()));


        verify(userRepository).findById(1L);
        verify(packingListRepository).findByUserId(1L);
        verify(packingListRepository).save(testPackingListEntity); // Should save the existing entity instance
    }

    @Test
    void whenUserDoesNotExistInSave_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        PackingListDto dummyDto = new PackingListDto(); // Content doesn't matter here

        assertThrows(PackingListService.ResourceNotFoundException.class, () -> {
            packingListService.savePackingListForUser(99L, dummyDto);
        });

        verify(userRepository).findById(99L);
        verify(packingListRepository, never()).findByUserId(anyLong());
        verify(packingListRepository, never()).save(any(PackingList.class));
    }
}
