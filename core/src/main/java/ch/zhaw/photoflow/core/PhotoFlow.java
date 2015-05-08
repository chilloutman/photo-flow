package ch.zhaw.photoflow.core;

import ch.zhaw.photoflow.core.dao.InMemoryPhotoDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.dao.SqlitePhotoDao;
import ch.zhaw.photoflow.core.dao.SqliteProjectDao;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.ProjectWorkflow;

/**
 * This is the creator and provider the main core classes (e.g. data access objects).
 */
public class PhotoFlow {
	
	// TODO: Maybe read this from The environment.
	private final boolean DEBUG = false;
	
	private final PhotoDao photoDao;
	private final ProjectDao projectDao;
	
	private final ProjectWorkflow projectWorkflow = new ProjectWorkflow();
	private final PhotoWorkflow photoWorkflow = new PhotoWorkflow();

	public PhotoFlow() {
		if (DEBUG) {
			photoDao = new InMemoryPhotoDao();
			projectDao = new InMemoryProjectDao();

			DummyData.addProjects(projectDao);
			try {
				DummyData.addPhotos(photoDao, projectDao.loadAll().stream().findAny().get());
			} catch (DaoException e) { throw new RuntimeException(e); }
		} else {
			
			//SQLite Initializer
			SQLiteInitialize.initialize();
			
			photoDao = new SqlitePhotoDao();
			projectDao = new SqliteProjectDao();
		}
	}

	public FileHandler getFileHandler(int projectId) throws FileHandlerException {
		return new FileHandler(projectId);
	}

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
