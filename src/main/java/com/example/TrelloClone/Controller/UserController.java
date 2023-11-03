package com.example.TrelloClone.Controller;

import com.example.TrelloClone.Models.Entity.UserDetails;
import com.example.TrelloClone.Models.Task.TaskResponse;
import com.example.TrelloClone.Service.TaskServiceInterface;
import com.example.TrelloClone.Service.UserServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserServiceInterface userServiceInterface;
    private final TaskServiceInterface taskServiceInterface;

    @PutMapping("/update")
    public ResponseEntity<UserDetails> updateUser(@RequestBody UserDetails userDetails) {
        UserDetails updatedUser = userServiceInterface.updateUser(userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/get")
    public ResponseEntity<UserDetails> getUserById(@RequestParam long userID) {
        return ResponseEntity.ok().body(userServiceInterface.getUser(userID));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<UserDetails>> getUsers() {
        return ResponseEntity.ok().body(userServiceInterface.getAllUsers());
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getUserTasks(@RequestParam long userID) {
        UserDetails user = userServiceInterface.getUser(userID);
        List<TaskResponse> userTasks = taskServiceInterface.getTasksByUser(user);
        return ResponseEntity.ok(userTasks);
    }


    @PostMapping("/add")
    public ResponseEntity<UserDetails> addUser(@RequestBody UserDetails userDetails) {
        return ResponseEntity.ok().body(userServiceInterface.saveUser(userDetails));
    }

    @DeleteMapping("/delete")
    public void deleteUser(@RequestParam long userID) {
        userServiceInterface.deleteUser(userID);
    }
}
