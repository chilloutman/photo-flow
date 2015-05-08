package ch.zhaw.photoflow.core;

import ch.zhaw.photoflow.core.dao.InMemoryPhotoDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.dao.SqlitePhotoDao;
import ch.zhaw.photoflow.core.dao.SqliteProjectDao;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.Project;
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
		this(false);
		if (DEBUG) {
			DummyData.addProjects(projectDao);
			try {
				DummyData.addPhotos(photoDao, projectDao.loadAll().stream().findAny().get());
			} catch (DaoException e) { throw new RuntimeException(e); }
		}
	}
	
	public PhotoFlow(boolean test) {
		if (test) {
			photoDao = new InMemoryPhotoDao();
			projectDao = new InMemoryProjectDao();
		} else {
			SQLiteInitialize.initialize();
			photoDao = new SqlitePhotoDao();
			projectDao = new SqliteProjectDao();
		}
	}

	public FileHandler fileHandler(Project project) throws FileHandlerException {
		return new FileHandler(project.getId().get());
	}
	
	public FileHandler fileHandler(int projectId) throws FileHandlerException {
		return new FileHandler(projectId);
	}

	public PhotoDao photoDao() {
		return photoDao;
	}

	public ProjectDao projectDao() {
		return projectDao;
	}
	
	public ProjectWorkflow projectWorkflow () {
		return projectWorkflow;
	}
	
	public PhotoWorkflow photoWorkflow () {
		return photoWorkflow;
	}
	
}
