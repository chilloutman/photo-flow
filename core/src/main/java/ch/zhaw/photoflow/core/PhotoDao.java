package ch.zhaw.photoflow.core;

import java.util.List;

import ch.zhaw.photoflow.core.domain.Photo;

public interface PhotoDao extends Dao<Photo> {
	
	public List<Photo> loadAll(int projectId) throws DaoException;
	
}
