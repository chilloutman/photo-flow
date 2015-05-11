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
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Tag;

public class PhotoController extends PhotoFlowController implements Initializable {

	/** Currently selected photo. */
	private Photo photo;
	private ErrorHandler errorHandler = new ErrorHandler();
	
	private Optional<PhotoListener> listener;
	
	@FXML
	private Button flagButton, discardButton, editButton;
	
	@FXML
	private Label filePathLabel, fileSizeLabel, metadataLabel;
	
	public void setListener(PhotoListener listener) {
		this.listener = Optional.of(listener);
	}
	
	public void setPhoto(Photo photo) {
		System.out.println("Photo has been selected: " + photo);
		this.photo = photo;
		
		flagButton.setDisable(false);
		discardButton.setDisable(false);
		editButton.setDisable(false);
		
		filePathLabel.textProperty().bind(stringProperty(photo, "filePath"));
		StringExpression fileSize =  Bindings.format("%.2f MB", numberProperty(photo, "fileSize").divide(1024*1014));
		fileSizeLabel.textProperty().bind(fileSize);
		
		try {
			FileHandler fileHandler = photoFlow.fileHandler(photo.getProjectId().get());
			metadataLabel.setText(fileHandler.loadPhotoMetadata(photo));
		} catch (FileHandlerException e) {
			errorHandler.spawnError("Could not load any metadata from your photo :-(");
			metadataLabel.setText("Could not load photo metadata. :-(");
		}
		
		flagButton.setOnAction(event -> {
			listener.ifPresent(listener -> {
				listener.flagPhoto(photo);
				savePhoto();
			});
		});
		discardButton.setOnAction(event -> {
			listener.ifPresent(listener -> {
				listener.discardPhoto(photo);
				savePhoto();
			});
		});
		
		//TODO open FileHandler
		editButton.setOnAction(event -> {
//			listener.ifPresent(listener -> {
//				listener.discardPhoto(photo);
//				savePhoto();
//			});
			System.out.println("edit picture");
			errorHandler.spawnInformation("Opening File explorer to exit your photo");
		});
	}
	
	private void savePhoto() {
		try {
			photoFlow.photoDao().save(photo);
		} catch (DaoException e) {
			errorHandler.spawnError("Your photo could not be safed somehow. Plese try again! Everybody needs a second chance :-)");
			throw new RuntimeException(e);
		}
	}
	
	public void addTag(String tagName) {
		Tag tag = new Tag(tagName);
		this.photo.addTag(tag);

		try {
			photoFlow.photoDao().save(this.photo);
		} catch (DaoException e) {
			this.photo.removeTag(tag);
			errorHandler.spawnError("Your Tags could not be safed. You would not have used the anyway, right?");
			throw new RuntimeException(e);
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
			errorHandler.spawnError("The photo tags could no be safed. Please try again.");
			throw new RuntimeException(e);
		}
	}
	
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
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		flagButton.setDisable(true);
		discardButton.setDisable(true);
		editButton.setDisable(true);
	}
	
}
