package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.PhotoFlow;
import ch.zhaw.photoflow.core.domain.Project;

import com.google.common.annotations.VisibleForTesting;

public class MainController extends PhotoFlowController implements Initializable {

	/**
	 * Special project that acts as the "add new project" button.
	 */
	private static final Project ADD_NEW_PROJECT = Project.newProject(p -> p.setName("+ New Project"));
	
	private final ObservableList<Project> projects = FXCollections.observableArrayList();
	private ErrorHandler errorHandler = new ErrorHandler();

	@FXML
	private ProjectController projectController;
	
	@FXML
	private ListView<Project> projectList;

	@VisibleForTesting
	protected void setPhotoFlow(PhotoFlow photoFlow) {
		this.photoFlow = photoFlow;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadProjects();
		projectList.setItems(projects);
		projectList.setCellFactory(listView -> new ProjectCell());
		projectList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		projectList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				projectSelected(newValue);
			}
		});
		
		//Deletelistener
		projectList.setOnKeyPressed((keyevent) -> {
			Project project = projectList.getSelectionModel().getSelectedItem();

			if (KeyCode.DELETE.equals(keyevent.getCode()) || KeyCode.BACK_SPACE.equals(keyevent.getCode())) {
				deleteProject(project);
			} else if (ADD_NEW_PROJECT.equals(project) && KeyCode.ENTER.equals(keyevent.getCode())) {
				createProject();
			}
		});
		
		projectList.setOnMouseClicked((mouseEvent) ->{
			Project project = projectList.getSelectionModel().getSelectedItem();
			
			if (ADD_NEW_PROJECT.equals(project)) {
				createProject();
			}
		});
	}
	
	/**
	 * Loads all the saved projects and adds them to the local list
	 */
	private void loadProjects() {
		try {
			this.projects.clear();
			this.projects.add(ADD_NEW_PROJECT);
			this.projects.addAll(photoFlow.projectDao().loadAll());
		} catch (DaoException e) {
			e.printStackTrace();
			errorHandler.spawnError("Could not load project! Please try restart PhotoFlow!");
		}
	}
	
	private void createProject() {
		projectController.setProject(null);
		CreateProjectController popup = new CreateProjectController(); // This call blocks
		Project newProject = popup.getProject();
		if (newProject != null) {
			addProject(newProject);
			projectController.setProject(newProject);
			errorHandler.spawnInformation("Your new Project was successfully created!");
		} else {
			System.out.println("Canceled project creation.");
		}

		// Update selection
		Platform.runLater(() -> {
			projectList.getSelectionModel().select(popup.getProject());
		});
	}
	
	public void projectSelected(Project selectedProject) {
		if (selectedProject != ADD_NEW_PROJECT) {
			projectController.setProject(selectedProject);
		}
	}

	/**
	 * Adds a project using the ProjectDao. Also saves the project to the local
	 * list
	 * @param project the project to add
	 */
	public void addProject(Project project) {
		try {
			photoFlow.projectDao().save(project);
			this.projects.add(project);
			System.out.println(this.projects);
		} catch (DaoException e) {
			// TODO: Warn user
			throw new RuntimeException(e);
		}
	}

	/**
	 * Deletes a project within the ProjectDao an the local list
	 * @param project the project to be deleted
	 */
	public void deleteProject(Project project) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Löschbestätigung");
		alert.setHeaderText("Projekt und beinhaltende Bilder unwiderruflich löschen?");

		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() != ButtonType.YES){
			return;
		}
		
		try {
			photoFlow.fileHandler(project).deleteProject();
			photoFlow.projectDao().delete(project);
			projects.remove(project);
		} catch (DaoException e) {
			// TODO: Warn user
			e.printStackTrace();
		} catch (FileHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@VisibleForTesting
	protected List<Project> getProjects() {
		return this.projects;
	}

	private static final class ProjectCell extends ListCell<Project> {
		
		public ProjectCell() {
			// We could probably avoid overriding updateItem below by somehow binding itemProperty() to project.getName() here.
		}
		
		@Override
		public void updateItem(Project project, boolean empty) {
			super.updateItem(project, empty);

			textProperty().unbind();
			if (empty) {
				setText("");
				return;
			}
			
			if (project.equals(ADD_NEW_PROJECT)) {
				textProperty().bind(stringProperty(project, "name"));
			} else {
				StringProperty nameProperty = stringProperty(project, "name");
				ObjectProperty<?> stateProperty = objectProperty(project, "state");
				textProperty().bind(Bindings.format("%s (%s)", nameProperty, stateProperty));
			}
		}
	}

}
