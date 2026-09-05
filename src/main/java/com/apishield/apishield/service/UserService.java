package com.apishield.apishield.service;
import com.apishield.apishield.dto.LoginRequest;
import com.apishield.apishield.dto.LoginResponse;
import com.apishield.apishield.enums.Role;
import com.apishield.apishield.exception.DuplicateEmailException;
import com.apishield.apishield.dto.UserRequest;
import com.apishield.apishield.entity.User;
import com.apishield.apishield.exception.InvalidCredentialsException;
import com.apishield.apishield.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.apishield.apishield.dto.UserResponse;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import com.apishield.apishield.exception.UserNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    //User Creation
    public UserResponse createUser(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.USER);


        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());


        return response;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        if (!isAdmin && !user.getEmail().equals(email)) {
            throw new AccessDeniedException("You cannot access this user");
        }

        return mapToUserResponse(user);
    }

    /// Update user
    public UserResponse updateUser(Long id, UserRequest userRequest) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String email = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        if (!isAdmin && !user.getEmail().equals(email)) {
            throw new AccessDeniedException(
                    "You cannot update this user"
            );
        }

        if (userRepository.existsByEmailAndIdNot(
                userRequest.getEmail(), id)) {
            throw new DuplicateEmailException(
                    "Email already exists"
            );
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(
                passwordEncoder.encode(userRequest.getPassword())
        );

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    //to Delete
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id)
                );

        userRepository.delete(user);
    }

    //loginRequestValidation
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password")
                );
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }



        String token = jwtService.generateToken(user.getEmail(),user.getRole().name());

        return new LoginResponse(token);
    }
    //Current user profile
    public UserResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return mapToUserResponse(user);
    }
}
