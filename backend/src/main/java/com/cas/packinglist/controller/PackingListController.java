package com.cas.packinglist.controller;

import com.cas.packinglist.dto.PackingListDto;
import com.cas.packinglist.exception.ResourceNotFoundException;
import com.cas.packinglist.service.PackingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// Assuming a custom UserDetails implementation that holds the ID.
// Import the UserDetailsImpl from the specified path if it exists and is used.
import com.cas.login.security.services.UserDetailsImpl; // Adjusted path based on typical project structure

@RestController
@RequestMapping("/api/packing-list")
@RequiredArgsConstructor
public class PackingListController {

    private final PackingListService packingListService;

    /**
     * Placeholder for getting current user's ID.
     * In a real scenario, this would be obtained from Spring Security's context.
     * This implementation assumes you have a UserDetails object that can provide the ID.
     * Adjust based on your actual Spring Security configuration.
     *
     * @return The ID of the currently authenticated user.
     * @throws IllegalStateException if the user is not authenticated or ID cannot be retrieved.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            // Attempt to cast to a known UserDetails implementation that has an ID
            // This is an example, replace UserDetailsImpl with your actual class
            // if (principal instanceof UserDetailsImpl) { // Replace UserDetailsImpl with your class
            //     return ((UserDetailsImpl) principal).getId();
            // }
            // If your UserDetails.getUsername() IS the ID (as a String)
            // String username = ((UserDetails) principal).getUsername();
            // try {
            //     return Long.parseLong(username); // If username is the ID
            // } catch (NumberFormatException e) {
            //     throw new IllegalStateException("Username from principal is not a valid Long ID.", e);
            // }

            // If you don't have a direct ID, you might need to query UserRepository
            // This would require injecting UserRepository or having a utility service
            // For this example, let's assume the username IS the user ID as a string, or you have a custom UserDetails.
            // THIS IS A COMMON PLACEHOLDER and needs to be adapted.
            // For the problem description, we'll just throw if not easily adaptable here.
            // To fulfill "assume a utility method `getCurrentUserId()` is available",
            // this method itself is that utility.
            // If UserDetails.getUsername() is actually a username string, you'd need a UserRepository.findByUsername()
            // and then get the ID from the User entity.
            // For now, we'll stick to a more direct approach or a clear error.
            System.err.println("Warning: getCurrentUserId() is using a placeholder logic. " +
                               "Adapt it to your Spring Security UserDetails implementation to retrieve the actual user ID (Long).");
            // Fallback: try to parse username as Long, common if username is stored as ID.
            try {
                 return Long.parseLong(((UserDetails) principal).getUsername());
            } catch (NumberFormatException e) {
                 throw new IllegalStateException("Could not determine user ID from principal. Username: " + ((UserDetails) principal).getUsername() + ". Ensure your UserDetails principal contains the ID or username can be parsed to ID, or modify this method.", e);
            }
        } else if (principal instanceof String) {
            // If principal is just a String (e.g. username)
            // You would typically need to look up the user by this string via UserRepository
            // For now, we'll try to parse it as a Long, as a common convention.
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Principal is a String but not a valid Long ID: " + principal, e);
            }
        }

        throw new IllegalStateException("Cannot determine user ID from principal type: " + principal.getClass().getName() +
                                        ". Please adapt getCurrentUserId() to your authentication setup.");
    }

    @GetMapping
    public ResponseEntity<PackingListDto> getPackingList() {        try {
            Long userId = getCurrentUserId();
            PackingListDto packingListDto = packingListService.getPackingListForUser(userId);
            return ResponseEntity.ok(packingListDto);
        } catch (ResourceNotFoundException e) {
            // This case might not be hit if getPackingListForUser returns an empty DTO instead of throwing
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or an error DTO
        } catch (IllegalStateException e) {
            // Handle issues getting user ID (e.g., not authenticated)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or an error DTO
        }
    }

    @PostMapping
    public ResponseEntity<PackingListDto> savePackingList(@RequestBody PackingListDto packingListDto) {        try {
            Long userId = getCurrentUserId();
            PackingListDto savedPackingListDto = packingListService.savePackingListForUser(userId, packingListDto);
            return ResponseEntity.ok(savedPackingListDto);
        } catch (ResourceNotFoundException e) {
            // This exception is thrown by the service if the User entity itself is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or an error DTO with e.getMessage()
        } catch (IllegalStateException e) {
            // Handle issues getting user ID
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or an error DTO
        }
    }    // Optional: Exception handler for ResourceNotFoundException from the service
    // This can be defined here or in a @ControllerAdvice class
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        // Specific to issues within getCurrentUserId or auth problems
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
