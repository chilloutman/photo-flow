package ch.zhaw.photoflow.core;

import java.sql.SQLException;
import java.util.Optional;

import ch.zhaw.photoflow.core.domain.PersistentDomainObject;

/**
 * Provides access to objects in some kind of storage.
 * @param <T> The type of the objects being accessed.
 */
public interface Dao<T extends PersistentDomainObject> {

	/**
	 * @param id The identifier of the instance to load.
	 * @return An object from storage or {@link Optional#empty()} if no object could be found.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 * Use {@link DaoException#getCause()} to get the storage specific cause.
	 */
	public Optional<T> load(int id) throws DaoException;
	
	/**
	 * This method can have UPDATE or CREATE semantics if the object:
	 * <ul>
	 * 	<li>{@link PersistentDomainObject#getId() ID} is empty: CREATE, generate new id, {@link PersistentDomainObject#setId(int) set new id} on object.
	 * 	<li>{@link PersistentDomainObject#getId() ID} is not empty: UPDATE.
	 * </ul>
	 * @param object The object to save to storage.
	 * @return The same as the input object.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 * Use {@link DaoException#getCause()} to get the storage specific cause.
	 * @throws SQLException 
	 */
	public T save(T object) throws DaoException, SQLException;
	
	/**
	 * @param object The object to delete.
	 * @throws DaoException If something goes wrong with the storage layer below.
	 * Use {@link DaoException#getCause()} to get the storage specific cause.
	 * @throws SQLException 
	 */
	public void delete(T object) throws DaoException, SQLException;
	
}
