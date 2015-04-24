package ch.zhaw.photoflow.controller;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;
import ch.zhaw.photoflow.core.domain.ProjectWorkflow;

public class ProjectController extends AbstractController {
	private final ProjectWorkflow workflow;
	private final PhotoWorkflow photoWorkflow;
	ProjectDao projectDao;
	PhotoDao photoDao;
	Project project;
	List<Photo> photos;
	
	public ProjectController(ProjectDao projectDao, PhotoDao photoDao, Project project, ProjectWorkflow workflow, PhotoWorkflow photoWorkflow) {
		this.workflow = workflow;
		this.photoWorkflow = photoWorkflow;
		this.photos = new ArrayList<Photo>();
		this.projectDao = projectDao;
		this.photoDao = photoDao;
		this.project = project;
		loadPhotos(project);
	}
	
	public Project getProject() {
		return project;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	public void loadPhotos(Project project) {
		List<Photo> tempPhotos = new ArrayList<Photo>(this.photos);
		try {
			this.photos.clear();
			this.photos.addAll(this.photoDao.loadAll(project.getId().get()));
		} catch (DaoException e) {
			//TODO: Inform user that loading failed
			this.photos = tempPhotos;
		}
	}
	
	public void addPhoto(Photo photo) {
		photos.remove(photo);
		photo.setProjectId(this.project.getId().get());
		photos.add(photo);
		try {
			photoDao.save(photo);
		} catch (DaoException e) {
			photos.remove(photo);
			//TODO: Inform user that photo could not be added to the actual project
		}
	}
	
	public void importPhotos() {
		//TODO: Usecase Import photos
	}
	
	public void deletePhoto(Photo photo) {
		this.photos.remove(photo);
		
		try {
			photoDao.delete(photo);
		} catch (DaoException e) {
			this.photos.add(photo);
			//TODO: Inform user that deletion failed
		}
	}
	
	/**
	 * Sets the state of the specified @{link Project} object to the given projectState.
	 * @param project
	 * @param projectStatus
	 */
	public void transistState(Project project, ProjectState projectState) {

		workflow.transition(project, this.photos, projectState);
		try {
			this.projectDao.save(project);
			try {
				this.project = projectDao.load(project.getId().get()).get();			
			}
			catch (DaoException e) {
				//TODO: Warn user that rollback failed
			}
		} catch (DaoException e) {
			// TODO: Inform user that saving failed
			//TODO: ROLLBACK REQUIRED. Project Model is now in a wrong state.
		}
	}
	
	/**
	 * Sets the status of the specified {@link Photo} object to {@link PhotoState.Flagged}.
	 * @param photo
	 */
	public void flagPhoto(Photo photo) {
		this.photos.remove(photo);
		photoWorkflow.transition(this.project, photo, PhotoState.FLAGGED);
		this.photos.add(photo);
	}
	
	/*
	 * Getter and Setter
	 */
	public List<Photo> getPhotos() {
		return photos;
	}
}
