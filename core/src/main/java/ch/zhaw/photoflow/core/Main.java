package ch.zhaw.photoflow.core;

import ch.zhaw.photoflow.core.dao.InMemoryPhotoDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;

/**
 * This is the creator and provider the main core classes (e.g. data access objects).
 */
public class Main {
	
//	private final PhotoDao photoDao = new SqlitePhotoDao();
//	private final ProjectDao projectDao = new SqliteProjectDao();

	private final PhotoDao photoDao = new InMemoryPhotoDao();
	private final ProjectDao projectDao = new InMemoryProjectDao();

	public PhotoDao getPhotoDao() {
		return photoDao;
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}

}
