package com.cas.packinglist.controller;

import com.cas.packinglist.dto.PackingListDto;
import com.cas.packinglist.service.PackingListService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackingListControllerTests {

    @Mock
    private PackingListService packingListService;

    @InjectMocks
    private PackingListController packingListController;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    @Mock private SecurityContext mockedSecurityContext;
    @Mock private Authentication mockedAuthentication;
    @Mock private UserDetails mockedUserDetails;

    private final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Mock SecurityContextHolder to control getCurrentUserId() behavior
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getContext()).thenReturn(mockedSecurityContext);
        when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuthentication);
        when(mockedAuthentication.isAuthenticated()).thenReturn(true);
        when(mockedAuthentication.getPrincipal()).thenReturn(mockedUserDetails);
        // Assuming UserDetails.getUsername() can be parsed to Long for user ID
        // This matches the controller's getCurrentUserId implementation detail.
        when(mockedUserDetails.getUsername()).thenReturn(String.valueOf(TEST_USER_ID));
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close(); // Important to close static mocks
    }

    @Test
    void whenAuthenticatedUserRequestsList_shouldReturnListFromService() {
        PackingListDto expectedDto = new PackingListDto(1L, new ArrayList<>(), null, null);
        when(packingListService.getPackingListForUser(TEST_USER_ID)).thenReturn(expectedDto);

        ResponseEntity<PackingListDto> response = packingListController.getPackingList();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
        Mockito.verify(packingListService).getPackingListForUser(TEST_USER_ID);
    }

    @Test
    void whenGetCurrentUserIdFails_shouldReturnUnauthorized() {
        // Override the default mock for getUsername to simulate a non-Long parsable username
        when(mockedUserDetails.getUsername()).thenReturn("cannotParseThisAsLong");

        ResponseEntity<PackingListDto> responseEntity = packingListController.getPackingList();
        // Check against the @ExceptionHandler for IllegalStateException in the Controller
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody()); // Body should be the error message from handler
    }
    
    @Test
    void whenAuthenticationIsNull_getCurrentUserIdFails_shouldReturnUnauthorized() {
        when(mockedSecurityContext.getAuthentication()).thenReturn(null);

        ResponseEntity<PackingListDto> responseEntity = packingListController.getPackingList();
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
         assertNull(responseEntity.getBody());
    }


    @Test
    void whenAuthenticatedUserSavesList_shouldCallServiceAndReturnSavedDto() {
        PackingListDto inputDto = new PackingListDto(null, new ArrayList<>(), null, null);
        PackingListDto expectedSavedDto = new PackingListDto(1L, new ArrayList<>(), null, null); // List now has an ID

        when(packingListService.savePackingListForUser(TEST_USER_ID, inputDto)).thenReturn(expectedSavedDto);

        ResponseEntity<PackingListDto> response = packingListController.savePackingList(inputDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSavedDto, response.getBody());
        Mockito.verify(packingListService).savePackingListForUser(TEST_USER_ID, inputDto);
    }

    @Test
    void whenSaveForNonExistentUser_viaServiceThrowingException_shouldReturnNotFound() {
        PackingListDto inputDto = new PackingListDto();
        // Simulate service throwing ResourceNotFoundException (e.g., user ID from token is invalid)
        when(packingListService.savePackingListForUser(TEST_USER_ID, inputDto))
                .thenThrow(new PackingListService.ResourceNotFoundException("User not found with id: " + TEST_USER_ID));

        ResponseEntity<PackingListDto> responseEntity = packingListController.savePackingList(inputDto);
        
        // Check against the @ExceptionHandler for ResourceNotFoundException in the Controller
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody()); // Body should be the error message from handler
    }

    @Test
    void whenSaveAndGetCurrentUserIdFails_shouldReturnUnauthorized() {
        when(mockedUserDetails.getUsername()).thenReturn("cannotParseThisAsLong");
        PackingListDto inputDto = new PackingListDto();

        ResponseEntity<PackingListDto> responseEntity = packingListController.savePackingList(inputDto);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        Mockito.verify(packingListService, Mockito.never()).savePackingListForUser(anyLong(), any(PackingListDto.class));
    }
}
