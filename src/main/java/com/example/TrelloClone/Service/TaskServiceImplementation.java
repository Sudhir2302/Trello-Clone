package com.example.TrelloClone.Service;

import com.example.TrelloClone.Models.Entity.*;
import com.example.TrelloClone.Models.Task.*;
import com.example.TrelloClone.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

@Service
@RequiredArgsConstructor
public class TaskServiceImplementation implements TaskServiceInterface {
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final TaskUsersRepository taskUsersRepository;
    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    LocalDateTime dateTime = LocalDateTime.now();
    DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    String  formattedDate = dateTime.format(dateTimeFormat);
    History history;

    @Override
    public TaskResponse saveTask(Task task) {
        task.setCreationTime(formattedDate);
        task.setStatus(TaskStatus.TODO);
        task.setEstimatedTime("1 Week");
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setTask(taskRepository.save(task));
        taskResponse.setUserDetails(taskUsersRepository.findByTask(task).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));
        taskResponse.setComments(commentRepository.findByTask(task).stream().map(Comment::getComment).collect(Collectors.toList()));
        taskRepository.save(task);
        return taskResponse;
    }

    @Override
    public TaskResponse getTask(long taskID) {
        Task task = taskRepository.findByTaskID(taskID);
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setTask(task);
        taskResponse.setUserDetails(taskUsersRepository.findByTask(task).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));
        taskResponse.setComments(commentRepository.findByTask(task).stream().map(Comment::getComment).collect(Collectors.toList()));
        return taskResponse;
    }

    @Override
    public List<TaskResponse> getAllTask() {
        List<TaskResponse> taskResponseList = new ArrayList<>();
        List<Task> tasks = taskRepository.findByOrderByStatusAsc();
        for (Task task: tasks){
            TaskResponse taskResponse = new TaskResponse();
            taskResponse.setTask(task);
            taskResponse.setComments(commentRepository.findByTask(task).stream().map(Comment::getComment).collect(Collectors.toList()));
            taskResponse.setUserDetails(taskUsersRepository.findByTask(task).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    @Override
    public TaskResponse addComment(AddComment addComment) {
        Comment comment = new Comment();
        TaskResponse taskResponse = new TaskResponse();
        comment.setComment(addComment.getComment());
        comment.setTask(taskRepository.findByTaskID(addComment.getTaskID()));
        comment.setUserDetails(userRepository.findByUserID(addComment.getUserID()));
        commentRepository.save(comment);
        Task task = taskRepository.findByTaskID(addComment.getTaskID());
        task.setUpdateTime(formattedDate);
        taskResponse.setTask(taskRepository.save(task));
        taskResponse.setComments(commentRepository.findByTask(task).stream().map(Comment::getComment).collect(Collectors.toList()));
        taskResponse.setUserDetails(taskUsersRepository.findByTask(task).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));
        String modification = addComment.getComment();
//        taskRepo.save(task);
        saveToHistory(task,modification,formattedDate,"comment");
        return taskResponse;
    }

    @Override
    public TaskResponse addUsers(AddUser addUser) {
        TaskResponse taskResponse = new TaskResponse();
        Task task = taskRepository.findByTaskID(addUser.getTaskID());
        UserDetails userDetails = userRepository.findByUserID(addUser.getUserID());
        TaskUsers taskUsers = new TaskUsers();
        taskUsers.setTask(task);
        taskUsers.setUserDetails(userDetails);
        taskUsersRepository.save(taskUsers);

        // Check if the task status is "TODO" and change it to "DOING"
        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.DOING);
        }

        // Update the task and save it
        task.setUpdateTime(formattedDate);
        taskResponse.setTask(taskRepository.save(task));
        taskResponse.setComments(commentRepository.findByTask(task).stream().map(Comment::getComment).collect(Collectors.toList()));
        taskResponse.setUserDetails(taskUsersRepository.findByTask(task).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));

        // Record the status change in the task history
        String modification = "Status changed to DOING";
        saveToHistory(task, modification, formattedDate, "status");

        return taskResponse;
    }

    @Override
    public TaskResponse modifyTask(ModifyTask modifyTask) {
        TaskResponse taskResponse = new TaskResponse();
        Task taskFromRepo = taskRepository.findByTaskID(modifyTask.getTaskID());

        // Handle state transitions using the Status enum
        if (modifyTask.getStatus() != null) {
            if (modifyTask.getStatus() == TaskStatus.DOING && taskFromRepo.getStatus() == TaskStatus.TODO) {
                taskFromRepo.setStatus(TaskStatus.DOING);
            } else if (modifyTask.getStatus() == TaskStatus.DONE && taskFromRepo.getStatus() == TaskStatus.DOING) {
                taskFromRepo.setStatus(TaskStatus.DONE);
            } else if (modifyTask.getStatus() == TaskStatus.TODO && taskFromRepo.getStatus() == TaskStatus.DONE) {
                taskFromRepo.setStatus(TaskStatus.TODO);
            }
        }

        if (modifyTask.getComment() != null) {
            Comment comment = new Comment();
            comment.setComment(modifyTask.getComment());
            comment.setTask(taskFromRepo);
            comment.setUserDetails(userRepository.findByUserID(modifyTask.getUserID()));
            commentRepository.save(comment);
        }

        if (modifyTask.getUserID() != 0) {
            TaskUsers taskUsers = new TaskUsers();
            taskUsers.setTask(taskFromRepo);
            taskUsers.setUserDetails(userRepository.findByUserID(modifyTask.getUserID()));
            taskUsersRepository.save(taskUsers);
        }

        if (modifyTask.getDescription() != null) {
            taskFromRepo.setDescription(modifyTask.getDescription());
        }

        if (modifyTask.getName() != null) {
            taskFromRepo.setName(modifyTask.getName());
        }

        taskFromRepo.setUpdateTime(formattedDate);
        taskRepository.save(taskFromRepo);

        // You can save task history and return it if needed

        taskResponse.setTask(taskFromRepo);
        taskResponse.setComments(commentRepository.findByTask(taskFromRepo).stream().map(Comment::getComment).collect(Collectors.toList()));
        taskResponse.setUserDetails(taskUsersRepository.findByTask(taskFromRepo).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));

        return taskResponse;
    }

    private void saveToHistory(Task task, String modification, String value, String tagg) {
        History history1 = new History();
        history1.setTaskID(task.getTaskID());
        history1.setName(task.getName());
        history1.setEstimatedTime(task.getEstimatedTime());
        history1.setStatus(task.getStatus());
        history1.setDescription(task.getDescription());
        history1.setCreationTime(task.getCreationTime());
        history1.setUpdateTime(task.getUpdateTime());
        history1.setModification(modification);
        history1.setUpdateTime(value);
        history1.setTag(tagg);
        history1.setUndone(false);
        historyRepository.save(history1);
    }

    @Override
    public List<Comment> getComments(long taskID) {
        Task task = taskRepository.findByTaskID(taskID);
        return commentRepository.findByTask(task);
    }

    @Override
    public List<TaskUsers> getTaskUsers(long taskID) {
        Task task = taskRepository.findByTaskID(taskID);
        return taskUsersRepository.findByTask(task);
    }

    @Override
    public List<History> getHistory(long taskID) {
        return historyRepository.findByTaskID(taskID);
    }

    @Override
    public void deleteTaskByID(long taskID) {
        taskRepository.deleteById(taskID);

    }

    @Override
    public TaskResponse undo(long taskID) {
        List <History> taskFromHistory = historyRepository.findByTaskID(taskID);
        for (int x = taskFromHistory.size() -1; x>=0; x--){
            if(!taskFromHistory.get(x).isUndone()){
                history = taskFromHistory.get(x);
                break;
            }
        }

        TaskResponse taskResponse = new TaskResponse();
        Task undoTask = taskRepository.findByTaskID(history.getTaskID());

        List<TaskUsers> taskUsers = taskUsersRepository.findByTask(undoTask);
        List<Comment> taskComment = commentRepository.findByTask(undoTask);
        String last = history.getTag();


        if (last.equals("taskName")) {
            undoTask.setName(history.getModification());
        }
        if (last.equals("status")) {
            undoTask.setStatus(undoTask.getStatus().undo());
        }
        if (last.equals("des")) {
            undoTask.setDescription(history.getModification());
        }
        if (last.equals("user")) {
            for (int i = 0; i < taskUsers.size(); i++) {
                if (taskUsers.get(i).getUserDetails().getUserID() == parseLong(history.getModification())) {
                    long taskUserID = taskUsers.get(i).getUserTaskID();
                    taskUsersRepository.deleteById(taskUserID);
                }
            }
        }
        if (last.equals("comment")) {
            for (int i = 0; i < taskComment.size(); i++) {
                if (taskComment.get(i).getComment().equals(history.getModification())) {
                    long commentID = taskComment.get(i).getCommentID();
                    commentRepository.deleteById(commentID);
                }
            }
        }
        undoTask.setUpdateTime(formattedDate);
        taskRepository.save(undoTask);
        taskResponse.setTask(undoTask);
        history.setUndone(true);
        historyRepository.save(history);
        taskResponse.setComments(commentRepository.findByTask(undoTask).stream().map(Comment::getComment).collect(Collectors.toList()));
        taskResponse.setUserDetails(taskUsersRepository.findByTask(undoTask).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));

        return taskResponse;
    }

    @Override
    public List<TaskResponse> getTasksByUser(UserDetails user) {
        List<TaskUsers> taskUsers = taskUsersRepository.findByUserDetails(user);
        List<TaskResponse> userTasks = new ArrayList<>();

        for (TaskUsers taskUser : taskUsers) {
            Task task = taskUser.getTask();
            TaskResponse taskResponse = new TaskResponse();
            taskResponse.setTask(task);
            taskResponse.setComments(commentRepository.findByTask(task).stream().map(Comment::getComment).collect(Collectors.toList()));
            taskResponse.setUserDetails(taskUsersRepository.findByTask(task).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()));
            userTasks.add(taskResponse);
        }

        return userTasks;
    }

}
