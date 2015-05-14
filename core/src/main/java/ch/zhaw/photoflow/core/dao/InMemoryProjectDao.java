package ch.zhaw.photoflow.core.dao;

import static ch.zhaw.photoflow.core.util.GuavaCollectors.toImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Todo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Implementation of {@link ProjectDao} without persistence.
 * Useful for testing.
 */
public class InMemoryProjectDao implements ProjectDao {
	
	private static int idCounter = 0;
	
	private static int nextId () {
		return idCounter++;
	}
	
	private final List<Project> projects = new ArrayList<>();
	
	/**
	 * Load all projects.
	 * @return An immutable list of all projects. {@link #save(Project)} must be called after changing projects.
	 */
	@Override
	public ImmutableList<Project> loadAll() {
		return projects.stream().map(Project::copy).collect(toImmutableList());
	}
	
	@Override
	public Optional<Project> load(int id) throws DaoException {
		// Search for a project with the given id.
		List<Project> found = projects.stream().filter(p -> p.getId().get().equals(id)).collect(Collectors.toList());
		if (found.size() > 1) {
			throw new DaoException("Project ID " + id + "is not unique! This is bad and should not have happened!");
		}
		return found.stream().findFirst().map(Project::copy);
	}

	@Override
	public Project save(Project project) {
		if (project.getId().isPresent()) {
			// Remove old project
			projects.removeIf(p -> p.getId().equals(project.getId()));
		} else {
			// Generate new ID.
			project.setId(nextId());
		}
		// Add new project
		projects.add(Project.copy(project));
		return project;
	}

	@Override
	public void delete(Project project) {
		Preconditions.checkNotNull(project);
		Preconditions.checkArgument(project.getId().isPresent());
		projects.remove(project);
	}

	@Override
	public List<Todo> loadAllTodosByProject(Project project)
			throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Todo> loadTodo(int id) throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Todo saveTodo(Project project, Todo todo) throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteTodo(Todo todo) throws DaoException {
		// TODO Auto-generated method stub
		
	}

}
