package ch.zhaw.photoflow.core;

import java.util.List;

public interface Dao<T> {

	public List<T> load() throws DaoException;
	
	public T save(T object) throws DaoException;
	
	public void delete(T object) throws DaoException;
	
}
