package ch.zhaw.photoflow.core;

import java.util.List;
import java.util.Optional;

import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Todo;

import com.google.common.collect.ImmutableList;

/**
 * Provides access to {@link Project} objects. 
 */
public interface ProjectDao extends Dao<Project> {
	
	/**
	 * @return All projects from storage with no filters applied.
	 * The returned list is immutable and the values need to be copied by the caller.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 * Use {@link DaoException#getCause()} to get the storage specific cause.
	 */
	public ImmutableList<Project> loadAll() throws DaoException;
	
	public List<Todo> loadAllTodosByProject(Project project) throws DaoException;
	public Optional<Todo> loadTodo(int id) throws DaoException;
	public Todo saveTodo(Project project, Todo todo) throws DaoException;
	public void deleteTodo(Todo todo) throws DaoException;
}
