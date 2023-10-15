package br.com.phfarolfi.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.phfarolfi.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity<String> create(@RequestBody TaskModel task, HttpServletRequest request) {
        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt()) || task.getEndAt().isBefore(task.getStartAt())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid timestamp.");
        }

        task.setIdUser((UUID) request.getAttribute("idUser"));

        this.taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping("/")
    public ResponseEntity<List<TaskModel>> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = taskRepository.findByIdUser((UUID) idUser);

        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody TaskModel task, HttpServletRequest request, @PathVariable UUID id) {
        var dbTask = this.taskRepository.findById(id).orElse(null);

        if (dbTask == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task not found.");
        }

        var idUser = request.getAttribute("idUser");

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User do not have permission to change this task.");
        }

        Utils.copyNonNullProperties(task, dbTask);

        this.taskRepository.save(dbTask);

        return ResponseEntity.status(HttpStatus.OK).body("Task changed successfully.");
    }
}
