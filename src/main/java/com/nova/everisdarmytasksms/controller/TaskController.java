package com.nova.everisdarmytasksms.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nova.everisdarmytasksms.model.Status;
import com.nova.everisdarmytasksms.model.Task;
import com.nova.everisdarmytasksms.repository.TaskRepository;
import com.nova.everisdarmytasksms.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class TaskController {

	@Autowired
	TaskRepository taskRepository;
	
	@Autowired
	TaskService taskService;
	
	private static final Logger logger =  LoggerFactory.getLogger(TaskController.class);
	
	@Operation(summary = "Get all tasks")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "Tasks found") 
	})
	@GetMapping("/tasks")
	public List<Task> getTasks() {
		return taskRepository.findAll();
	}

	@Operation(summary = "Get task by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Task found")
	})
	@GetMapping("/tasks/{id}")
	public Task getTask(@PathVariable("id") Integer id) {
		return taskService.getTaskById(id);
	}
	
	@Operation(summary = "Create a task")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "201", description = "Task created"),
	  @ApiResponse(responseCode = "422", description = "Task couldn't be created. It must contain a "
	  		+ "maximun of 255 characters")
	})
	@PostMapping("/tasks")
	public ResponseEntity<String> createTasks(@RequestParam("status") Status status, 
			@RequestParam("description") String description) {
		Task task = taskService.createTask(status, description);
		if (task != null) {
			logger.info("Se puede crear la tarea correctamente");
			taskRepository.save(task);
			return new ResponseEntity<>("Tarea creada correctamente", HttpStatus.CREATED);
		} else {
			logger.info("La tarea no ha podido ser creada");
			return new ResponseEntity<>("La tarea debe tener una descripción "
					+ "con una longitud máxima de 255 caracteres", 
					HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
	
	@Operation(summary = "Get task by its status")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "Tasks found")
	})
	@GetMapping("/tasks/status/{status}")
	public List<Task> getTasksByStatus(@PathVariable("status") Status status) {
		return taskRepository.findAllByStatus(status);
	}
	
	@Operation(summary = "Update an entire task")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "Task updated"),
	  @ApiResponse(responseCode = "400", description = "The task with the given id doesn't exists")
	})
	@PutMapping("/tasks/{id}")
	public ResponseEntity<Task> updateTask(@PathVariable("id") Integer id, 
			@RequestBody Task task) {
		Task existingTask = taskService.getTaskById(id);
		if (existingTask != null) {
			logger.info("La tarea con el id " + id + " ha sido actualizada");
			existingTask.setDescription(task.getDescription());
			existingTask.setStatus(task.getStatus());
			taskRepository.save(existingTask);
			
			return new ResponseEntity<Task>(existingTask, 
					HttpStatus.OK);
		} else {
			logger.info("La tarea con el id " + id + " no existe");
			return new ResponseEntity<Task>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Operation(summary = "Update the decription of one task")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "Task updated"),
	  @ApiResponse(responseCode = "400", description = "The task with the given id doesn't exists")
	})
	@PutMapping("/tasks/description/{id}")
	public ResponseEntity<Task> updateDescriptionTask(@PathVariable("id") Integer id, 
			@RequestParam("description") String description) {
		Task existingTask = taskService.getTaskById(id);
		if (existingTask != null) {
			logger.info("La descripcion de la tarea con el id " + id + " ha sido actualizada");
			existingTask.setDescription(description);
			taskRepository.save(existingTask);
			
			return new ResponseEntity<Task>(existingTask, 
					HttpStatus.OK);
		} else {
			logger.info("La tarea con el id " + id + " no existe");
			return new ResponseEntity<Task>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Operation(summary = "Update the status of one task")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "Task updated"),
	  @ApiResponse(responseCode = "400", description = "The task with the given id doesn't exists")
	})
	@PutMapping("/tasks/status/{id}")
	public ResponseEntity<Task> updateStatusTask(@PathVariable("id") Integer id, 
			@RequestParam("status") Status status) {
		Task existingTask = taskService.getTaskById(id);
		if (existingTask != null) {
			logger.info("El status de la tarea con el id " + id + " ha sido actualizada");
			existingTask.setStatus(status);
			taskRepository.save(existingTask);
			
			return new ResponseEntity<Task>(existingTask, 
					HttpStatus.OK);
		} else {
			logger.info("La tarea con el id " + id + " no existe");
			return new ResponseEntity<Task>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@Operation(summary = "Delete one task")
	@ApiResponses(value = { 
	  @ApiResponse(responseCode = "200", description = "Task deleted"),
	  @ApiResponse(responseCode = "400", description = "The task with the given id doesn't exists")
	})
	@DeleteMapping("/tasks/{id}")
	public ResponseEntity<String> deleteTask(@PathVariable("id") Integer id) {
		if (taskRepository.existsById(id)) {
			taskRepository.deleteById(id);
			logger.info("La tarea con el id " + id + " ha sido eliminada correctamente");
			return new ResponseEntity<String>("Tarea eliminada correctamente", 
					HttpStatus.OK);
		} else {
			logger.info("La tarea con el id " + id + " no existe");
			return new ResponseEntity<String>("La tarea con el id indicado no existe", HttpStatus.BAD_REQUEST);
		}
		
		
	}
}
