package com.taskmanager.service;

import com.taskmanager.dto.CommentRequest;
import com.taskmanager.dto.CommentResponse;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Comment;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.CommentRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentResponse addComment(UUID taskId, CommentRequest request) {
        User author = getCurrentUser();
        Task task = findOwnedTask(taskId, author);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .author(author)
                .build();

        commentRepository.save(comment);
        return toResponse(comment);
    }

    public Page<CommentResponse> getComments(UUID taskId, Pageable pageable) {
        User user = getCurrentUser();
        findOwnedTask(taskId, user); // verifies access
        return commentRepository.findByTaskIdAndDeletedAtIsNull(taskId, pageable)
                .map(this::toResponse);
    }

    public void deleteComment(UUID commentId) {
        User user = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Comment not found: " + commentId);
        }

        comment.setDeletedAt(Instant.now());
        commentRepository.save(comment);
    }

    /** Verifies the task exists and belongs to this user before allowing comments. */
    private Task findOwnedTask(UUID taskId, User owner) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (!task.getOwner().getId().equals(owner.getId())) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getTask().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getCreatedAt()
        );
    }
}