package com.example.TrelloClone.Models.Task;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyTask {
    @NonNull
    long taskID;
    TaskStatus status;
    String name;
    String description;
    String comment;
    long userID;
}
