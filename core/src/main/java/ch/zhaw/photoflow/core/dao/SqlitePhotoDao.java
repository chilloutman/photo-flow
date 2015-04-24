package ch.zhaw.photoflow.core.dao;

import java.util.Optional;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.domain.Photo;

import com.google.common.collect.ImmutableList;

public class SqlitePhotoDao implements PhotoDao {

	@Override
	public ImmutableList<Photo> loadAll(int projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Photo> load(int id) throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Photo save(Photo object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Photo object) {
		// TODO Auto-generated method stub
		
	}

}
