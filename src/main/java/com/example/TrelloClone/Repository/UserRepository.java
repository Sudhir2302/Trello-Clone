package com.example.TrelloClone.Repository;


import com.example.TrelloClone.Models.Entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserDetails, Long> {
    UserDetails findByUserID(long userID);

    @Override
    UserDetails save(UserDetails userDetails);

    @Override
    List<UserDetails> findAll();
}
