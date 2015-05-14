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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
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
import ch.zhaw.photoflow.core.FileHandler;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.dao.DaoException;
import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;
import ch.zhaw.photoflow.core.domain.Todo;

import com.google.common.annotations.VisibleForTesting;

/**
 * Controller for {@link Project} specific interactions.
 */
public class ProjectController extends PhotoFlowController implements Initializable {
	
	private Project project;
	private FileHandler fileHandler;
	private List<Photo> photos = new ArrayList<Photo>();
	final ObservableList<Todo> todos = FXCollections.observableArrayList();
	
	/** Daemon threads for background task execution */
	private final ExecutorService imageLoaderService = newImageLoaderService();
	private final Collection<ImageLoadingTask> imageLoadingTasks = new HashSet<>();

	@FXML
	private PhotoController photoController;
	
	@FXML
	private TextField projectNameField;
	@FXML
	private Button newButton, archiveButton, editButton, finishButton, importPhotoButton, exportProjectButton, todoButton, pauseProjectButton;
	
	@FXML
	private TilePane photosPane;
	@FXML
	private Pane toolbar;
	
	private PopOver popOver;
	
	private final Map<Photo, Node> photoNodes = new HashMap<>();
	private Optional<Node> selectedImageNode = Optional.empty();
	private static final String SELECTED_STYLE = "selected-photo";
	private static final String DISCARDED_STYLE = "discarded-photo";
	private static final String FLAGGED_STYLE = "flagged-photo";

	/**
	 * Informs the {@link ProjectController} which project-content to display.
	 * @param project The project to display photos and other information.
	 */
	public void setProject(Project project) {
		if (this.project == project ) {
			return;
		}
		
		reset();
		
		this.project = project;
		System.out.println("Project has been selected: " + project);
		
		if (this.project != null) {
			try {
				fileHandler = photoFlow.fileHandler(this.project);
			} catch (FileHandlerException e) {
				throw new RuntimeException(e);
			}
			initializeProjectNameField();
			
			todos.addAll(this.project.getTodos());
			toolbar.setDisable(false);
			
			loadPhotos();
			displayPhotos();
		}
		updateWorkflowButtons();
	}
	
	/**
	 * Resets the {@link ProjectController}
	 */
	private void reset() {
		if (project != null) {
			projectNameField.textProperty().unbindBidirectional(stringProperty(project, "name"));
		}
		photoController.setPhoto(null);
		resetPhotosPane();
		todos.clear();
		toolbar.setDisable(true);
	}
	
	private void initializeProjectNameField() {
		projectNameField.textProperty().bindBidirectional(stringProperty(project, "name"));
	}
	
