package com.example.TrelloClone.Service;

import com.example.TrelloClone.Models.Entity.History;
import com.example.TrelloClone.Models.Entity.Task;
import com.example.TrelloClone.Models.Entity.UserDetails;
import com.example.TrelloClone.Models.Task.AddComment;
import com.example.TrelloClone.Models.Task.AddUser;
import com.example.TrelloClone.Models.Task.ModifyTask;
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
                .doReturn(LocalDateTime.of(2026, 1, 1, 10, 1, 0))
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

        assertThat(firstTask.getCreationTime()).isEqualTo("01-01-2026 10:00");
        assertThat(secondTask.getCreationTime()).isEqualTo("01-01-2026 10:01");
        verify(taskRepository, times(2)).save(any(Task.class));
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
        assertThat(taskCaptor.getValue().getUpdateTime()).isEqualTo("01-01-2026 10:00");
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
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

    @Test
    void addCommentUsesFreshTimestampForEachOperation() {
        TaskServiceImplementation taskService = taskService();
        doReturn(LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 1, 10, 1))
                .when(taskService).currentTime();
        Task task = task(1L, TaskStatus.TODO);
        task.setCreationTime("31-12-2025 09:00");
        when(taskRepository.findByTaskID(1L)).thenReturn(task);
        when(userRepository.findByUserID(7L)).thenReturn(new UserDetails());
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.findByTask(task)).thenReturn(List.of());
        when(taskUsersRepository.findByTask(task)).thenReturn(List.of());

        taskService.addComment(commentRequest(1L, 7L, "first"));
        taskService.addComment(commentRequest(1L, 7L, "second"));

        ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(History::getUpdateTime)
                .containsExactly("01-01-2026 10:00", "01-01-2026 10:01");
        assertThat(historyCaptor.getAllValues()).extracting(History::getModification)
                .containsExactly("first", "second");
        assertThat(task.getCreationTime()).isEqualTo("31-12-2025 09:00");
    }

    @Test
    void addUsersUsesFreshTimestampAndMovesTodoTaskToDoing() {
        TaskServiceImplementation taskService = taskService();
        doReturn(LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 1, 10, 1))
                .when(taskService).currentTime();
        Task first = task(1L, TaskStatus.TODO);
        Task second = task(2L, TaskStatus.TODO);
        when(taskRepository.findByTaskID(1L)).thenReturn(first);
        when(taskRepository.findByTaskID(2L)).thenReturn(second);
        when(userRepository.findByUserID(7L)).thenReturn(new UserDetails());
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.findByTask(any(Task.class))).thenReturn(List.of());
        when(taskUsersRepository.findByTask(any(Task.class))).thenReturn(List.of());

        taskService.addUsers(userRequest(1L, 7L));
        taskService.addUsers(userRequest(2L, 7L));

        assertThat(first.getStatus()).isEqualTo(TaskStatus.DOING);
        assertThat(second.getStatus()).isEqualTo(TaskStatus.DOING);
        ArgumentCaptor<History> historyCaptor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues()).extracting(History::getUpdateTime)
                .containsExactly("01-01-2026 10:00", "01-01-2026 10:01");
    }

    @Test
    void modifyTaskUsesFreshTimestampForEachOperation() {
        TaskServiceImplementation taskService = taskService();
        doReturn(LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 1, 10, 1))
                .when(taskService).currentTime();
        Task first = task(1L, TaskStatus.TODO);
        Task second = task(2L, TaskStatus.TODO);
        first.setCreationTime("31-12-2025 09:00");
        second.setCreationTime("31-12-2025 09:01");
        when(taskRepository.findByTaskID(1L)).thenReturn(first);
        when(taskRepository.findByTaskID(2L)).thenReturn(second);
        when(commentRepository.findByTask(any(Task.class))).thenReturn(List.of());
        when(taskUsersRepository.findByTask(any(Task.class))).thenReturn(List.of());

        taskService.modifyTask(modifyRequest(1L, "First update"));
        taskService.modifyTask(modifyRequest(2L, "Second update"));

        assertThat(first.getUpdateTime()).isEqualTo("01-01-2026 10:00");
        assertThat(second.getUpdateTime()).isEqualTo("01-01-2026 10:01");
        assertThat(first.getCreationTime()).isEqualTo("31-12-2025 09:00");
        assertThat(second.getCreationTime()).isEqualTo("31-12-2025 09:01");
    }

    @Test
    void undoWithOnlyUndoneHistoryDoesNotMutateTaskOrHistory() {
        TaskServiceImplementation taskService = taskService();
        History history = new History();
        history.setUndone(true);
        when(historyRepository.findByTaskID(99L)).thenReturn(List.of(history));

        assertThatThrownBy(() -> taskService.undo(99L))
                .isInstanceOf(NoUndoHistoryException.class)
                .hasMessage("No unapplied history found for taskId: 99");

        verify(taskRepository, never()).findByTaskID(99L);
        verify(taskRepository, never()).save(any(Task.class));
        verify(historyRepository, never()).save(any(History.class));
    }

    @Test
    void undoDoesNotReuseHistoryWhenSubsequentRequestTargetsDifferentTask() {
        TaskServiceImplementation taskService = taskService();
        Task firstTask = task(1L, TaskStatus.DOING);
        History firstHistory = statusHistory(1L);
        when(historyRepository.findByTaskID(1L)).thenReturn(List.of(firstHistory));
        when(historyRepository.findByTaskID(2L)).thenReturn(List.of());
        when(taskRepository.findByTaskID(1L)).thenReturn(firstTask);
        when(taskUsersRepository.findByTask(firstTask)).thenReturn(List.of());
        when(commentRepository.findByTask(firstTask)).thenReturn(List.of());
        when(taskRepository.save(firstTask)).thenReturn(firstTask);

        taskService.undo(1L);

        assertThatThrownBy(() -> taskService.undo(2L))
                .isInstanceOf(NoUndoHistoryException.class)
                .hasMessage("No unapplied history found for taskId: 2");

        verify(taskRepository, times(1)).findByTaskID(1L);
        verify(historyRepository, times(1)).save(firstHistory);
    }

    private Task task(long taskID, TaskStatus status) {
        Task task = new Task();
        task.setTaskID(taskID);
        task.setName("Task " + taskID);
        task.setStatus(status);
        return task;
    }

    private History statusHistory(long taskID) {
        History history = new History();
        history.setTaskID(taskID);
        history.setStatus(TaskStatus.DOING);
        history.setTag("status");
        history.setModification("Status changed to DOING");
        return history;
    }

    private AddComment commentRequest(long taskID, long userID, String comment) {
        AddComment request = new AddComment(taskID, comment);
        request.setUserID(userID);
        return request;
    }

    private AddUser userRequest(long taskID, long userID) {
        AddUser request = new AddUser();
        request.setTaskID(taskID);
        request.setUserID(userID);
        return request;
    }

    private ModifyTask modifyRequest(long taskID, String description) {
        ModifyTask request = new ModifyTask();
        request.setTaskID(taskID);
        request.setDescription(description);
        return request;
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
