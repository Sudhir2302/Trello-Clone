package com.example.TrelloClone.Service;

import com.example.TrelloClone.Models.Entity.History;
import com.example.TrelloClone.Models.Entity.Task;
import com.example.TrelloClone.Models.Task.TaskStatus;
import com.example.TrelloClone.Repository.CommentRepository;
import com.example.TrelloClone.Repository.HistoryRepository;
import com.example.TrelloClone.Repository.TaskRepository;
import com.example.TrelloClone.Repository.TaskUsersRepository;
import com.example.TrelloClone.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplementationTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskUsersRepository taskUsersRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void saveTaskUsesFreshTimestampForEachOperation() {
        TaskServiceImplementation taskService = taskService();
        doReturn(LocalDateTime.of(2026, 1, 1, 10, 0, 0))
                .doReturn(LocalDateTime.of(2026, 1, 1, 10, 0, 1))
                .when(taskService)
                .currentTime();
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskUsersRepository.findByTask(any(Task.class))).thenReturn(List.of());
        when(commentRepository.findByTask(any(Task.class))).thenReturn(List.of());

        Task firstTask = new Task();
        firstTask.setName("First");
        Task secondTask = new Task();
        secondTask.setName("Second");

        taskService.saveTask(firstTask);
        taskService.saveTask(secondTask);

        assertThat(firstTask.getCreationTime()).isEqualTo("01-01-2026 10:00:00");
        assertThat(secondTask.getCreationTime()).isEqualTo("01-01-2026 10:00:01");
    }

    @Test
    void undoDoesNotReuseHistoryFromPriorRequest() {
        TaskServiceImplementation taskService = taskService();
        Task task = new Task();
        task.setStatus(TaskStatus.DOING);
        History history = new History();
        history.setTaskID(1L);
        history.setStatus(TaskStatus.DOING);
        history.setTag("status");
        history.setModification("Status changed to DOING");

        when(historyRepository.findByTaskID(1L)).thenReturn(List.of(history), List.of());
        when(taskRepository.findByTaskID(1L)).thenReturn(task);
        when(taskUsersRepository.findByTask(task)).thenReturn(List.of());
        when(commentRepository.findByTask(task)).thenReturn(List.of());
        when(taskRepository.save(task)).thenReturn(task);

        taskService.undo(1L);

        assertThatThrownBy(() -> taskService.undo(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No unapplied history found for taskId: 1");

        verify(taskRepository, times(1)).findByTaskID(1L);
    }

    @Test
    void undoUpdatesTaskWithFreshOperationTimestamp() {
        TaskServiceImplementation taskService = taskService();
        doReturn(LocalDateTime.of(2026, 1, 1, 10, 0, 0))
                .when(taskService)
                .currentTime();
        Task task = new Task();
        task.setStatus(TaskStatus.DOING);
        History history = new History();
        history.setTaskID(1L);
        history.setStatus(TaskStatus.DOING);
        history.setTag("status");
        history.setModification("Status changed to DOING");

        when(historyRepository.findByTaskID(1L)).thenReturn(List.of(history));
        when(taskRepository.findByTaskID(1L)).thenReturn(task);
        when(taskUsersRepository.findByTask(task)).thenReturn(List.of());
        when(commentRepository.findByTask(task)).thenReturn(List.of());
        when(taskRepository.save(task)).thenReturn(task);

        taskService.undo(1L);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getUpdateTime()).isEqualTo("01-01-2026 10:00:00");
        assertThat(history.isUndone()).isTrue();
        verify(historyRepository).save(history);
    }

    @Test
    void undoWithoutHistoryDoesNotMutateTaskOrHistory() {
        TaskServiceImplementation taskService = taskService();
        when(historyRepository.findByTaskID(99L)).thenReturn(List.of());

        assertThatThrownBy(() -> taskService.undo(99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No unapplied history found for taskId: 99");

        verify(taskRepository, never()).findByTaskID(99L);
        verify(taskRepository, never()).save(any(Task.class));
        verify(historyRepository, never()).save(any(History.class));
    }

    private TaskServiceImplementation taskService() {
        return spy(new TaskServiceImplementation(
                taskRepository,
                commentRepository,
                taskUsersRepository,
                historyRepository,
                userRepository));
    }
}
