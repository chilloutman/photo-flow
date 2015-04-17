package ch.zhaw.photoflow.core;

import java.util.List;

import ch.zhaw.photoflow.core.domain.Project;

public interface ProjectDao extends Dao<Project> {
	
	public List<Project> loadAll() throws DaoException;
	
}
