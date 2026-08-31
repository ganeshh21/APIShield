package com.apishield.apishield.service;
import com.apishield.apishield.exception.DuplicateEmailException;
import com.apishield.apishield.dto.UserRequest;
import com.apishield.apishield.entity.User;
import com.apishield.apishield.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.apishield.apishield.dto.UserResponse;


import java.util.List;
import com.apishield.apishield.exception.UserNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse  createUser(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());



        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }
    private UserResponse mapToUserResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }
    public UserResponse getUserById(Long id){
    User user = userRepository.findById(id)
            .orElseThrow(()->
                    new UserNotFoundException("User not Found with id :"+id));
    return mapToUserResponse(user);
    }

    /// Update user
    public UserResponse updateUser(Long id, UserRequest userRequest) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id)
                );
        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new DuplicateEmailException("Email already exists");
        }
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());

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
}
