package com.example.user.resource;

import com.example.user.dto.CardDto;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.RegisterRequest;
import com.example.user.entity.User;
import com.example.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserResource {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> saveUser(@RequestBody RegisterRequest request) throws URISyntaxException {
        log.info("REST request to register user: {}", request);

        User user = userService.saveUser(request);
        log.info("Saved user: {}", user);
        return ResponseEntity.created(new URI("/api/users/" + user.getId()))
                    .body("Registered successfully");

    }
    @PostMapping("/auth/sign-in")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("REST request to login");

        String username = userService.login(request);

        return ResponseEntity.ok().body(username);

    }


//    @GetMapping("/{userId}")
//    public Boolean ifUserExists(@PathVariable("userId") int userId) {
//        log.info("REST request to check if user exists: {}", userId);
//        return userService.checkUserExistenceById(userId);
//    }


    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Integer userId,
                                     @AuthenticationPrincipal UserDetails user) {
        log.info("REST request to get user details: {}", user);

        User foundUser = userService.getUserById(userId, user);
        log.info("Found user: {}", foundUser);

        return ResponseEntity.ok().body(foundUser);

    }

    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<CardDto>> getCardsOfUser(@PathVariable Integer userId,
                                          @AuthenticationPrincipal UserDetails user) {
        log.info("REST request to get cards of user: {}", userId);

        List<CardDto> cards = userService.getAllCardsOfUser(userId, user);
        log.info("Found cards: {}", cards);

        return ResponseEntity.ok().body(cards);
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("")
    public ResponseEntity<List<User>> getAllUsers(){
        log.info("REST request to get all users");
        List<User> users = userService.getAllUsers();
        log.info("All found users: {}", users);

        return ResponseEntity.ok().body(users);
    }


    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<User> updateUser(@PathVariable Integer userId, @Valid @RequestBody User user) {
        log.info("REST request to update user: {}", user);
        User updatedUser = userService.updateUser(userId, user);
        log.info("Updated user: {}", updatedUser);
        return ResponseEntity.ok().body(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Only administrators can delete users
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        log.info("REST request to delete user with id: {}", userId);
        userService.deleteUser(userId);
        log.info("Deleted user with id: {}", userId);
        return ResponseEntity.ok().body("Deleted successfully");
    }




}
