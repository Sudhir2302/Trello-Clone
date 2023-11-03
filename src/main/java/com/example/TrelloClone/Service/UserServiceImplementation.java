package com.example.TrelloClone.Service;


import com.example.TrelloClone.Models.Entity.UserDetails;
import com.example.TrelloClone.Models.Task.TaskResponse;
import com.example.TrelloClone.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserServiceInterface {

    private final UserRepository userRepository;
    private final TaskServiceInterface taskServiceInterface;


    @Override
    public UserDetails saveUser(UserDetails userDetails) {
        return userRepository.save(userDetails);
    }

    @Override
    public UserDetails getUser(long userID) {
        return userRepository.findByUserID(userID);
    }

    @Override
    public List<UserDetails> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(long userID) {
        userRepository.deleteById(userID);
    }

    @Override
    public UserDetails updateUser(UserDetails userDetails) {
        // Implement the logic to update user details in the database
        // You can use userRepository to update the user's information
        UserDetails updatedUser = userRepository.save(userDetails);
        return updatedUser;
    }

    @Override
    public List<TaskResponse> getUserTasks(UserDetails user) {
        // Implement a method to retrieve tasks associated with the user from the taskServiceInterface.
        return taskServiceInterface.getTasksByUser(user);

    }
}

