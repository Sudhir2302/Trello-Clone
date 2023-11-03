package com.example.TrelloClone.Service;

import com.example.TrelloClone.Models.Entity.UserDetails;
import com.example.TrelloClone.Models.Task.TaskResponse;

import java.util.List;

public interface UserServiceInterface {

    UserDetails saveUser(UserDetails userDetails);

    UserDetails updateUser(UserDetails userDetails);

    UserDetails getUser(long userID);

    List<UserDetails> getAllUsers();

    void deleteUser(long userID);

    List<TaskResponse> getUserTasks(UserDetails user);
}
