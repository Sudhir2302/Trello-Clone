package com.example.TrelloClone.Models.Task;

import lombok.Data;
import lombok.NonNull;

@Data
public class AddComment {

    @NonNull
    private long taskID;

    @NonNull
    private String comment;

    private long userID;

}