	/**
	 * Loads list of {@link Photo}.
	 */
	private void loadPhotos() {
		try {
			List<Photo> loadedPhotos = photoFlow.photoDao().loadAll(project.getId().get());
			this.photos.clear();
			this.photos.addAll(loadedPhotos);
		} catch (DaoException e) {
			EventHandler.spawnError("Something went wrong :-( Please try again!");
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Display loaded photos from path {@link Photo#getFilePath()}.
	 */
	private void displayPhotos() {
		resetPhotosPane();
		
		for (Photo photo : photos) {
			ImageLoadingTask task = new ImageLoadingTask(photo, fileHandler);
			
			task.valueProperty().addListener((observable, old, imageNode) -> {
				photoNodes.put(photo, imageNode);
				
				imageNode.setOnKeyPressed((keyevent) -> {
					selectPhoto(photo);

					if (KeyCode.DELETE.equals(keyevent.getCode()) || KeyCode.BACK_SPACE.equals(keyevent.getCode())) {
						deletePhoto(photo);
					}
				});
				
				imageNode.setOnMouseClicked((event) -> {
					selectPhoto(photo);
					if (event.getClickCount() == 2) {
						new ImageViewer(photo, fileHandler);
					}
				});
				
				photosPane.getChildren().add(imageNode);
				imageLoadingTasks.remove(task);
			});
			
			task.onFailedProperty().addListener((observale, a, b) -> {
				System.out.println("Loading photo failed: " + photo);
				EventHandler.spawnError("Darn! We could not load your Image "+photo.getFilePath());
				imageLoadingTasks.remove(task);
			});
			
			imageLoadingTasks.add(task);
			imageLoaderService.execute(task);
		}
	}
	
	/**
	 * Resets the pane displaying pictures.
	 */
	private void resetPhotosPane() {
		// Cancel all previous tasks
		imageLoadingTasks.forEach(task -> task.cancel(true));
		imageLoadingTasks.clear();
		
		photosPane.getChildren().clear();
		photoNodes.clear();
	}
	
	/**
	 * @param photo Photo to select.
	 */
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
	
	/**
	 * Use for multithreading. We don't want the GUI to wait for every task.
	 */
	private ExecutorService newImageLoaderService () {
		
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), runnable -> {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * Adds a {@link Photo} to the selected {@link Project}.
	 * Photo will be saved.
	 * @param photo
	 */
	public void addPhoto(Photo photo) {
		photos.remove(photo);
		photo.setProjectId(this.project.getId().get());
		try {
			photoFlow.photoDao().save(photo);
			photos.add(photo);
		} catch (DaoException e) {
			EventHandler.spawnError("We are so sorry. We tried really hard to add your photo to the project, but we failed...miserably!");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Imports pictures and saves {@link Photo} objects.
	 * @param event
	 */
	public void importPhotos(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import Photos");
		
		List<String> extensions = FileFormat.getAllFileExtensions().stream().map(e -> "*." + e).collect(Collectors.toList());
		ExtensionFilter imageFilter = new ExtensionFilter("Image Files", extensions);
		fileChooser.getExtensionFilters().add(imageFilter);

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(importPhotoButton.getScene().getWindow());
		
		if (selectedFiles == null) {
			EventHandler.spawnWarning("You have not selected any photos, have you?");
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
				EventHandler.spawnError("Your File is already imported. Please select another one, will you?");
				throw new RuntimeException(e);
			} catch (DaoException e) {
				EventHandler.spawnError("Sorry! Something went wrong in saving your Photo. Please try again!");
				throw new RuntimeException(e);
			}
			
		}
		Notifications.create()
			.darkStyle()
			.title("Success")
			.text("All your photos are belong to us!")
			.showInformation();
		displayPhotos();
		if(photoFlow.projectWorkflow().canTransition(project, this.photos, ProjectState.IN_WORK)){
			transitionState(this.project, ProjectState.IN_WORK);
		}
		updateWorkflowButtons();
	}

	/**
	 * Deletes a {@link Photo}.
	 * @param photo
	 */
	public void deletePhoto(Photo photo) {
		try {
			photoFlow.photoDao().delete(photo);
			fileHandler.deletePhoto(photo);
			photos.remove(photo);
		} catch (FileHandlerException | DaoException e) {
			EventHandler.spawnError("Your photo could not be deleted. Guess a 'sorry' would be appropriate...");
			throw new RuntimeException(e);
		}
		displayPhotos();
		updateWorkflowButtons();
	}

	/**
	 * Sets the state of the specified @{link Project} object to the given
	 * projectState.
	 * 
	 * @param project The project to transition.
	 * @param projectState The state to transition to.
	 */
	protected void transitionState(Project project, ProjectState projectState) {
		if (photoFlow.projectWorkflow().canTransition(project, this.photos, projectState)) {
			photoFlow.projectWorkflow().transition(project, this.photos, projectState);
			// Manually update bindings.
			objectProperty(project, "state").fireValueChangedEvent();
			try {
				photoFlow.projectDao().save(project);
			} catch (DaoException e) {
				EventHandler.spawnError("Something went wrong in saving Project!");
			}
			saveProject();
		} else {
			EventHandler.spawnError("Your Project is not in a correct State!");
			throw new IllegalStateException("Your Project is not in a correct State!");
		}
		updateWorkflowButtons();
	}
	
	/**
	 * Saves a {@link Project}
	 */
	private void saveProject() {
		System.out.println("Saving project: " + project);
		try {
			photoFlow.projectDao().save(project);
		} catch (DaoException e) {
			EventHandler.spawnError("Something went wrong in saving Project!");
			throw new RuntimeException(e);
		}
		updateWorkflowButtons();
	}

	/**
	 * Changes the state of the selected {@link Project} to {@link ProjectState#ARCHIVED} if valid.
	 * @param event
	 */
	public void archiveProject(ActionEvent event) {
		try {
			fileHandler.archiveProject();
			transitionState(this.project, ProjectState.ARCHIVED);
			EventHandler.spawnInformation("Project was archived using cryo tech!");
		} catch (FileHandlerException e) {
			EventHandler.spawnError("Project could not be archived!");
			throw new RuntimeException(e);
		}
		updateWorkflowButtons();
	}
	
	/**
	 * Exports a Project to a ZIP-File.
	 * @param event UI event.
	 */
	public void exportProject(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export Project (ZIP)");
		
		ExtensionFilter imageFilter = new ExtensionFilter("Zip Files", "*.zip");
		fileChooser.getExtensionFilters().add(imageFilter);
		fileChooser.setInitialFileName(project.getName()+".zip");
		
		File selectedFile = fileChooser.showSaveDialog(exportProjectButton.getScene().getWindow());
		
		if (selectedFile != null) {
			try {
			fileHandler.exportZip(selectedFile.getAbsolutePath(), photos);
			} catch (FileHandlerException e) {
				EventHandler.spawnError("Project could not be exported!");
				throw new RuntimeException(e);
			}
		} else {
			EventHandler.spawnWarning("You have not selected a destination Folder, have you?");
			return;
		}
		EventHandler.spawnInformation("Your files were being transfered to new location: " + selectedFile);
	}
	
	/**
	 * Changes the state of the selected {@link Project} to {@link ProjectState#PAUSED} if valid.
	 * @param event
	 */
	public void pauseProject(ActionEvent event) {
		pauseProjectButton.getStyleClass().removeAll();
		if (project.getState() != ProjectState.PAUSED) {
			transitionState(this.project, ProjectState.PAUSED);
			EventHandler.spawnInformation("Project paused! To continue click Button again.");
			pauseProjectButton.getStyleClass().add("playProjectButton");
		} else {
			transitionState(this.project, ProjectState.IN_WORK);
			EventHandler.spawnInformation("Project continues...");
			pauseProjectButton.getStyleClass().add("pauseProjectButton");
		}
	}
	
	/**
	 * Changes the state of the selected {@link Project} to {@link ProjectState#IN_WORK} if valid.
	 * @param event
	 */
	public void editProject(ActionEvent event){
		if (photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.IN_WORK) && !photos.isEmpty()) {
			if (project.getState() == ProjectState.ARCHIVED) {
				try {
					fileHandler.unArchiveProject();
				} catch (FileHandlerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			transitionState(project, ProjectState.IN_WORK);
		} else {
			EventHandler.spawnWarning("State cannot be changed! Be sure you have imported any Photos");
		}
		updateWorkflowButtons();
	}
	
	/**
	 * Changes the state of the selected {@link Project} to {@link ProjectState#DONE} if valid.
	 * @param event
	 */
	public void finishProject(ActionEvent event){
		if (photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.DONE)) {
			transitionState(project, ProjectState.DONE);
		} else {
			EventHandler.spawnWarning("Finishing Project not possible! Every Photo must be flagged or discarded!");
		}
		updateWorkflowButtons();
	}
	
	/**
	 * Updates the workflow button designs to indicate the actual and possible future workflow state.
	 */
	public void updateWorkflowButtons() {
		if (project != null) {
			String green = "workflowButtonGreen";
			String play = "playProjectButton";
			String pause = "pauseProjectButton";

			newButton.setEffect(null);
			newButton.getStyleClass().remove(green);
			editButton.setEffect(null);
			editButton.getStyleClass().remove(green);
			finishButton.setEffect(null);
			finishButton.getStyleClass().remove(green);
			archiveButton.setEffect(null);
			archiveButton.getStyleClass().remove(green);
			pauseProjectButton.getStyleClass().remove(play);
			pauseProjectButton.getStyleClass().remove(pause);

			if (project.getState() == ProjectState.PAUSED) {
				projectNameField.setDisable(true);

				newButton.setDisable(true);
				archiveButton.setDisable(true);
				editButton.setDisable(true);
				finishButton.setDisable(true);

				importPhotoButton.setDisable(true);
				exportProjectButton.setDisable(true);
				todoButton.setDisable(true);
				pauseProjectButton.setDisable(false);
				pauseProjectButton.getStyleClass().add(play);
				photoController.reset();
			} else {
				projectNameField.setDisable(false);

				newButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.NEW));
				editButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.IN_WORK));
				finishButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.DONE));
				archiveButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.ARCHIVED));

