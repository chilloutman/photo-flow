package ch.zhaw.photoflow.core;

import ch.zhaw.photoflow.core.dummy.DummyPhotoDao;
import ch.zhaw.photoflow.core.dummy.DummyProjectDao;

/**
 * This is the creator and provider the main core classes (e.g. data access objects).
 */
public class Main {
	
//	private final PhotoDao photoDao = new SqlitePhotoDao();
//	private final ProjectDao projectDao = new SqliteProjectDao();

	private final PhotoDao photoDao = new DummyPhotoDao();
	private final ProjectDao projectDao = new DummyProjectDao();

	public PhotoDao getPhotoDao() {
		return photoDao;
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}

}
