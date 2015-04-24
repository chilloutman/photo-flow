package ch.zhaw.photoflow.core.dao;

import java.util.Optional;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Project;

import com.google.common.collect.ImmutableList;

public class SqliteProjectDao implements ProjectDao {

	@Override
	public ImmutableList<Project> loadAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Project> load(int id) throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Project save(Project object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Project object) {
		// TODO Auto-generated method stub
		
	}

}
