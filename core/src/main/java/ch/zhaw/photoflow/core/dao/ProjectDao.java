package ch.zhaw.photoflow.core.dao;

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
	 * @param project Todos for this project will be loaded.
	 * @return all todos held by the given {@link Project}.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 */
	public List<Todo> loadAllTodosByProject(Project project) throws DaoException;
	
	/**
	 * @param id of a {@link Todo}.
	 * @return a {@link Todo} identified by given parameter.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 */
	public Optional<Todo> loadTodo(int id) throws DaoException;
	
	/**
	 * Saves the {@link Todo} object relating to the provided {@link Project}.
	 * @param project {@link Project} that the todo is linked to.
	 * @param todo {@link Todo} The todo to save.
	 * @return {@link Todo} The given todo with generated id.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 */
	public Todo saveTodo(Project project, Todo todo) throws DaoException;
	
	/**
	 * Removes a Todo.
	 * @param todo The todo to be remove.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 */
	public void deleteTodo(Todo todo) throws DaoException;
}
