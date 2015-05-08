package ch.zhaw.photoflow.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;

public class PhotoController extends PhotoFlowController {

	/** Currently selected photo. */
	private Photo photo;
	
	@FXML
	private Label filePathLabel, fileSizeLabel, metadataLabel;

	public void setPhoto(Photo photo) {
		System.out.println("Photo has been selected: " + photo);
		this.photo = photo;
		
		filePathLabel.textProperty().bind(stringProperty(photo, "filePath"));
		StringExpression fileSize =  Bindings.format("%.2f MB", numberProperty(photo, "fileSize").divide(1024*1014));
		fileSizeLabel.textProperty().bind(fileSize);
		
		try {
			FileHandler fileHandler = photoFlow.fileHandler(photo.getProjectId().get());
			metadataLabel.setText(fileHandler.loadPhotoMetadata(photo));
		} catch (FileHandlerException e) {
			metadataLabel.setText("Could not load photo metadata. :-(");
		}
	}

	/**
	 * Sets the state of the specified @{link Photo} object to the given
	 * photoState.
	 * 
	 * @param project
	 * @param photoState
	 */
	public void transitionState(Project project, PhotoState photoState) {
		photoFlow.photoWorkflow().transition(project, photo, photoState);
		try {
			photoFlow.photoDao().save(photo);
		} catch (DaoException e) {
			try {
				this.photo = photoFlow.photoDao().load(photo.getId().get()).get();
				// TODO: Warn user that photo couldn't get saved.
			} catch (DaoException e1) {
				// TODO: Warn user that photo couldn't get rolled back.
			}
		}
	}

	public void addTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.addTag(tag);

		try {
			photoFlow.photoDao().save(this.photo);
		} catch (DaoException e) {
			this.photo.removeTag(tag);
			// TODO: Inform user, that persistence failed
		}

	}

	public void deleteTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.removeTag(tag);

		try {
			photoFlow.photoDao().save(this.photo);
		} catch (DaoException e) {
			this.photo.addTag(tag);
			// TODO: Inform user, that persistence failed
		}
	}

}
