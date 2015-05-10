package ch.zhaw.photoflow.controller;

import impl.org.controlsfx.skin.CheckComboBoxSkin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.Notifications;

import ch.zhaw.photoflow.controller.PhotoController.PhotoListener;
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
	private PopUpHandler popup;
	private ImageViewer imageViewer;
	
	/** Daemon threads for background task execution */
	private final ExecutorService imageLoaderService = newImageLoaderService();
	private final Collection<ImageLoadingTask> imageLoadingTasks = new HashSet<>();

	private CheckComboBox<Todo> todoCheckComboBox;

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
	
	private final Map<Photo, Node> photoNodes = new HashMap<>();
	private Optional<Node> selectedImageNode = Optional.empty();
	private static final String SELECTED_STYLE = "selected-photo";
	private static final String DISCARDED_STYLE = "discarded-photo";
	private static final String FLAGGED_STYLE = "flagged-photo";

	public void setProject(Project project) {
		if (this.project == project ) {
			return;
		}
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
		updateWorkflowButtons();
	}
	
	private void loadPhotos() {
		try {
			List<Photo> loadedPhotos = photoFlow.photoDao().loadAll(project.getId().get());
			this.photos.clear();
			this.photos.addAll(loadedPhotos);
		} catch (DaoException e) {
			Notifications.create()
			.darkStyle()
            .title("Oops")
            .text("Something went wrong :-( Please try again!")
            .showError();
			throw new RuntimeException(e);
		}
	}
	
	private void displayPhotos() {
		resetPhotosPane();
		
		for (Photo photo : photos) {
			ImageLoadingTask task = new ImageLoadingTask(photo, fileHandler);
			
			task.valueProperty().addListener((observable, old, imageNode) -> {
				photoNodes.put(photo, imageNode);
				
				imageNode.setOnMouseClicked((event) -> {
					selectPhoto(photo);
					if (event.getClickCount() == 2) {
						imageViewer = new ImageViewer(photo, fileHandler);
					}
				});

				photosPane.getChildren().add(imageNode);
				imageLoadingTasks.remove(task);
			});
			
			task.onFailedProperty().addListener((observale, a, b) -> {
				System.out.println("Loading photo failed: " + photo);
				Notifications.create()
				.darkStyle()
	            .title("Cancel")
	            .text("Darn! We could not load your Image "+photo.getFilePath())
	            .showError();
				imageLoadingTasks.remove(task);
			});
			
			imageLoadingTasks.add(task);
			imageLoaderService.execute(task);
		}
	}
	
	private void resetPhotosPane() {
		// Cancel all previous tasks
		imageLoadingTasks.forEach(task -> task.cancel(true));
		imageLoadingTasks.clear();
		
		photosPane.getChildren().clear();
		photoNodes.clear();
	}
	
	private void selectPhoto(Photo photo) {
		Node imageNode = photoNodes.get(photo);
		
		// Apply selected style
		selectedImageNode.ifPresent(v -> v.getStyleClass().remove(SELECTED_STYLE));
		imageNode.getStyleClass().add(SELECTED_STYLE);
		
		// Update photo detail view
		photoController.setPhoto(photo);
		
		// Store this so that we can remove the styling next time.
		selectedImageNode = Optional.of(imageNode);
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
			Notifications.create()
			.darkStyle()
            .title("Error")
            .text("We are so sorry. We tried really hard to add your photo to the project, but we failed...miserably!")
            .showError();
			throw new RuntimeException(e);
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
			Notifications.create()
			.darkStyle()
            .title("Warning")
            .text("You have not selected any photos, have you?")
            .showWarning();
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
				Notifications.create()
				.darkStyle()
	            .title("Success")
	            .text("All your photos are belong to us!")
	            .showInformation();
			} catch (FileHandlerException e) {
				System.out.println("FILEHANDLEREXCEPTION");
				Notifications.create()
				.darkStyle()
	            .title("Error")
	            .text("Could not load your Photo. Maybe it was deleted from your filesystem...")
	            .showError();
				e.printStackTrace();
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
			Notifications.create()
			.darkStyle()
            .title("Error")
            .text("Your photo could not be deleted. Guess a 'sorry' would be appropriate...")
            .showError();
			throw new RuntimeException(e);
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
			updateWorkflowButtons();

			try {
				photoFlow.projectDao().save(project);
			} catch (DaoException e) {
				// TODO: Inform user that saving failed
			}
		} else {
			// Inform User
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
	
	public void updateWorkflowButtons() {
		// TODO: Use Project Workflow for this!
		// canTransition() can be used to determine which button should be neabled.
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
		projectNameField.setText("new Project");
		
		initializeTooltips();
		initializePhotoListener();
		
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
	
	private void initializeTooltips() {
		exportProjectButton.setTooltip(new Tooltip("Export Project"));
		archiveProjectButton.setTooltip(new Tooltip("Archive Project"));
		importPhotoButton.setTooltip(new Tooltip("Import a new Photo"));
	}
	
	private void initializePhotoListener() {
		photoController.setListener(new PhotoListener() {
			
			@Override
			public void flagPhoto(Photo photo) {
				if (photoFlow.photoWorkflow().canFlag(project, photo)) {
					photoFlow.photoWorkflow().flag(project, photo);
					
					photoNodes.get(photo).getStyleClass().remove(DISCARDED_STYLE);
					photoNodes.get(photo).getStyleClass().add(FLAGGED_STYLE);
					
					System.out.println("Photo flagged: " + photo);
				} else {
					// TODO Button should have been disabled.
				}
			}
			
			@Override
			public void discardPhoto(Photo photo) {
				if (photoFlow.photoWorkflow().canDiscard(project, photo)) {
					photoFlow.photoWorkflow().discard(project, photo);
					
					photoNodes.get(photo).getStyleClass().remove(FLAGGED_STYLE);
					photoNodes.get(photo).getStyleClass().add(DISCARDED_STYLE);
					
					System.out.println("Photo discarded: " + photo);
				} else {
					// TODO Button should have been disabled.
				}
			}
		});
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
				@SuppressWarnings("unchecked")
				CheckComboBoxSkin<Todo> skin = (CheckComboBoxSkin<Todo>) newValue;
				@SuppressWarnings("unchecked")
				ComboBox<Todo> combo = (ComboBox<Todo>) skin.getChildren().get(0);
				combo.setPrefWidth(180.0);
				combo.setMaxWidth(Double.MAX_VALUE);
			}
		});
		
	}

	private static class ImageLoadingTask extends Task<Pane> {
		
		private final Photo photo;
		private final FileHandler fileHandler;
		
		public ImageLoadingTask(Photo photo, FileHandler fileHandler) {
			this.photo = photo;
			this.fileHandler = fileHandler;
		}

		@Override
		protected Pane call() throws FileHandlerException, IOException {
			//System.out.println("Loading photo: " + photo);
			try (InputStream file = fileHandler.loadPhoto(photo)) {
				Image image = new Image(file, 200, 200, true, true);
				
				// Wrap in pane so that we can apply styles.
				ImageView imageView = new ImageView(image);
				StackPane pane = new StackPane(imageView);
				StackPane.setAlignment(imageView, Pos.CENTER);
				
				// Set styles
				if (PhotoState.DISCARDED.equals(photo.getState())) {
					pane.getStyleClass().add(DISCARDED_STYLE);
				} else if (PhotoState.FLAGGED.equals(photo.getState())) {
					pane.getStyleClass().add(FLAGGED_STYLE);
				}
				
				return pane;
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
