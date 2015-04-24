package ch.zhaw.photoflow.controller;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;

public class PhotoController extends AbstractController {
	
	PhotoWorkflow workflow;
	PhotoDao photoDao;
	Photo photo;
	
	public PhotoController(PhotoDao photoDao, Photo photo, PhotoWorkflow workflow) {
		this.photoDao = photoDao;
		this.photo = photo;
		this.workflow = workflow;
	}
	
	/**
	 * Sets the state of the specified @{link Photo} object to the given photoState.
	 * @param project
	 * @param photoState
	 */
	public void transistState(Project project, PhotoState photoState) {
		
		
		workflow.transition(project, photo, photoState);
		try {
			photoDao.save(photo);
		} catch (DaoException e) {
			try {
				this.photo = photoDao.load(photo.getId().get()).get();
				//TODO: Warn user that photo couldn't get saved.
			} catch (DaoException e1) {
				//TODO: Warn user that photo couldn't get rolled back.
			}
		}
	}
	
	public void addTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.addTag(tag);
		
		try {
			this.photoDao.save(this.photo);
		} catch (DaoException e) {
			this.photo.removeTag(tag);
			//TODO: Inform user, that persistence failed
		}
		
	}
	
	public void deleteTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.removeTag(tag);
		
		try {
			this.photoDao.save(this.photo);
		} catch (DaoException e) {
			this.photo.addTag(tag);
			//TODO: Inform user, that persistence failed
		}
	}

	/*
	 * Getter and Setter
	 */
	public Photo getPhoto() {
		return photo;
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
	}
	
}