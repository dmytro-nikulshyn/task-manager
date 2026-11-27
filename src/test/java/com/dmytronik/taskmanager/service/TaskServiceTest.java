package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.TaskAssigneeRequestDTO;
import com.dmytronik.taskmanager.dto.TaskCreateRequestDTO;
import com.dmytronik.taskmanager.dto.TaskResponseDTO;
import com.dmytronik.taskmanager.dto.TaskUpdateRequestDTO;
import com.dmytronik.taskmanager.exception.ForbiddenOperationException;
import com.dmytronik.taskmanager.exception.IllegalTaskStatusTransitionException;
import com.dmytronik.taskmanager.exception.InvalidTaskStatusException;
import com.dmytronik.taskmanager.exception.NotProjectMemberException;
import com.dmytronik.taskmanager.exception.ProjectNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.TaskAlreadyCancelledException;
import com.dmytronik.taskmanager.exception.TaskNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.model.TaskStatus;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.TaskRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    private static final Long TASK_ID = 1L;
    private static final Long CURRENT_USER_ID = 2L;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskAccessChecker taskAccessChecker;
    @Mock
    private ProjectAccessChecker projectAccessChecker;
    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_shouldReturnEmptyList_whenNoTasksExist() {
        when(taskRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))).thenReturn(List.of());

        List<TaskResponseDTO> result = taskService.getAllTasks();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllTasks_shouldReturnListOfDTOs_whenTasksExist() {
        Task task = newTask();
        task.setTitle("Task 1");

        when(taskRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))).thenReturn(List.of(task));

        List<TaskResponseDTO> result = taskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void getTaskById_shouldReturnDTO_whenTaskExists() {
        Task task = newTask();
        task.setTitle("Task 1");

        when(taskAccessChecker.getTaskWithViewAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.getTaskById(TASK_ID, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getId()).isEqualTo(TASK_ID);
        assertThat(taskResponseDTO.getTitle()).isEqualTo("Task 1");
    }

    @Test
    void getTaskById_shouldThrowTaskNotFoundOrAccess_whenTaskNotFoundOrNotVisible() {
        when(taskAccessChecker.getTaskWithViewAccess(TASK_ID, CURRENT_USER_ID))
                .thenThrow(new TaskNotFoundOrAccessException(TASK_ID));

        assertThatThrownBy(() -> taskService.getTaskById(TASK_ID, CURRENT_USER_ID))
                .isInstanceOf(TaskNotFoundOrAccessException.class);
    }

    @Test
    void getTasksByProject_shouldReturnListOfDTOs_whenUserCanViewProject() {
        Long projectId = 3L;

        Task task = newTask();

        when(projectAccessChecker.getProjectWithViewAccess(projectId, CURRENT_USER_ID)).thenReturn(new Project());
        when(taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId)).thenReturn(List.of(task));

        List<TaskResponseDTO> taskResponseDTOs = taskService.getTasksByProject(projectId, CURRENT_USER_ID);

        assertThat(taskResponseDTOs).hasSize(1);
        assertThat(taskResponseDTOs.get(0).getId()).isEqualTo(TASK_ID);
    }

    @Test
    void getTasksByProject_shouldThrowProjectNotFoundOrAccess_whenProjectNotFoundOrNotVisible() {
        Long projectId = 3L;

        when(projectAccessChecker.getProjectWithViewAccess(projectId, CURRENT_USER_ID))
                .thenThrow(new ProjectNotFoundOrAccessException(projectId));

        assertThatThrownBy(() -> taskService.getTasksByProject(projectId, CURRENT_USER_ID))
                .isInstanceOf(ProjectNotFoundOrAccessException.class);
    }

    @Test
    void createTask_shouldReturnDTO_whenTaskCreated() {
        Long projectId = 3L;

        TaskCreateRequestDTO taskCreateRequestDTO = new TaskCreateRequestDTO();
        taskCreateRequestDTO.setTitle("Task 1");
        taskCreateRequestDTO.setDescription("Task 1 description");
        taskCreateRequestDTO.setProjectId(projectId);

        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        currentUser.setUsername("dmytronik");

        Project project = new Project();
        project.setId(projectId);

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(currentUser));
        when(taskAccessChecker.getProjectForTaskCreation(projectId, CURRENT_USER_ID)).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(TASK_ID);
            return savedTask;
        });

        TaskResponseDTO taskResponseDTO = taskService.createTask(taskCreateRequestDTO, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getId()).isEqualTo(TASK_ID);
        assertThat(taskResponseDTO.getTitle()).isEqualTo("Task 1");
        assertThat(taskResponseDTO.getDescription()).isEqualTo("Task 1 description");
        assertThat(taskResponseDTO.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskResponseDTO.getReporter().getId()).isEqualTo(CURRENT_USER_ID);
    }

    @Test
    void createTask_shouldThrowUserNotFound_whenUserNotFound() {
        TaskCreateRequestDTO taskCreateRequestDTO = new TaskCreateRequestDTO();

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(taskCreateRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateTask_shouldReturnDTO_whenTaskUpdated() {
        TaskUpdateRequestDTO taskUpdateRequestDTO = new TaskUpdateRequestDTO();
        taskUpdateRequestDTO.setTitle("Updated title");
        taskUpdateRequestDTO.setDescription("Updated description");
        taskUpdateRequestDTO.setStatus(TaskStatus.IN_PROGRESS);

        Task task = newTask();
        task.setTitle("Task 1");
        task.setDescription("Task 1");
        task.setStatus(TaskStatus.TODO);

        when(taskAccessChecker.getTaskWithEditAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.updateTask(TASK_ID, taskUpdateRequestDTO, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getId()).isEqualTo(TASK_ID);
        assertThat(taskResponseDTO.getTitle()).isEqualTo("Updated title");
        assertThat(taskResponseDTO.getDescription()).isEqualTo("Updated description");
        assertThat(taskResponseDTO.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateTask_shouldThrowForbidden_whenUserCannotEditTask() {
        when(taskAccessChecker.getTaskWithEditAccess(TASK_ID, CURRENT_USER_ID))
                .thenThrow(new ForbiddenOperationException("Edit access denied"));

        assertThatThrownBy(() -> taskService.updateTask(TASK_ID, new TaskUpdateRequestDTO(), CURRENT_USER_ID))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void updateTask_shouldThrowTaskNotFoundOrAccess_whenTaskNotFoundOrNotVisible() {
        when(taskAccessChecker.getTaskWithEditAccess(TASK_ID, CURRENT_USER_ID))
                .thenThrow(new TaskNotFoundOrAccessException(TASK_ID));

        assertThatThrownBy(() -> taskService.updateTask(TASK_ID, new TaskUpdateRequestDTO(), CURRENT_USER_ID))
                .isInstanceOf(TaskNotFoundOrAccessException.class);
    }

    @Test
    void updateTask_shouldThrowInvalidTaskStatus_whenRequestedStatusIsCancelled() {
        TaskUpdateRequestDTO taskUpdateRequestDTO = new TaskUpdateRequestDTO();
        taskUpdateRequestDTO.setStatus(TaskStatus.CANCELLED);

        Task task = newTask();
        task.setStatus(TaskStatus.TODO);

        when(taskAccessChecker.getTaskWithEditAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        assertThatThrownBy(() -> taskService.updateTask(TASK_ID, taskUpdateRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(InvalidTaskStatusException.class);
    }

    @Test
    void updateTask_shouldThrowTaskAlreadyCancelled_whenTaskAlreadyCancelled() {
        TaskUpdateRequestDTO taskUpdateRequestDTO = new TaskUpdateRequestDTO();
        taskUpdateRequestDTO.setStatus(TaskStatus.IN_PROGRESS);

        Task task = newTask();
        task.setStatus(TaskStatus.CANCELLED);

        when(taskAccessChecker.getTaskWithEditAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        assertThatThrownBy(() -> taskService.updateTask(TASK_ID, taskUpdateRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(TaskAlreadyCancelledException.class);
    }

    @Test
    void updateTask_shouldThrowInvalidTaskStatus_whenBothRequestedStatusIsCancelledAndTaskAlreadyCancelled() {
        TaskUpdateRequestDTO taskUpdateRequestDTO = new TaskUpdateRequestDTO();
        taskUpdateRequestDTO.setStatus(TaskStatus.CANCELLED);

        Task task = newTask();
        task.setStatus(TaskStatus.CANCELLED);

        when(taskAccessChecker.getTaskWithEditAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        assertThatThrownBy(() -> taskService.updateTask(TASK_ID, taskUpdateRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(InvalidTaskStatusException.class);
    }

    @Test
    void assignTask_shouldReturnDTO_whenTaskAssigned() {
        Long newAssigneeId = 3L;

        TaskAssigneeRequestDTO taskAssigneeRequestDTO = new TaskAssigneeRequestDTO();
        taskAssigneeRequestDTO.setAssigneeId(newAssigneeId);

        Project project = new Project();
        User member = new User();
        member.setId(newAssigneeId);
        project.getMembers().add(member);

        Task task = newTask();
        task.setProject(project);

        when(taskAccessChecker.getTaskWithAssignAccess(TASK_ID, CURRENT_USER_ID, newAssigneeId)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.assignTask(TASK_ID, taskAssigneeRequestDTO, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getAssignee().getId()).isEqualTo(newAssigneeId);
        assertThat(taskResponseDTO.getId()).isEqualTo(TASK_ID);
    }

    @Test
    void assignTask_shouldReturnDTO_whenTaskUnassigned() {
        Long newAssigneeId = null;

        TaskAssigneeRequestDTO taskAssigneeRequestDTO = new TaskAssigneeRequestDTO();
        taskAssigneeRequestDTO.setAssigneeId(newAssigneeId);

        User currentAssignee = new User();

        Task task = newTask();
        task.setAssignee(currentAssignee);

        when(taskAccessChecker.getTaskWithAssignAccess(TASK_ID, CURRENT_USER_ID, newAssigneeId)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.assignTask(TASK_ID, taskAssigneeRequestDTO, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getAssignee()).isNull();
        assertThat(taskResponseDTO.getId()).isEqualTo(TASK_ID);
    }

    @Test
    void assignTask_shouldThrowNotProjectMember_whenNewAssigneeNotMember() {
        Long newAssigneeId = 3L;

        TaskAssigneeRequestDTO taskAssigneeRequestDTO = new TaskAssigneeRequestDTO();
        taskAssigneeRequestDTO.setAssigneeId(newAssigneeId);

        Project project = new Project();

        Task task = newTask();
        task.setProject(project);

        when(taskAccessChecker.getTaskWithAssignAccess(TASK_ID, CURRENT_USER_ID, newAssigneeId)).thenReturn(task);

        assertThatThrownBy(() -> taskService.assignTask(TASK_ID, taskAssigneeRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(NotProjectMemberException.class);
    }

    @Test
    void assignTask_shouldThrowTaskAlreadyCancelled_whenTaskAlreadyCancelled() {
        Long newAssigneeId = 3L;

        TaskAssigneeRequestDTO taskAssigneeRequestDTO = new TaskAssigneeRequestDTO();
        taskAssigneeRequestDTO.setAssigneeId(newAssigneeId);

        Project project = new Project();
        User member = new User();
        member.setId(newAssigneeId);
        project.getMembers().add(member);

        Task task = newTask();
        task.setProject(project);
        task.setStatus(TaskStatus.CANCELLED);

        when(taskAccessChecker.getTaskWithAssignAccess(TASK_ID, CURRENT_USER_ID, newAssigneeId)).thenReturn(task);

        assertThatThrownBy(() -> taskService.assignTask(TASK_ID, taskAssigneeRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(TaskAlreadyCancelledException.class);
    }

    @Test
    void assignTask_shouldThrowNotProjectMember_whenBothNewAssigneeNotMemberAndTaskAlreadyCancelled() {
        Long newAssigneeId = 3L;

        TaskAssigneeRequestDTO taskAssigneeRequestDTO = new TaskAssigneeRequestDTO();
        taskAssigneeRequestDTO.setAssigneeId(newAssigneeId);

        Project project = new Project();

        Task task = newTask();
        task.setProject(project);
        task.setStatus(TaskStatus.CANCELLED);

        when(taskAccessChecker.getTaskWithAssignAccess(TASK_ID, CURRENT_USER_ID, newAssigneeId)).thenReturn(task);

        assertThatThrownBy(() -> taskService.assignTask(TASK_ID, taskAssigneeRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(NotProjectMemberException.class);
    }

    @Test
    void assignTask_shouldThrowForbidden_whenUserCannotAssignTask() {
        Long newAssigneeId = 3L;

        TaskAssigneeRequestDTO taskAssigneeRequestDTO = new TaskAssigneeRequestDTO();
        taskAssigneeRequestDTO.setAssigneeId(newAssigneeId);

        when(taskAccessChecker.getTaskWithAssignAccess(TASK_ID, CURRENT_USER_ID, newAssigneeId))
                .thenThrow(new ForbiddenOperationException("Assign access denied"));

        assertThatThrownBy(() -> taskService.assignTask(TASK_ID, taskAssigneeRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void cancelTask_shouldReturnDTO_whenTaskCancelled() {
        User assignee = new User();
        assignee.setId(3L);

        Task task = newTask();
        task.setStatus(TaskStatus.TODO);
        task.setAssignee(assignee);

        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.cancelTask(TASK_ID, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getStatus()).isEqualTo(TaskStatus.CANCELLED);
        assertThat(taskResponseDTO.getId()).isEqualTo(TASK_ID);
        assertThat(taskResponseDTO.getAssignee().getId()).isEqualTo(assignee.getId());
    }

    @Test
    void cancelTask_shouldThrowTaskAlreadyCancelled_whenTaskAlreadyCancelled() {
        Task task = newTask();
        task.setStatus(TaskStatus.CANCELLED);

        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        assertThatThrownBy(() -> taskService.cancelTask(TASK_ID, CURRENT_USER_ID))
                .isInstanceOf(TaskAlreadyCancelledException.class);
    }

    @Test
    void cancelTask_shouldThrowIllegalTaskStatusTransition_whenTaskDone() {
        Task task = newTask();
        task.setStatus(TaskStatus.DONE);

        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        assertThatThrownBy(() -> taskService.cancelTask(TASK_ID, CURRENT_USER_ID))
                .isInstanceOf(IllegalTaskStatusTransitionException.class);
    }

    @Test
    void cancelTask_shouldThrowForbidden_whenUserCannotCancelTask() {
        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID))
                .thenThrow(new ForbiddenOperationException("Cancellation access denied"));

        assertThatThrownBy(() -> taskService.cancelTask(TASK_ID, CURRENT_USER_ID))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void reopenTask_shouldKeepAssignee_whenAssigneeStillMember() {
        Long assigneeId = 3L;

        User assignee = new User();
        assignee.setId(assigneeId);

        Project project = new Project();
        project.getMembers().add(assignee);

        Task task = newTask();
        task.setStatus(TaskStatus.CANCELLED);
        task.setAssignee(assignee);
        task.setProject(project);

        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.reopenTask(TASK_ID, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskResponseDTO.getAssignee().getId()).isEqualTo(assigneeId);
    }

    @Test
    void reopenTask_shouldUnassign_whenAssigneeNotMember() {
        Long assigneeId = 3L;

        User assignee = new User();
        assignee.setId(assigneeId);

        Project project = new Project();

        Task task = newTask();
        task.setStatus(TaskStatus.CANCELLED);
        task.setAssignee(assignee);
        task.setProject(project);

        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.reopenTask(TASK_ID, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskResponseDTO.getAssignee()).isNull();
    }

    @Test
    void reopenTask_shouldKeepUnassigned_whenHasNoAssignee() {
        Task task = newTask();
        task.setStatus(TaskStatus.CANCELLED);

        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponseDTO taskResponseDTO = taskService.reopenTask(TASK_ID, CURRENT_USER_ID);

        assertThat(taskResponseDTO.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskResponseDTO.getAssignee()).isNull();
    }

    @Test
    void reopenTask_shouldThrowIllegalTaskStatusTransition_whenTaskNotCancelled() {
        Task task = newTask();
        task.setStatus(TaskStatus.TODO);

        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        assertThatThrownBy(() -> taskService.reopenTask(TASK_ID, CURRENT_USER_ID))
                .isInstanceOf(IllegalTaskStatusTransitionException.class);
    }

    @Test
    void reopenTask_shouldThrowForbidden_whenUserCannotReopenTask() {
        when(taskAccessChecker.getTaskWithCancellationAccess(TASK_ID, CURRENT_USER_ID))
                .thenThrow(new ForbiddenOperationException("Reopening access denied"));

        assertThatThrownBy(() -> taskService.reopenTask(TASK_ID, CURRENT_USER_ID))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deleteTask_shouldDeleteTask_whenUserHasDeleteAccess() {
        Task task = newTask();

        when(taskAccessChecker.getTaskWithDeleteAccess(TASK_ID, CURRENT_USER_ID)).thenReturn(task);

        taskService.deleteTask(TASK_ID, CURRENT_USER_ID);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_shouldThrowForbidden_whenUserCannotDelete() {
        when(taskAccessChecker.getTaskWithDeleteAccess(TASK_ID, CURRENT_USER_ID))
                .thenThrow(new ForbiddenOperationException("Delete access denied"));

        assertThatThrownBy(() -> taskService.deleteTask(TASK_ID, CURRENT_USER_ID))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private Task newTask() {
        Task task = new Task();
        task.setId(TASK_ID);
        // reporter is required by convertToDTO
        task.setReporter(new User());
        return task;
    }
}
