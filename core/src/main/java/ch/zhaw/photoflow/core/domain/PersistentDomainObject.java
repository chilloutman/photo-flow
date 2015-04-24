package ch.zhaw.photoflow.core.domain;

import java.util.Optional;

/**
 * Objects relevant to the application domain.
 * These objects can be persisted to some kind of storage.
 */
public interface PersistentDomainObject {
	
	/**
	 * @param id The new id to set. Must be unique across all objects of the same type.
	 */
	public void setId(int id);
	
	/**
	 * @return {@link Optional#empty()} means that the object has not yet been persisted.
	 */
	public Optional<Integer> getId();
	
}
