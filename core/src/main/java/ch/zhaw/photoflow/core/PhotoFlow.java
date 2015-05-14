package ch.zhaw.photoflow.core;

import com.google.common.annotations.VisibleForTesting;

import ch.zhaw.photoflow.core.dao.DaoException;
import ch.zhaw.photoflow.core.dao.InMemoryPhotoDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.dao.PhotoDao;
import ch.zhaw.photoflow.core.dao.ProjectDao;
import ch.zhaw.photoflow.core.dao.SQLiteConnectionProvider;
import ch.zhaw.photoflow.core.dao.SQLiteInitialize;
import ch.zhaw.photoflow.core.dao.SqlitePhotoDao;
import ch.zhaw.photoflow.core.dao.SqliteProjectDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;
import ch.zhaw.photoflow.core.domain.ProjectWorkflow;

/**
 * This is the creator of dao and business logic classes and main entry point for users of the core module.
 */
public class PhotoFlow {
	
	// TODO: Maybe read this from The environment for easier debugging/development.
	private final boolean DEBUG = false;
	
	private final PhotoDao photoDao;
	private final ProjectDao projectDao;
	
	private final ProjectWorkflow projectWorkflow = new ProjectWorkflow();
	private final PhotoWorkflow photoWorkflow = new PhotoWorkflow();

	/**
	 * Creates an instance for production.
	 */
	public PhotoFlow() {
		this(false);
		if (DEBUG) {
			DummyData.addProjects(projectDao);
			try {
				DummyData.addPhotos(photoDao, projectDao.loadAll().stream().findAny().get());
			} catch (DaoException e) { throw new RuntimeException(e); }
		}
	}
	
	/**
	 * PhotoFlow constructor.
	 * @param test If test is {@code true}, in-memory DAOs will be used in this project instead of a database.
	 */
	@VisibleForTesting
	public PhotoFlow(boolean test) {
		if (test) {
			photoDao = new InMemoryPhotoDao();
			projectDao = new InMemoryProjectDao();
		} else {
			SQLiteConnectionProvider connectionProvider = new SQLiteConnectionProvider();
			SQLiteInitialize.initialize(connectionProvider);
			photoDao = new SqlitePhotoDao(connectionProvider);
			projectDao = new SqliteProjectDao(connectionProvider);
		}
	}

	/**
	 * @param project The project to manage files for.
	 * @return A file handler for the given project.
	 * @throws FileHandlerException When the {@link FileHandler} could not be created.
	 */
	public FileHandler fileHandler(Project project) throws FileHandlerException {
		return new FileHandler(project);
	}
	
	/**
	 * Use {@link #fileHandler(Project)} whenever possible. A FileHandler without a full instance of {@link Project} can't handle archived projects.
	 * @param projectId The id of the project to manage files for.
	 * @return A file handler for the given project.
	 * @throws FileHandlerException When the {@link FileHandler} could not be created.
	 */
	public FileHandler fileHandler(int projectId) throws FileHandlerException {
		return new FileHandler(projectId);
	}

	/**
	 * @return A {@link PhotoDao} to be used for CRUD operation related to {@link Photo photos}.
	 */
	public PhotoDao photoDao() {
		return photoDao;
	}

	/**
	 * @return A {@link ProjectDao} to be used for CRUD operation related to {@link Project projects}.
	 */
	public ProjectDao projectDao() {
		return projectDao;
	}
	
	/**
	 * @return Validate and execute {@link ProjectState} transitions.
	 */
	public ProjectWorkflow projectWorkflow () {
		return projectWorkflow;
	}
	
	/**
	 * @return Validate and execute {@link PhotoState} transitions.
	 */
	public PhotoWorkflow photoWorkflow () {
		return photoWorkflow;
	}
	
}
