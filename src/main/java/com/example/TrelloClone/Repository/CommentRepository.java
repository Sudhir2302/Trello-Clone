package com.example.TrelloClone.Repository;

import com.example.TrelloClone.Models.Entity.Comment;
import com.example.TrelloClone.Models.Entity.Task;
import com.example.TrelloClone.Models.Entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTask(Task task);

    List<Comment> findByUserDetails(UserDetails userDetails);

    @Override
    Comment save(Comment comment);

}
