package com.taskmanager.dto;
import com.taskmanager.model.TaskPriority;
import com.taskmanager.model.TaskStatus;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TaskRequest {
    @NotBlank
    @Size(max=200)
    private String title;

    @Size(max =2000)
    private String description;

    @NotNull
    private TaskStatus status;

    @NotNull
    private TaskPriority priority;

     @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @NotNull
    private UUID projectId;
}


    

