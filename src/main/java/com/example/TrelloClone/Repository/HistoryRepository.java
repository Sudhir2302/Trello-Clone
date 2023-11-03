package com.example.TrelloClone.Repository;


import com.example.TrelloClone.Models.Entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByTaskID(long taskID);

    @Override
    History save(History history);
}
