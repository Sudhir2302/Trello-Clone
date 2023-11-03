package com.example.TrelloClone.Service;

import com.example.TrelloClone.Models.Entity.*;
import com.example.TrelloClone.Models.Task.AddComment;
import com.example.TrelloClone.Models.Task.AddUser;
import com.example.TrelloClone.Models.Task.ModifyTask;
import com.example.TrelloClone.Models.Task.TaskResponse;

import java.util.List;

public interface TaskServiceInterface {
    TaskResponse saveTask(Task task);

    TaskResponse getTask(long taskID);

    List<TaskResponse> getAllTask();

    TaskResponse addComment(AddComment addComment);

    TaskResponse addUsers(AddUser addUser);

    TaskResponse modifyTask(ModifyTask modifyTask);

    List<Comment> getComments(long taskID);

    List<TaskUsers> getTaskUsers(long taskID);

    List<History> getHistory(long taskID);

    void deleteTaskByID(long taskID);
    TaskResponse undo(long taskID);

    List<TaskResponse> getTasksByUser(UserDetails user);


}
