package ch.zhaw.photoflow.core;

import java.util.Optional;

public interface Dao<T> {

	public Optional<T> load(int id) throws DaoException;
	
	public T save(T object) throws DaoException;
	
	public void delete(T object) throws DaoException;
	
}
