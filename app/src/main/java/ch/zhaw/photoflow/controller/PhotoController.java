package ch.zhaw.photoflow.controller;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import ch.zhaw.photoflow.Main;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;

public class PhotoController extends AnchorPane {

	private final PhotoWorkflow workflow;
	private final PhotoDao photoDao;
	private Photo photo;
	
	@FXML
	private Label filePathLabel, fileSizeLabel, metadataLabel;

	public PhotoController() {
		this(Main.PHOTO_FLOW.getPhotoDao(), Main.PHOTO_FLOW.getPhotoWorkflow());
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/photo.fxml"));
		fxmlLoader.setController(this);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public PhotoController(PhotoDao photoDao, PhotoWorkflow workflow) {
		this.photoDao = photoDao;
		this.workflow = workflow;
	}

	public void setPhoto(Photo photo) {
		System.out.println("Photo has been selected: " + photo);
		this.photo = photo;
		
		filePathLabel.textProperty().bind(stringProperty(photo, "filePath"));
		StringExpression fileSize =  Bindings.format("%.2f MB", numberProperty(photo, "fileSize").divide(1024*1014));
		fileSizeLabel.textProperty().bind(fileSize);
		
		try {
			FileHandler fileHandler = Main.PHOTO_FLOW.getFileHandler(photo.getProjectId().get());
			metadataLabel.setText(fileHandler.loadPhotoMetadata(photo));
		} catch (FileHandlerException e) {
			metadataLabel.setText("Could not load photo metadata. :-(");
		}
	}

	private StringProperty stringProperty(Object bean, String property) {
		try {
			return JavaBeanStringPropertyBuilder.create().bean(bean).name(property).build();
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private ReadOnlyFloatProperty numberProperty(Object bean, String property) {
		try {
			// Convert to float property so we can divide and get decimals if required.
			return ReadOnlyFloatProperty.readOnlyFloatProperty(
				JavaBeanIntegerPropertyBuilder.create().bean(bean).name(property).build()
			);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
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
		workflow.transition(project, photo, photoState);
		try {
			photoDao.save(photo);
		} catch (DaoException e) {
			try {
				this.photo = photoDao.load(photo.getId().get()).get();
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
			this.photoDao.save(this.photo);
		} catch (DaoException e) {
			this.photo.removeTag(tag);
			// TODO: Inform user, that persistence failed
		}

	}

	public void deleteTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.removeTag(tag);

		try {
			this.photoDao.save(this.photo);
		} catch (DaoException e) {
			this.photo.addTag(tag);
			// TODO: Inform user, that persistence failed
		}
	}

	/*
	 * Getter and Setter
	 */
	public Photo getPhoto() {
		return photo;
	}

}
