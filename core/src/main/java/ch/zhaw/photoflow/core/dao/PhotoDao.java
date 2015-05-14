package ch.zhaw.photoflow.core.dao;

import ch.zhaw.photoflow.core.domain.Photo;

import com.google.common.collect.ImmutableList;

/**
 * Provides access to {@link Photo} objects.
 */
public interface PhotoDao extends Dao<Photo> {
	
	/**
	 * @param projectId The ID of the project to load all photos for.
	 * @return All photos of the project.
	 * The returned list is immutable and the values need to be copied by the caller.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 * Use {@link DaoException#getCause()} to get the storage specific cause.
	 */
	public ImmutableList<Photo> loadAll(int projectId) throws DaoException;
	
}
