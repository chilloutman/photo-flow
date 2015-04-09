package ch.zhaw.photoflow.core;

import ch.zhaw.photoflow.core.impl.SqlitePhotoDao;
import ch.zhaw.photoflow.core.impl.SqliteProjectDao;

/**
 * This is the creator and provider the main core classes (e.g. data access objects).
 */
public class Main {
	
	private final PhotoDao photoDao = new SqlitePhotoDao();
	
	private final ProjectDao projectDao = new SqliteProjectDao();

	public PhotoDao getPhotoDao() {
		return photoDao;
	}
	
	public ProjectDao getProjectDao() {
		return projectDao;
	}

}
