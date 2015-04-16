package ch.zhaw.photoflow.core.domain;

import java.util.List;

public class Project {

	private String name;
	private String description;
	private ProjectStatus status;
	private List<Todo> todos;
	
	public Project() {
		
	}

	
	/************ GETTERS AND SETTERS ************/
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

	public List<Todo> getTodos() {
		return todos;
	}

	public void setTodos(List<Todo> todos) {
		this.todos = todos;
	}
}
