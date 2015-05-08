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

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import ch.zhaw.photoflow.Main;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;
import ch.zhaw.photoflow.core.domain.ProjectWorkflow;

import com.google.common.annotations.VisibleForTesting;

public class ProjectController extends BorderPane implements Initializable {
	private final ProjectWorkflow projectWorkflow;
	private final PhotoWorkflow photoWorkflow;
	private final ProjectDao projectDao;
	private final PhotoDao photoDao;
	private Project project;
	private List<Photo> photos;
	private FileHandler fileHandler;
	
	/** Daemon threads for background task execution */
	private final ExecutorService imageLoaderService;
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

	public ProjectController() {
		this(Main.PHOTO_FLOW.getProjectDao(), Main.PHOTO_FLOW.getPhotoDao(), Main.PHOTO_FLOW.getProjectWorkflow(), Main.PHOTO_FLOW.getPhotoWorkflow());
		URL gui = getClass().getResource("../view/project.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(gui);
		fxmlLoader.setController(this);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public ProjectController(ProjectDao projectDao, PhotoDao photoDao, ProjectWorkflow workflow, PhotoWorkflow photoWorkflow) {
		this.projectWorkflow = workflow;
		this.photoWorkflow = photoWorkflow;
		this.photos = new ArrayList<Photo>();
		this.projectDao = projectDao;
		this.photoDao = photoDao;
		this.imageLoaderService = newImageLoaderService();
	}
	
	public void setProject(Project project) {
		projectNameField.setText(project.getName());
		System.out.println("Project \"" + project.getName() + "\" has been selected.");
		this.project = project;
		try {
			fileHandler = new FileHandler(project);
		} catch (FileHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loadPhotos();
		displayPhotos();
	}
	
	private void loadPhotos() {
		try {
			List<Photo> loadedPhotos = this.photoDao.loadAll(project.getId().get());
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
			photoDao.save(photo);
			photos.add(photo);
		} catch (DaoException e) {
			// TODO: Inform user that photo could not be added to the actual
			// project
		}
	}

	public void importPhotos(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Photos");
		
		ExtensionFilter imageFilter = new ExtensionFilter("Image Files", "*.png", "*.jpg");
		fileChooser.getExtensionFilters().add(imageFilter);

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(this.getScene().getWindow());
		
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
				photoDao.save(photo);
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
			photoDao.delete(photo);
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
		if (projectWorkflow.canTransition(project, this.photos, projectState)) {
			projectWorkflow.transition(project, this.photos, projectState);

			try {
				this.projectDao.save(project);
				try {
					this.project = projectDao.load(project.getId().get()).get();
				} catch (DaoException e) {
					// TODO: Warn user that rollback failed
				}
			} catch (DaoException e) {
				// TODO: Inform user that saving failed
				// TODO: ROLLBACK REQUIRED. Project Model is now in a wrong
				// state.
			}
		}
		else {
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
		if (photoWorkflow.canTransition(this.project, photo, PhotoState.FLAGGED)) {
			this.photos.remove(photo);
			photoWorkflow.transition(this.project, photo, PhotoState.FLAGGED);
			this.photos.add(photo);
		}
		else {
			// TODO Inform user
		}
	}

	public void archiveProject(ActionEvent event) {
		try {
			fileHandler.archiveProject();
			if(projectWorkflow.canTransition(this.project, photos, ProjectState.ARCHIVED)) {
				projectWorkflow.transition(this.project, photos, ProjectState.ARCHIVED);
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
		
		File selectedFile = fileChooser.showSaveDialog(this.getScene().getWindow());
		
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
		
		//Disable first
		this.setDisable(true);

		importPhotoButton.setOnAction(this::importPhotos);
		archiveProjectButton.setOnAction(this::archiveProject);
		exportProjectButton.setOnAction(this::exportProject);
		
	}

	public void test(ActionEvent event) {

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
