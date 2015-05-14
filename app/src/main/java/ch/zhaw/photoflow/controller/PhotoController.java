package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.dao.DaoException;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Tag;

/**
 * Controller for {@link Photo} specific interactions.
 */
public class PhotoController extends PhotoFlowController implements Initializable {

	private static final String STATE_PROPERTY = "state";
	
	/** Currently selected photo. */
	private Photo photo;
	
	private Optional<PhotoListener> listener;
	
	@FXML
	private Button flagButton, discardButton, editButton, deleteButton;
	
	@FXML
	private Label filePathLabel, fileSizeLabel, stateLabel, metadataLabel;
	
	/**
	 * @param listener Listener to set.
	 * @see PhotoListener
	 */
	public void setListener(PhotoListener listener) {
		this.listener = Optional.of(listener);
	}
	
	/**
	 * Informs the {@link PhotoController} which {@link Photo} to display.
	 * @param photo Information for this photo will be loaded and displayed.
	 */
	public void setPhoto(Photo photo) {
		System.out.println("Photo has been selected: " + photo);
		this.photo = photo;
		
		reset();
		
		if (this.photo != null) {
			initializeLabels();
			initializeButtons();
		}
	}
	
	/**
	 * Resets the {@link PhotoController}
	 */
	public void reset() {
		flagButton.setDisable(true);
		discardButton.setDisable(true);
		editButton.setDisable(true);
		deleteButton.setDisable(true);
		
		filePathLabel.textProperty().unbind();
		filePathLabel.setText("");
		
		fileSizeLabel.textProperty().unbind();
		fileSizeLabel.setText("");
		
		stateLabel.textProperty().unbind();
		stateLabel.setText("");
		
		metadataLabel.setVisible(false);
	}
	
	private void initializeLabels() {
		filePathLabel.textProperty().bind(stringProperty(photo, "filePath"));
		
		StringExpression fileSize =  Bindings.format("%.2f MB", numberProperty(photo, "fileSize").divide(1024*1014));
		fileSizeLabel.textProperty().bind(fileSize);
		
		stateLabel.textProperty().bind(objectProperty(photo, STATE_PROPERTY).asString("State: %s"));
		
		try {
			FileHandler fileHandler = photoFlow.fileHandler(photo.getProjectId().get());
			String metadata = fileHandler.loadPhotoMetadata(photo);
			metadataLabel.setText("Metadata/Exif:\n" + metadata);
			metadataLabel.setVisible(!metadata.isEmpty());
		} catch (FileHandlerException e) {
			EventHandler.spawnError("Could not load any metadata from your photo :-(");
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Initializes photo related buttons.
	 */
	private void initializeButtons() {
		flagButton.setOnAction(event -> {
			listener.ifPresent(listener -> {
				listener.flagPhoto(photo);
				savePhoto();
				updateButtons();
				fireStateChangeEvent();
			});
		});
		discardButton.setOnAction(event -> {
			listener.ifPresent(listener -> {
				listener.discardPhoto(photo);
				savePhoto();
				updateButtons();
				fireStateChangeEvent();
			});
		});
		editButton.setOnAction(event -> {
			listener.ifPresent(listener -> {
				listener.editPhoto(photo);
				//savePhoto();
				fireStateChangeEvent();
			});
		});
		deleteButton.setOnAction(event -> {
			listener.ifPresent(listener -> {
				listener.deletePhoto(photo);
				//savePhoto();
				updateButtons();
				fireStateChangeEvent();
			});
		});
		
		updateButtons();
	}
	
	private void fireStateChangeEvent() {
		objectProperty(photo, STATE_PROPERTY).fireValueChangedEvent();
	}
	
	/**
	 * Disables buttons if not meant for usage in actual photostate.
	 */
	private void updateButtons() {
		flagButton.setDisable(PhotoState.FLAGGED.equals(photo.getState()));
		discardButton.setDisable(PhotoState.DISCARDED.equals(photo.getState()));
		editButton.setDisable(false);
		deleteButton.setDisable(false);
	}
	
	/**
	 * Saves a {@link Photo}
	 */
	private void savePhoto() {
		try {
			photoFlow.photoDao().save(photo);
		} catch (DaoException e) {
			EventHandler.spawnError("Your photo could not be safed somehow. Plese try again! Everybody needs a second chance :-)");
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Adds and saves a new {@link Tag}
	 * @param tagName
	 */
	public void addTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.addTag(tag);

		try {
			photoFlow.photoDao().save(this.photo);
		} catch (DaoException e) {
			this.photo.removeTag(tag);
			EventHandler.spawnError("Your Tags could not be safed. You would not have used the anyway, right?");
			throw new RuntimeException(e);
		}

	}

	/**
	 * Deletes a {@link Tag}
	 * @param tagName The name of the tag to be deleted.
	 */
	public void deleteTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.removeTag(tag);

		try {
			photoFlow.photoDao().save(this.photo);
		} catch (DaoException e) {
			this.photo.addTag(tag);
			EventHandler.spawnError("The photo tags could no be safed. Please try again.");
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets notified about actions to be executed on a photo.
	 * The listener is responsible for handling status transitions and delete operations.
	 */
	public static interface PhotoListener {
	
		/**
		 * Sets the status of the specified {@link Photo} object to {@link PhotoState#FLAGGED}.
		 * @param photo
		 */
		public void flagPhoto(Photo photo);
		
		/**
		 * Sets the status of the specified {@link Photo} object to {@link PhotoState#DISCARDED}.
		 * @param photo
		 */
		public void discardPhoto(Photo photo);
		
		/**
		 * Sets the status of the specified {@link Photo} object to {@link PhotoState#EDITING}.
		 * @param photo
		 */
		public void editPhoto(Photo photo);
		
		/**
		 * Sets the status of the specified {@link Photo} object to {@link PhotoState#EDITING}.
		 * @param photo
		 */
		public void deletePhoto(Photo photo);
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		flagButton.setDisable(true);
		discardButton.setDisable(true);
		editButton.setDisable(true);
		deleteButton.setDisable(true);
	}
	
}
