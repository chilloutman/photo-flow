package ch.zhaw.photoflow.core.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public class Project implements PersistentDomainObject {

	private Optional<Integer> id = Optional.empty();
	private String name;
	private String description;
	private ProjectState state = ProjectState.NEW;
	private List<Todo> todos = new ArrayList<>();
	
	public static Project newProject () {
		return new Project();
	}
	
	/**
	 * Conveniently create and configure a new instance.
	 * @param setUpProject configure function.
	 * @return the new instance.
	 */
	public static Project newProject (Consumer<Project> configureProject) {
		Project p = newProject();
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
			p.state = project.state;
			p.todos = new ArrayList<>(project.todos);
		});
	}
	
	private Project() {
	}

	@Override
	public Optional<Integer> getId() {
		return id;
	}
	
	@Override
	public void setId(int id) {
		this.id = Optional.of(id);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = checkNotNull(name);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = checkNotNull(description);
	}

	public ProjectState getState() {
		return state;
	}
	
	/** Not public, so that only the Workflow can change it. */
	void setState(ProjectState state) {
		this.state = state;
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
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
			.add("description", description)
			.add("state", state)
			.add("todos", todos)
			.toString();
	}
	
}
