package com.nova.everisdarmytasksms;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.everisdarmytasksms.controller.TaskController;
import com.nova.everisdarmytasksms.model.Task;
import com.nova.everisdarmytasksms.model.Status;
import com.nova.everisdarmytasksms.repository.TaskRepository;
import com.nova.everisdarmytasksms.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@Disabled
class EverisDarMytasksMsApplicationTests {

	@MockBean
	TaskRepository taskRepository;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	TaskController taskController;
	
	@Autowired
	MockMvc mockMvc;
	
	//Delete all tasks
	@After
	void emptyDB() {
		taskRepository.deleteAll();
	}
	
	@Test
	void createTaskFromServiceTest() {
		//Description with 256 characters
		Task task256Characters = build256CharactersTask();
		//Description with 255 characters
		Task task255Characters = build255CharactersTask();
		
		assertEquals(task256Characters, null);
		assertNotNull(task255Characters);
	}
	
	@Test
	void getTasksTest() throws Exception {
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(buildPendingTask());
		tasks.add(buildInProgressTask());
		tasks.add(buildFinishedTask());
		
		when(taskRepository.findAll()).thenReturn(tasks);
		this.mockMvc.perform(get("/tasks")).
		andDo(print()).andExpect(status().isOk()).
		andExpect(jsonPath("$.length()",is(3))).
		andExpect(jsonPath("$.[0].status").value(Status.PENDING.toString())).
		andExpect(jsonPath("$.[1].status").value(Status.IN_PROGRESS.toString())).
		andExpect(jsonPath("$.[2].status").value(Status.FINISHED.toString()));
	} 
	
	@Test
	void createTaskWithLessThan256Characters() throws Exception {
		Task task = buildPendingTask();
		
		this.mockMvc.perform(post("/tasks").
		param("status", task.getStatus().toString()).param("description", task.getDescription())).
		andDo(print()).andExpect(status().isCreated()).
		andExpect(content().string("Tarea creada correctamente"));
	}
	
	@Test
	void createTaskWith256Characters() throws Exception {		
		this.mockMvc.perform(post("/tasks").
		param("status", Status.PENDING.toString()).param("description", descriptionOf256Characters())).
		andDo(print()).andExpect(status().isUnprocessableEntity()).
		andExpect(content().string("La tarea debe tener una descripción "
				+ "con una longitud máxima de 255 caracteres"));
	}
	
	@Test
	void getTasksByStatusPendingTest() throws Exception {
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(buildPendingTask());
		
		when(taskRepository.findAllByStatus(Status.PENDING)).thenReturn(tasks);
		this.mockMvc.perform(get("/tasks/status/" + Status.PENDING)).
		andDo(print()).andExpect(status().isOk()).
		andExpect(jsonPath("$.length()",is(1))).
		andExpect(jsonPath("$.[0].status").value(Status.PENDING.toString()));
	}
	
	@Test
	void getTasksByStatusInProgressTest() throws Exception {
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(buildInProgressTask());
		
		when(taskRepository.findAllByStatus(Status.IN_PROGRESS)).thenReturn(tasks);
		this.mockMvc.perform(get("/tasks/status/" + Status.IN_PROGRESS)).
		andDo(print()).andExpect(status().isOk()).
		andExpect(jsonPath("$.length()",is(1))).
		andExpect(jsonPath("$.[0].status").value(Status.IN_PROGRESS.toString()));
	}
	
	@Test
	void getTasksByStatusFinishedTest() throws Exception {
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(buildFinishedTask());
		
		when(taskRepository.findAllByStatus(Status.FINISHED)).thenReturn(tasks);
		this.mockMvc.perform(get("/tasks/status/" + Status.FINISHED)).
		andDo(print()).andExpect(status().isOk()).
		andExpect(jsonPath("$.length()",is(1))).
		andExpect(jsonPath("$.[0].status").value(Status.FINISHED.toString()));
	}
	
	@Test
	void updateTaskTest() throws Exception {
		Task task = buildPendingTask();
		task.setId(1);
		Optional<Task> optional = Optional.of(task);
		
		Task updatedTask = buildUpdatedTask();
		ObjectMapper map = new ObjectMapper();
		String jsonString = map.writeValueAsString(updatedTask);
		
		Mockito.when(taskRepository.findById(1)).thenReturn(optional);
		this.mockMvc.perform(put("/tasks/1").contentType(MediaType.APPLICATION_JSON)
				.content(jsonString)).
		andDo(print()).andExpect(status().isOk()).
		andExpect(content().json("{\"id\": 1,\"status\":\"" + updatedTask.getStatus() + "\"," + 
				"\"description\":\"" + updatedTask.getDescription() + "\"}"));
	}

	@Test
	void updateDescriptionTaskTest() throws Exception {
		Task task = buildPendingTask();
		task.setId(1);
		Optional<Task> optional = Optional.of(task);
		
		task.setDescription("Descripcion actualizada");;
		
		Mockito.when(taskRepository.findById(1)).thenReturn(optional);
		this.mockMvc.perform(put("/tasks/description/1").param("description", task.getDescription())).
		andDo(print()).andExpect(status().isOk()).
		andExpect(content().json("{\"id\": 1,\"status\":\"" + task.getStatus() + "\"," + 
				"\"description\":\"" + task.getDescription() + "\"}"));
	}
	
	@Test
	void updateStatusTaskTest() throws Exception {
		Task task = buildPendingTask();
		task.setId(1);
		Optional<Task> optional = Optional.of(task);
		
		task.setStatus(Status.FINISHED);
		
		Mockito.when(taskRepository.findById(1)).thenReturn(optional);
		this.mockMvc.perform(put("/tasks/status/1").param("status", task.getStatus().toString())).
		andDo(print()).andExpect(status().isOk()).
		andExpect(content().json("{\"id\": 1,\"status\":\"" + task.getStatus() + "\"," + 
				"\"description\":\"" + task.getDescription() + "\"}"));
	}
	
	
	private Task buildUpdatedTask() {
		return taskService.createTask(Status.FINISHED, "Descripcion actualizada");
	}

	private Task buildPendingTask() {
		return taskService.createTask(Status.PENDING, "Descripcion PENDING");
	}
	
	private Task buildInProgressTask() {
		return taskService.createTask(Status.IN_PROGRESS, "Descripcion IN PROGRESS");
	}
	
	private Task buildFinishedTask() {
		return taskService.createTask(Status.FINISHED, "Descripcion FINISHED");
	}
	
	private Task build255CharactersTask() {
		return taskService.createTask(Status.PENDING, "sssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssss");
	}
	
	private Task build256CharactersTask() {
		return taskService.createTask(Status.PENDING, descriptionOf256Characters());
	}
	
	private String descriptionOf256Characters() {
		return "sssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss"
				+ "ssssssssssssssssssssssssssssssss";
	}

}
