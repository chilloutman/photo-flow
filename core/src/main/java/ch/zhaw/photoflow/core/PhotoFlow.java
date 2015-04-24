package ch.zhaw.photoflow.core;

import ch.zhaw.photoflow.core.dao.InMemoryPhotoDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.ProjectWorkflow;

/**
 * This is the creator and provider the main core classes (e.g. data access objects).
 */
public class PhotoFlow {
	
//	private final PhotoDao photoDao = new SqlitePhotoDao();
//	private final ProjectDao projectDao = new SqliteProjectDao();

	private final PhotoDao photoDao = new InMemoryPhotoDao();
	private final ProjectDao projectDao = new InMemoryProjectDao();
	
	private final ProjectWorkflow projectWorkflow = new ProjectWorkflow();
	private final PhotoWorkflow photoWorkflow = new PhotoWorkflow();

	public PhotoDao getPhotoDao() {
		return photoDao;
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}
	
	public ProjectWorkflow getProjectWorkflow () {
		return projectWorkflow;
	}
	
	public PhotoWorkflow getPhotoWorkflow () {
		return photoWorkflow;
	}

}
