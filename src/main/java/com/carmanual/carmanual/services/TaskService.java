package com.carmanual.carmanual.services;

import jakarta.persistence.EntityNotFoundException;
import com.carmanual.carmanual.models.Task;
import com.carmanual.carmanual.repositories.TaskRepository;
import com.carmanual.carmanual.repositories.UserRepository;
import com.carmanual.carmanual.services.exception.DataBindingViolationException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.carmanual.carmanual.services.UserService;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<Task> findAllByUserId(Long userId) {
        userService.checkUserAccess(userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + userId));
        return taskRepository.findAllByUserId(userId);
    }

    public Task save(Task task) {
        if (task.getUser() == null || task.getUser().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario obrigatorio");
        }
        userService.checkUserAccess(task.getUser().getId());
        task.setUser(userRepository.findById(task.getUser().getId())
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + task.getUser().getId())));
        return taskRepository.save(task);
    }

    public Task update(Long id, Task task) {
        Task existingTask = findById(id);
        userService.checkUserAccess(existingTask.getUser().getId());
        task.setId(id);
        return save(task);
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada com ID: " + id));
    }

    public void delete(Long id) {
        Task task = findById(id);
        userService.checkUserAccess(task.getUser().getId());
        try {
            taskRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            throw new DataBindingViolationException(
                    "Não é possível excluir pois há entidades relacionadas", exception);
        }
    }
}
