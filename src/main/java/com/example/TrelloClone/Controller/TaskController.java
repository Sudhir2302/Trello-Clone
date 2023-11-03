package com.example.TrelloClone.Controller;

import com.example.TrelloClone.Models.Entity.Comment;
import com.example.TrelloClone.Models.Entity.History;
import com.example.TrelloClone.Models.Entity.Task;
import com.example.TrelloClone.Models.Entity.TaskUsers;
import com.example.TrelloClone.Models.Task.AddComment;
import com.example.TrelloClone.Models.Task.AddUser;
import com.example.TrelloClone.Models.Task.ModifyTask;
import com.example.TrelloClone.Models.Task.TaskResponse;
import com.example.TrelloClone.Service.TaskServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
public class TaskController {
    public final TaskServiceInterface taskServiceInterface;

    @GetMapping("/getAll")
    public ResponseEntity<List<TaskResponse>> getTasks(){
        return ResponseEntity.ok().body(taskServiceInterface.getAllTask());
    }

    @PostMapping("/add")
    public ResponseEntity<TaskResponse> saveTask(@RequestBody Task task) {
        return ResponseEntity.ok().body(taskServiceInterface.saveTask(task));
    }

    @PutMapping("/modifyTask")
    public ResponseEntity<TaskResponse> modifyTask(@RequestBody ModifyTask task) {
        TaskResponse taskResponse = new TaskResponse();
        Task updatedTask = taskServiceInterface.modifyTask(task).getTask();
        taskResponse.setTask(updatedTask);
        return ResponseEntity.ok().body(taskResponse);
    }

    @PutMapping("/addComment")
    public ResponseEntity<TaskResponse> addComment(@RequestBody AddComment addComment) {
        return ResponseEntity.ok().body(taskServiceInterface.addComment(addComment));
    }

    @PostMapping("/addUsers")
    public ResponseEntity<TaskResponse> addUsers(@RequestBody AddUser addUser) {
        return ResponseEntity.ok().body(taskServiceInterface.addUsers(addUser));
    }



    @GetMapping("/get")
    public ResponseEntity<TaskResponse> getTaskById(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskServiceInterface.getTask(taskID));
    }

    @PutMapping("/undo")
    public ResponseEntity<TaskResponse> undoTask(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskServiceInterface.undo(taskID));
    }

    @GetMapping("/getComments")
    public ResponseEntity<List<Comment>> getTaskCommentsById(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskServiceInterface.getComments(taskID));
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<TaskUsers>> getTaskUsersById(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskServiceInterface.getTaskUsers(taskID));
    }

    @GetMapping("/getHistory")
    public ResponseEntity<List<History>> getTaskHistoryById(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskServiceInterface.getHistory(taskID));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTask(@RequestParam long taskID) {
        taskServiceInterface.deleteTaskByID(taskID);
        return ResponseEntity.ok("Deleted task with taskId: " + taskID);
    }






}
