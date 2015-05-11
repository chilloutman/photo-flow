package ch.zhaw.photoflow.controller;

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

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;

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
	private ErrorHandler errorHandler = new ErrorHandler();
	
	/** Daemon threads for background task execution */
	private final ExecutorService imageLoaderService = newImageLoaderService();
	private final Collection<ImageLoadingTask> imageLoadingTasks = new HashSet<>();

	@FXML
	private PhotoController photoController;
	
	@FXML
	TextField projectNameField;
	@FXML
	Button newButton, importButton, editButton,finishButton, importPhotoButton, archiveProjectButton, exportProjectButton, todoButton, pauseProjectButton;
	@FXML
	Pane todoCheckComboBoxPane, separatorOne, separatorTwo, separatorThree;
	@FXML
	TilePane photosPane;
	
	PopOver popOver;
	
	private final Map<Photo, Node> photoNodes = new HashMap<>();
	private Optional<Node> selectedImageNode = Optional.empty();
	private static final String SELECTED_STYLE = "selected-photo";
	private static final String DISCARDED_STYLE = "discarded-photo";
	private static final String FLAGGED_STYLE = "flagged-photo";

	public void setProject(Project project) {
		if (this.project == project ) {
			return;
		}
		
		System.out.println("Project \"" + project.getName() + "\" has been selected.");
		if (this.project != null) {
			projectNameField.textProperty().unbindBidirectional(stringProperty(this.project, "name"));
		}
		this.project = project;
		initializeProjectNameField();
		
		try {
			fileHandler = photoFlow.fileHandler(project);
		} catch (FileHandlerException e) {
			throw new RuntimeException(e);
		}
		
		this.todos.clear();
		this.todos.addAll(project.getTodos());
		
		loadPhotos();
		displayPhotos();
		updateWorkflowButtons();
	}
	
	private void initializeProjectNameField() {
		projectNameField.textProperty().bindBidirectional(stringProperty(this.project, "name"));
		projectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
			saveProject();
		});
	}
	
	private void loadPhotos() {
		try {
			List<Photo> loadedPhotos = photoFlow.photoDao().loadAll(project.getId().get());
			this.photos.clear();
			this.photos.addAll(loadedPhotos);
		} catch (DaoException e) {
			errorHandler.spawnError("Something went wrong :-( Please try again!");
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
				errorHandler.spawnError("Darn! We could not load your Image "+photo.getFilePath());
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
			errorHandler.spawnError("We are so sorry. We tried really hard to add your photo to the project, but we failed...miserably!");
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
			errorHandler.spawnWarining("You have not selected any photos, have you?");
			return;
		}
		
		for (File file : selectedFiles) {
			Photo photo = Photo.newPhoto( p -> {
				p.setProjectId(project.getId().get());
			});
			
			try {
				fileHandler.importPhoto(photo, file);
				photoFlow.photoDao().save(photo);
				photos.add(photo);
			} catch (FileHandlerException e) {
				System.out.println("FILEHANDLEREXCEPTION");
				errorHandler.spawnError("Could not load your Photo. Maybe it was deleted from your filesystem...");
				e.printStackTrace();
			} catch (DaoException e) {
				System.out.println("DAOEXCEPTION");
				e.printStackTrace();
				// TODO Inform User (DAO)
			}
			
		}
		Notifications.create()
		.darkStyle()
        .title("Success")
        .text("All your photos are belong to us!")
        .showInformation();
		displayPhotos();
		transitionState(this.project, ProjectState.IN_WORK);
	}

	public void deletePhoto(Photo photo) {
		try {
			photoFlow.photoDao().delete(photo);
			photos.remove(photo);
		} catch (DaoException e) {
			errorHandler.spawnError("Your photo could not be deleted. Guess a 'sorry' would be appropriate...");
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
			try {
				photoFlow.projectDao().save(project);
			} catch (DaoException e) {
				// TODO: Inform user that saving failed
			}
			saveProject();
		} else {
			errorHandler.spawnError("Your Project is not in a correct State!");
			throw new RuntimeException();
		}
		updateWorkflowButtons();
	}
	
	private void saveProject() {
		System.out.println("Saving project: " + project);
		try {
			photoFlow.projectDao().save(project);
		} catch (DaoException e) {
			// TODO display erroe message.
			throw new RuntimeException(e);
		}
		updateWorkflowButtons();
	}

	public void archiveProject(ActionEvent event) {
		try {
			fileHandler.archiveProject();
			transitionState(this.project, ProjectState.ARCHIVED);
			errorHandler.spawnInformation("Project was archived using cryo tech!");
		} catch (FileHandlerException e) {
			// TODO Auto-generated catch block.
			throw new RuntimeException(e);
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
				throw new RuntimeException(e);
			}
		}
		errorHandler.spawnInformation("Your files were being transfered to new location: "+selectedFile.getAbsolutePath());
	}
	
	public void pauseProject(ActionEvent event) {
		pauseProjectButton.getStyleClass().removeAll();
		if(project.getState() != ProjectState.PAUSED){
			transitionState(this.project, ProjectState.PAUSED);
			errorHandler.spawnInformation("Project paused! To continue click Button again.");
			pauseProjectButton.getStyleClass().add("playProjectButton");
		}else{
			transitionState(this.project, ProjectState.IN_WORK);
			errorHandler.spawnInformation("Project continues...");
			pauseProjectButton.getStyleClass().add("pauseProjectButton");
		}
	}
	
	public void updateWorkflowButtons() {
		newButton.getStyleClass().removeAll();
		importButton.getStyleClass().removeAll();
		editButton.getStyleClass().removeAll();
		finishButton.getStyleClass().removeAll();
		pauseProjectButton.getStyleClass().removeAll();
		
		newButton.getStyleClass().add("workflowButtonRed");
		importButton.getStyleClass().add("workflowButtonRed");
		editButton.getStyleClass().add("workflowButtonRed");
		finishButton.getStyleClass().add("workflowButtonRed");
		pauseProjectButton.getStyleClass().add("pauseProjectButton");
		
		if(project.getState() == ProjectState.PAUSED){
			newButton.setDisable(true);
			importButton.setDisable(true);
			editButton.setDisable(true);
			finishButton.setDisable(true);
			archiveProjectButton.setDisable(true);
			exportProjectButton.setDisable(true);
			todoButton.setDisable(true);
			pauseProjectButton.setDisable(false);
		}else {
			newButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.NEW));
			importButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.IN_WORK));
			editButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.IN_WORK));
			finishButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.DONE));
			importPhotoButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.IN_WORK));
			exportProjectButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.ARCHIVED));
			archiveProjectButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.ARCHIVED));
			todoButton.setDisable(false);
			pauseProjectButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.PAUSED));
		}
		
		switch(project.getState()){
		case NEW:
			newButton.getStyleClass().add("workflowButtonGreen");
			break;
		case IN_WORK:
			newButton.getStyleClass().add("workflowButtonGreen");
			importButton.getStyleClass().add("workflowButton");
			editButton.getStyleClass().add("workflowButton");
			pauseProjectButton.getStyleClass().add("pauseProjectButton");
			break;
		case ARCHIVED:
			newButton.getStyleClass().add("workflowButtonGreen");
			importButton.getStyleClass().add("workflowButtonGreen");
			editButton.getStyleClass().add("workflowButtonGreen");
			finishButton.getStyleClass().add("workflowButtonGreen");
			break;
		case DONE:
			newButton.getStyleClass().add("workflowButtonGreen");
			importButton.getStyleClass().add("workflowButtonGreen");
			editButton.getStyleClass().add("workflowButtonGreen");
			finishButton.getStyleClass().add("workflowButton");
			break;
		case PAUSED:
			pauseProjectButton.getStyleClass().add("playProjectButton");
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
		pauseProjectButton.setOnAction(this::pauseProject);
		
		initializeTodoButton();
		initializeTodoPopOver();
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
	
	private void initializeTodoButton() {
		todoButton.setOnAction(event -> {
			popOver.show(todoButton);
		});
	}
	
	private void initializeTodoPopOver() {
	popOver = new PopOver();
	popOver.autoHideProperty().set(true);
	popOver.detachedTitleProperty().set("Todos");
	
	popOver.setContentNode(createPopOverContent());
}
	
	private VBox createPopOverContent() {
		VBox rootBox = new VBox();
		
		Label todosLabel = new Label("Todos");
		todosLabel.setTextFill(Color.BLACK);
		todosLabel.setContentDisplay(ContentDisplay.CENTER);
		rootBox.getChildren().add(todosLabel);
		
		HBox content = new HBox();
		
		//List
		ListView<Todo> listViewTodo = new ListView<Todo>(todos);
		
		//Custom Cellfactory
		listViewTodo.setCellFactory(column -> {
			return new ListCell<Todo>() {
				
				@Override
				public void updateItem(Todo todo, boolean empty) {
					super.updateItem(todo, empty);

					if (todo == null || empty) {
						setGraphic(null);
					}
					else {
						CheckBox checkBox = new CheckBox(todo.getDescription());
						BooleanProperty checkedProperty = booleanProperty(todo, "checked");
						checkedProperty.addListener((observable, oldvalue, newvalue) -> {
							try {
								photoFlow.projectDao().saveTodo(project, todo);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
						checkBox.selectedProperty().bindBidirectional(checkedProperty);

						setGraphic(checkBox);
					}
				}
				
			};
		});
		
		//Delete
		listViewTodo.setOnKeyPressed(keyevent -> {
			if (KeyCode.DELETE.equals(keyevent.getCode())) {
				Todo selectedTodo = listViewTodo.getSelectionModel().getSelectedItem();
				try {
					photoFlow.projectDao().deleteTodo(selectedTodo);
					todos.remove(selectedTodo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		content.getChildren().add(listViewTodo);
		
		//Detail
		VBox editBox = new VBox();
		Label descriptionLabel = new Label("Description");
		descriptionLabel.setTextFill(Color.BLACK);
		
		TextField descriptionTextField = new TextField();
		
		descriptionTextField.setOnKeyPressed(keyevent -> {
			if (KeyCode.ENTER.equals(keyevent.getCode())) {
				Todo todo = new Todo(descriptionTextField.getText());
				try {
					photoFlow.projectDao().saveTodo(project, todo);
					todos.add(todo);
					descriptionTextField.setText("");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		//GUI Creation
		editBox.getChildren().add(descriptionLabel);
		editBox.getChildren().add(descriptionTextField);
		
		content.getChildren().add(editBox);
		rootBox.getChildren().add(content);
		
		return rootBox;
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