				todoButton.setDisable(false);
				pauseProjectButton.setDisable(!photoFlow.projectWorkflow().canTransition(project, photos, ProjectState.PAUSED));
				pauseProjectButton.getStyleClass().add(pause);

				switch (project.getState()) {
				case NEW:
					importPhotoButton.setDisable(false);
					newButton.setEffect(new DropShadow(10, Color.YELLOWGREEN));
					break;
				case IN_WORK:
					projectNameField.setDisable(false);
					importPhotoButton.setDisable(false);
					newButton.getStyleClass().add(green);
					pauseProjectButton.getStyleClass().add(pause);
					editButton.setEffect(new DropShadow(10, Color.YELLOWGREEN));
					break;
				case DONE:
					exportProjectButton.setDisable(false);
					projectNameField.setDisable(true);
					importPhotoButton.setDisable(true);
					newButton.getStyleClass().add(green);
					editButton.getStyleClass().add(green);
					finishButton.getStyleClass().add(green);
					finishButton.setEffect(new DropShadow(10, Color.YELLOWGREEN));
					photoController.reset();
					break;
				case ARCHIVED:
					projectNameField.setDisable(true);
					exportProjectButton.setDisable(true);
					importPhotoButton.setDisable(true);
					newButton.getStyleClass().add(green);
					editButton.getStyleClass().add(green);
					finishButton.getStyleClass().add(green);
					archiveButton.getStyleClass().add(green);
					archiveButton.setEffect(new DropShadow(10, Color.YELLOWGREEN));
					photoController.reset();
					break;
				default:
					break;
				}
			}
		} else {
			projectNameField.setText("");
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		projectNameField.setText("new Project");
		
		initializeTooltips();
		initializePhotoListener();
		
		projectNameField.setDisable(true);
		newButton.setDisable(true);
		editButton.setDisable(true);
		finishButton.setDisable(true);
		archiveButton.setDisable(true);

		editButton.setOnAction(this::editProject);
		finishButton.setOnAction(this::finishProject);
		archiveButton.setOnAction(this::archiveProject);
		importPhotoButton.setOnAction(this::importPhotos);
		exportProjectButton.setOnAction(this::exportProject);
		pauseProjectButton.setOnAction(this::pauseProject);
		
		initializeTodoButton();
		initializeTodoPopOver();
		
		projectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
			saveProject();
		});
	}
	
	/**
	 * Initializes workflow buttons to use {@link Tooltip}.
	 */
	private void initializeTooltips() {
		exportProjectButton.setTooltip(new Tooltip("Export Project"));
		importPhotoButton.setTooltip(new Tooltip("Import a new Photo"));
		pauseProjectButton.setTooltip(new Tooltip("Pause/Resume Project"));
	}
	
	/**
	 * Initializes PhotoListener.
	 */
	private void initializePhotoListener() {
		photoController.setListener(new PhotoListener() {
			
			@Override
			public void flagPhoto(Photo photo) {
				if (photoFlow.photoWorkflow().canFlag(project, photo)) {
					photoFlow.photoWorkflow().flag(project, photo);
					
					photoNodes.get(photo).getStyleClass().remove(DISCARDED_STYLE);
					photoNodes.get(photo).getStyleClass().add(FLAGGED_STYLE);
					
					updateWorkflowButtons();
					
					System.out.println("Photo flagged: " + photo);
				} else {
					throw new IllegalStateException("Button should have been disabled.");
				}
			}
			
			@Override
			public void discardPhoto(Photo photo) {
				if (photoFlow.photoWorkflow().canDiscard(project, photo)) {
					photoFlow.photoWorkflow().discard(project, photo);
					
					photoNodes.get(photo).getStyleClass().remove(FLAGGED_STYLE);
					photoNodes.get(photo).getStyleClass().add(DISCARDED_STYLE);
					
					updateWorkflowButtons();
					
					System.out.println("Photo discarded: " + photo);
				} else {
					throw new IllegalStateException("Button should have been disabled.");
				}
			}

			@Override
			public void editPhoto(Photo photo) {
				try {
					String executable;
					String projectPath = fileHandler.projectDir().getAbsolutePath();
					if (isWindows()) {
						System.out.println("Opening Windows Windows Explorer");
						executable = "explorer";
						projectPath = projectPath.replace("/", "\\");
					} else if (isMac()) {
						System.out.println("Opening OS X Finder");
						executable = "open ";
					} else {
						EventHandler.spawnError("Editting is not yet supported on your OS. Please buy a Mac :-P");
						return;
					}
					Runtime.getRuntime().exec(executable + " " + projectPath);
				} catch (FileHandlerException | IOException e) {
					EventHandler.spawnError("Could not open file browser");
					throw new RuntimeException(e);
				}
				
				EventHandler.spawnInformation("Opening File explorer to edit your photo");
			}

			@Override
			public void deletePhoto(Photo photo) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Delete Confirmation");
				alert.setHeaderText("Are you sure you want to delete this Photo?");

				alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() != ButtonType.YES){
					return;
				}	
				try {
					photoFlow.photoDao().delete(photo);
					fileHandler.deletePhoto(photo);
					photos.remove(photo);
				} catch (FileHandlerException | DaoException e) {
					EventHandler.spawnError("Delete Photo not possible.");
					throw new RuntimeException(e);
				}
				displayPhotos();
				updateWorkflowButtons();
			}
		});
	}
	
	private boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win");
	}
	
	private boolean isMac() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("mac");
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
	
	/**
	 * Creates the content for managing {@link Todo}s
	 * @return
	 */
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
							} catch (DaoException e) {
								throw new RuntimeException(e);
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
				} catch (DaoException e) {
					throw new RuntimeException(e);
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
					project.addTodo(todo);
					todos.add(todo);
					descriptionTextField.setText("");
				} catch (DaoException e) {
					throw new RuntimeException(e);
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

	/**
	 * Task for reading a picture from the filesystem and creates a {@link Pane} for displaying pictures.
	 */
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
