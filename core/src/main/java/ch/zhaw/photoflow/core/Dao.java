package ch.zhaw.photoflow.core;

import java.util.List;

public interface Dao<T> {

	public List<T> load();
	
	public T save(T object);
	
	public void delete(T object);
	
}
