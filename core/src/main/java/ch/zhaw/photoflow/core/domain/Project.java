package ch.zhaw.photoflow.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

public class Project {

	private Optional<Integer> id = Optional.empty();
	private String name;
	private String description;
	private ProjectStatus status = ProjectStatus.NEW;
	private List<Todo> todos = new ArrayList<>();
	
	/**
	 * Conveniently create and configure a new instance.
	 * @param setUpProject configure function.
	 * @return the new instance.
	 */
	public static Project newProject (Consumer<Project> configureProject) {
		Project p = new Project();
		configureProject.accept(p);
		return p;
	}
	
	/**
	 * Creates a new instance an copies all properties to it.
	 * @param project the project to copy the properties from.
	 * @return the new instance.
	 */
	public static Project copy (Project project) {
		return newProject(p -> {
			p.id = project.id;
			p.name = project.name;
			p.description = project.description;
			p.status = project.status;
			p.todos = new ArrayList<>(project.todos);
		});
	}
	
	private Project() {
	}

	public Optional<Integer> getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = Optional.of(id);
	}
	
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

	public ImmutableList<Todo> getTodos() {
		return ImmutableList.copyOf(todos);
	}
	
	public void addTodo(Todo todo) {
		todos.add(todo);
	}
	
	public void removeTodo(Todo todo) {
		todos.remove(todo);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	/**
	 * Only the id is relevant for equality.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		Project that = (Project) object;

		return Objects.equals(id, that.id);
	}
	
}
