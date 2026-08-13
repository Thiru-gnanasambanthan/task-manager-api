package com.taskmanager.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ProjectRequest {
    @NotBlank
    @Size(max = 200)
    private String name;
    @Size(max = 2000)
    private String description;
}
