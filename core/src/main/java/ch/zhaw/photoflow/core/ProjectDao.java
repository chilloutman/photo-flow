package ch.zhaw.photoflow.core;

import ch.zhaw.photoflow.core.domain.Project;

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
	
}
