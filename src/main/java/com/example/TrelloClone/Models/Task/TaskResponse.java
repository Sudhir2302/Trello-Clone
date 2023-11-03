package com.example.TrelloClone.Models.Task;


import com.example.TrelloClone.Models.Entity.Task;
import com.example.TrelloClone.Models.Entity.UserDetails;
import lombok.Data;

import java.util.List;

@Data
public class TaskResponse {

    private Task task;
    private List<String> comments;
    private List<UserDetails> userDetails;
}
