package com.example.TrelloClone.Controller;

import com.example.TrelloClone.Service.NoUndoHistoryException;
import com.example.TrelloClone.Service.TaskServiceInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskServiceInterface taskService;

    @Test
    void undoWithoutAvailableHistoryReturnsConflict() throws Exception {
        when(taskService.undo(99L)).thenThrow(new NoUndoHistoryException(99L));

        mockMvc.perform(put("/task/undo").param("taskID", "99"))
                .andExpect(status().isConflict())
                .andExpect(content().string("No unapplied history found for taskId: 99"));
    }
}
