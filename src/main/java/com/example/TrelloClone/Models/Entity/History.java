package com.example.TrelloClone.Models.Entity;

import com.example.TrelloClone.Models.Task.TaskStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long taskHistoryID;

    private long taskID;
    @NonNull
    private String name;
    private String creationTime;
    private String estimatedTime;
    private TaskStatus status;
    private String description;
    private String updateTime;
    @NonNull
    private String modification;
    private String tag;
    private boolean isUndone;

}
