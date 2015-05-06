package ch.zhaw.photoflow.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	/** Daemon threads for background task execution */
	private final ExecutorService background;

	@FXML
	private PhotoController photoController;
	
	@FXML
	TextField projectNameField;
	@FXML
	Button workflowNextButton, workflowPauseButton, workflowBackButton, importPhotoButton, archiveProjectButton, exportProjectButton;
	@FXML
	MenuButton todoButton;
	@FXML
	TilePane photosPane;

	public ProjectController() {
		this(Main.photoFlow.getProjectDao(), Main.photoFlow.getPhotoDao(), Main.photoFlow.getProjectWorkflow(), Main.photoFlow.getPhotoWorkflow());
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
		
		this.background = Executors.newFixedThreadPool(5, runnable -> {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(true);
			return thread;
		});
	}
	
	public void setProject(Project project) {
		projectNameField.setText(project.getName());
		System.out.println("Project \"" + project.getName() + "\" has been selected.");
		this.project = project;
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
		photosPane.getChildren().clear();
		
		// Asynchronously load Images and display them
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			Task<Image> imageTask = new Task<Image>() {
				@Override
				protected Image call() throws Exception {
					int width = (random.nextInt(15) + 10) * 10;
					int height = (random.nextInt(15) + 10) * 10;
					return new Image("http://lorempixel.com/" + width + "/" + height + "/");
				}
			};
			
			imageTask.valueProperty().addListener((observable, oldImage, image) ->{
				photosPane.getChildren().add(new ImageView(image));
			});
			
			//imageTask.onFailedProperty().addListener(TODO);
			
			background.execute(imageTask);
		}
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
		
		FileHandler filehandler;
		try {
			filehandler = new FileHandler(this.project);
			
			Photo photo;
			for (File file : selectedFiles) {
				photo = Photo.newPhoto( p -> {
					p.setProjectId(project.getId().get());
				});
				
				try {
					filehandler.importPhoto(photo, file);
					photoDao.save(photo);
				} catch (IOException e) {
					System.out.println("IOEXCEPTION");
					// TODO Inform User (FileHandler)
					e.printStackTrace();
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
		} catch (FileHandlerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
	public void transistState(Project project, ProjectState projectState) {
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
			// Inform user
		}
	}

	@FXML
	public void syso() {
		System.out.println("Pause klicked (over @FXML Annotation)");
	}

	public void archiveProject(ActionEvent event) {
		System.out.println("Project archived!");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// direct connection to TextField in FXML GUI
		projectNameField.setText("new Project");
		
		//Disable first
		this.setDisable(true);

		// inline
		workflowNextButton.setOnAction(event -> {
			System.out.println("clicked Next");
		});

		// external method
		workflowNextButton.setOnAction(this::test);

		archiveProjectButton.setOnAction(this::archiveProject);
		importPhotoButton.setOnAction(this::importPhotos);

	}

	public void test(ActionEvent event) {

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
