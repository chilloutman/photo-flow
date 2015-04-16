package ch.zhaw.photoflow.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Project {

	private Optional<Integer> id = Optional.empty();
	private String name;
	private String description;
	private ProjectStatus status = ProjectStatus.NEW;
	private List<Todo> todos;
	
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
	
	public static Project copy (Project project) {
		return newProject(p -> {
			if (project.getId().isPresent()) {
				p.setId(project.getId().get());
			}
			p.setName(project.getName());
			p.setDescription(project.getDescription());
			p.setStatus(project.getStatus());
			p.setTodos(project.getTodos()); // TODO: copy this?
		});
	}
	
	public Project() {
	}

	/************ GETTERS AND SETTERS ************/
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

	public List<Todo> getTodos() {
		return todos;
	}

	public void setTodos(List<Todo> todos) {
		this.todos = todos;
	}
}
