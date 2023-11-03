package com.example.TrelloClone.Repository;


import com.example.TrelloClone.Models.Entity.Task;
import com.example.TrelloClone.Models.Entity.TaskUsers;
import com.example.TrelloClone.Models.Entity.UserDetails;
import com.example.TrelloClone.Models.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskUsersRepository extends JpaRepository<TaskUsers, Long> {

    List<TaskUsers> findByTask(Task task);

    List<TaskUsers> findByUserDetails(UserDetails userDetails);

    @Override
    TaskUsers save(TaskUsers taskUsers);
}

