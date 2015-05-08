package ch.zhaw.photoflow.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.PhotoFlow;
import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class ProjectController implements Initializable {
	
	@Inject
	private PhotoFlow photoFlow;
	
	private Project project;
	private FileHandler fileHandler;
	private List<Photo> photos = new ArrayList<Photo>();

	/** Daemon threads for background task execution */
	private final ExecutorService imageLoaderService = newImageLoaderService();
	private final Collection<ImageLoadingTask> imageLoadingTasks = new HashSet<>();

	@FXML
	private PhotoController photoController;
	
	@FXML
	TextField projectNameField;
	@FXML
	Button newButton, importButton, editButton, archiveButton, importPhotoButton, archiveProjectButton, exportProjectButton;
	@FXML
	MenuButton todoButton;
	@FXML
	TilePane photosPane;

	public void setProject(Project project) {
		projectNameField.setText(project.getName());
		System.out.println("Project \"" + project.getName() + "\" has been selected.");
		this.project = project;
		try {
			fileHandler = photoFlow.fileHandler(project);
		} catch (FileHandlerException e) {
			throw new RuntimeException(e);
		}
		loadPhotos();
		displayPhotos();
	}
	
	private void loadPhotos() {
		try {
			List<Photo> loadedPhotos = photoFlow.photoDao().loadAll(project.getId().get());
			this.photos.clear();
			this.photos.addAll(loadedPhotos);
		} catch (DaoException e) {
			// TODO: Inform user that loading failed
		}
	}
	
	private void displayPhotos() {
		// Cancel all previous tasks
		imageLoadingTasks.forEach(task -> task.cancel(true));
		imageLoadingTasks.clear();
		photosPane.getChildren().clear();
		
		for (Photo photo : photos) {
			ImageLoadingTask task = new ImageLoadingTask(photo, fileHandler);
			
			task.valueProperty().addListener((observable, oldImage, image) -> {
				//System.out.println("Creating ImageView for image");
				ImageView view = new ImageView(image);
				view.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
					photoController.setPhoto(photo);
				});
				photosPane.getChildren().add(view);
				imageLoadingTasks.remove(task);
			});
			
			task.onFailedProperty().addListener((observale, a, b) -> {
				// TODO display error message
				System.out.println("Loading photo failed: " + photo);
				imageLoadingTasks.remove(task);
			});
			
			imageLoadingTasks.add(task);
			imageLoaderService.execute(task);
		}
	}
	
	private ExecutorService newImageLoaderService () {
		
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), runnable -> {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(true);
			return thread;
		});
	}

	public void addPhoto(Photo photo) {
		photos.remove(photo);
		photo.setProjectId(this.project.getId().get());
		try {
			photoFlow.photoDao().save(photo);
			photos.add(photo);
		} catch (DaoException e) {
			// TODO: Inform user that photo could not be added to the actual
			// project
		}
	}

	public void importPhotos(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Photos");
		
		List<String> extensions = FileFormat.getAllFileExtensions().stream().map(e -> "*." + e).collect(Collectors.toList());
		ExtensionFilter imageFilter = new ExtensionFilter("Image Files", extensions);
		fileChooser.getExtensionFilters().add(imageFilter);

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(importButton.getScene().getWindow());
		
		if (selectedFiles == null) {
			return;
		}
		
		Photo photo;
		for (File file : selectedFiles) {
			photo = Photo.newPhoto( p -> {
				p.setProjectId(project.getId().get());
			});
			
			try {
				fileHandler.importPhoto(photo, file);
				photoFlow.photoDao().save(photo);
				photos.add(photo);
			} catch (FileHandlerException e) {
				System.out.println("FILEHANDLEREXCEPTION");
				e.printStackTrace();
				// TODO Inform User (FileHandler)
			} catch (DaoException e) {
				System.out.println("DAOEXCEPTION");
				e.printStackTrace();
				// TODO Inform User (DAO)
			}
			
		}
		displayPhotos();	
	}

	public void deletePhoto(Photo photo) {
		try {
			photoFlow.photoDao().delete(photo);
			photos.remove(photo);
		} catch (DaoException e) {
			
			// TODO: Inform user that deletion failed
		}
	}

	/**
	 * Sets the state of the specified @{link Project} object to the given
	 * projectState.
	 * 
	 * @param project
	 * @param projectStatus
	 */
	public void transitionState(Project project, ProjectState projectState) {
		if (photoFlow.projectWorkflow().canTransition(project, this.photos, projectState)) {
			photoFlow.projectWorkflow().transition(project, this.photos, projectState);

			try {
				this.project = photoFlow.projectDao().load(project.getId().get()).get();
				photoFlow.projectDao().save(project);
			} catch (DaoException e) {
				// TODO: Inform user that saving failed
			}
		} else {
			// Inform User
		}
	}

	/**
	 * Sets the status of the specified {@link Photo} object to
	 * {@link PhotoState.Flagged}.
	 * 
	 * @param photo
	 */
	public void flagPhoto(Photo photo) {
		if (photoFlow.photoWorkflow().canTransition(this.project, photo, PhotoState.FLAGGED)) {
			this.photos.remove(photo);
			photoFlow.photoWorkflow().transition(this.project, photo, PhotoState.FLAGGED);
			this.photos.add(photo);
		}
		else {
			// TODO Inform user
		}
	}

	public void archiveProject(ActionEvent event) {
		try {
			fileHandler.archiveProject();
			if(photoFlow.projectWorkflow().canTransition(this.project, photos, ProjectState.ARCHIVED)) {
				photoFlow.projectWorkflow().transition(this.project, photos, ProjectState.ARCHIVED);
			}else {
				// TODO Inform user
			}
		} catch (FileHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void exportProject(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export Project (ZIP)");
		
		ExtensionFilter imageFilter = new ExtensionFilter("Zip Files", "*.zip");
		fileChooser.getExtensionFilters().add(imageFilter);
		
		File selectedFile = fileChooser.showSaveDialog(exportProjectButton.getScene().getWindow());
		
		if (selectedFile != null) {
			try {
			fileHandler.exportZip(selectedFile.getAbsolutePath(), photos);
			} catch (FileHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// direct connection to TextField in FXML GUI
		projectNameField.setText("new Project");
		
		exportProjectButton.setTooltip(new Tooltip("Export Project"));
		archiveProjectButton.setTooltip(new Tooltip("Archive Project"));
		importPhotoButton.setTooltip(new Tooltip("Import a new Photo"));
		
		//Disable first TODO
		//this.setDisable(true);

		importPhotoButton.setOnAction(this::importPhotos);
		archiveProjectButton.setOnAction(this::archiveProject);
		exportProjectButton.setOnAction(this::exportProject);
		
	}

	private static class ImageLoadingTask extends Task<Image> {
		
		private final Photo photo;
		private final FileHandler fileHandler;
		
		public ImageLoadingTask(Photo photo, FileHandler fileHandler) {
			this.photo = photo;
			this.fileHandler = fileHandler;
		}

		@Override
		protected Image call() throws FileHandlerException, IOException {
			//System.out.println("Loading photo: " + photo);
			try (InputStream file = fileHandler.loadPhoto(photo)) {
				return new Image(file, 200, 200, true, true);
			}
		}
	}
	
	@VisibleForTesting
	protected List<Photo> getPhotos() {
		return photos;
	}
	
	@VisibleForTesting
	protected Project getProject() {
		return project;
	}

}
