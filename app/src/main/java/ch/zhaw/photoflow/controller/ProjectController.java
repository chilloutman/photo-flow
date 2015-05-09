package ch.zhaw.photoflow.controller;

import impl.org.controlsfx.skin.CheckComboBoxSkin;

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

import org.controlsfx.control.CheckComboBox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;
import ch.zhaw.photoflow.core.domain.Todo;

import com.google.common.annotations.VisibleForTesting;

public class ProjectController extends PhotoFlowController implements Initializable {
	
	private Project project;
	private FileHandler fileHandler;
	private List<Photo> photos = new ArrayList<Photo>();
	final ObservableList<Todo> todos = FXCollections.observableArrayList();

	/** Daemon threads for background task execution */
	private final ExecutorService imageLoaderService = newImageLoaderService();
	private final Collection<ImageLoadingTask> imageLoadingTasks = new HashSet<>();

	CheckComboBox<Todo> todoCheckComboBox;

	@FXML
	private PhotoController photoController;
	
	@FXML
	TextField projectNameField;
	@FXML
	Button newButton, importButton, editButton,finishButton, importPhotoButton, archiveProjectButton, exportProjectButton;
	@FXML
	Pane todoCheckComboBoxPane, separatorOne, separatorTwo, separatorThree;
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
		setWorkflowButtons();
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
		transitionState(this.project, ProjectState.IN_WORK);
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
			setWorkflowButtons();

			try {
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
			transitionState(this.project, ProjectState.ARCHIVED);
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
	
	public void setWorkflowButtons() {
		switch(project.getState()){
		case NEW:
			newButton.setDisable(false);
			newButton.getStyleClass().removeAll();
			newButton.getStyleClass().add("workflowButton");
			importButton.setDisable(true);
			importButton.getStyleClass().removeAll();
			importButton.getStyleClass().add("workflowButtonRed");
			editButton.setDisable(true);
			editButton.getStyleClass().removeAll();
			editButton.getStyleClass().add("workflowButtonRed");
			finishButton.setDisable(true);
			finishButton.getStyleClass().removeAll();
			finishButton.getStyleClass().add("workflowButtonRed");
			break;
		case IN_WORK:
			newButton.setDisable(true);
			newButton.getStyleClass().removeAll();
			newButton.getStyleClass().add("workflowButtonGreen");
			importButton.setDisable(false);
			importButton.getStyleClass().removeAll();
			importButton.getStyleClass().add("workflowButton");
			editButton.setDisable(false);
			editButton.getStyleClass().removeAll();
			editButton.getStyleClass().add("workflowButton");
			finishButton.setDisable(true);
			finishButton.getStyleClass().removeAll();
			finishButton.getStyleClass().add("workflowButtonRed");
			break;
		case ARCHIVED:
			newButton.setDisable(true);
			newButton.getStyleClass().removeAll();
			newButton.getStyleClass().add("workflowButtonGreen");
			importButton.setDisable(true);
			importButton.getStyleClass().removeAll();
			importButton.getStyleClass().add("workflowButtonGreen");
			editButton.setDisable(true);
			editButton.getStyleClass().removeAll();
			editButton.getStyleClass().add("workflowButtonGreen");
			finishButton.setDisable(true);
			finishButton.getStyleClass().removeAll();
			finishButton.getStyleClass().add("workflowButtonGreen");
			break;
		case DONE:
			newButton.setDisable(true);
			newButton.getStyleClass().removeAll();
			newButton.getStyleClass().add("workflowButtonGreen");
			importButton.setDisable(true);
			importButton.getStyleClass().removeAll();
			importButton.getStyleClass().add("workflowButtonGreen");
			editButton.setDisable(true);
			editButton.getStyleClass().removeAll();
			editButton.getStyleClass().add("workflowButtonGreen");
			finishButton.setDisable(false);
			finishButton.getStyleClass().removeAll();
			finishButton.getStyleClass().add("workflowButton");
			break;
		case PAUSED:
			newButton.setDisable(true);
			importButton.setDisable(true);
			editButton.setDisable(true);
			finishButton.setDisable(true);
			break;
		default:
			break;
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
		newButton.setDisable(true);
		editButton.setDisable(true);
		importButton.setDisable(true);
		finishButton.setDisable(true);

		importPhotoButton.setOnAction(this::importPhotos);
		archiveProjectButton.setOnAction(this::archiveProject);
		exportProjectButton.setOnAction(this::exportProject);
		
		initializeTodoCheckComboBox();
	}
	
	/**
	 * Initializes the CheckComboBox for Todo tasks.
	 */
	private void initializeTodoCheckComboBox() {
		
		//TestData
		Todo todo1 = new Todo("Test1");
		Todo todo2 = new Todo("Test2");
		Todo todo3 = new Todo("Test3");
		
		todos.add(todo1);
		todos.add(todo2);
		todos.add(todo3);
		
		todoCheckComboBox = new CheckComboBox<Todo>(todos);
		todoCheckComboBoxPane.getChildren().add(todoCheckComboBox);

		//Converter
		todoCheckComboBox.converterProperty().set(
				new StringConverter<Todo>() {

					@Override
					public Todo fromString(String string) {
						
						return null;
					}

					@Override
					public String toString(Todo object) {
						return object.getDescription();
					}
				}
		);
		
		//Listener Required for resizing until Issue is resolved:
		//https://bitbucket.org/controlsfx/controlsfx/issue/462/checkcombobox-ignores-prefwidth-maybe-any
		todoCheckComboBox.skinProperty().addListener((observable, oldValue, newValue) -> {
			if(oldValue==null && newValue!=null){
                CheckComboBoxSkin<Todo> skin = (CheckComboBoxSkin<Todo>)newValue;
                ComboBox<Todo> combo = (ComboBox<Todo>)skin.getChildren().get(0);
                combo.setPrefWidth(180.0);
                combo.setMaxWidth(Double.MAX_VALUE);
            }
		});
		
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
