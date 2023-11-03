package com.example.TrelloClone.Models.Entity;

import com.example.TrelloClone.Models.Task.TaskStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
    private long taskID;
    @NonNull
    private String name;
    private String creationTime;
    private String estimatedTime;
    private TaskStatus status;
    private String description;
    private String updateTime;

}
