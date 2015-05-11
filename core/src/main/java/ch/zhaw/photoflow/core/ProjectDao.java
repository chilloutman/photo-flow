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
	
	/**
	 * 
	 * @param {@link Project}
	 * @return all todos held by a specific {@link Project}
	 * @throws DaoException
	 */
	public List<Todo> loadAllTodosByProject(Project project) throws DaoException;
	
	/**
	 * @param id of a {@link Todo}.
	 * @return a {@link Todo} identified by given parameter.
	 * @throws DaoException
	 */
	public Optional<Todo> loadTodo(int id) throws DaoException;
	
	/**
	 * Saves the {@link Todo} object relating to the provided {@link Project}.
	 * @param {@link Project}
	 * @param {@link Todo}
	 * @return {@link Todo}
	 * @throws DaoException
	 */
	public Todo saveTodo(Project project, Todo todo) throws DaoException;
	
	/**
	 * Removes a Todo.
	 * @param {@link todo}
	 * @throws DaoException
	 */
	public void deleteTodo(Todo todo) throws DaoException;
}
