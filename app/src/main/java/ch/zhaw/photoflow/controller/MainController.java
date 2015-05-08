package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
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
import javafx.scene.layout.Pane;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.FileHandlerException;
import ch.zhaw.photoflow.core.PhotoFlow;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class MainController extends AbstractController implements Initializable {

	/**
	 * Special project that acts as the "add new project" button.
	 */
	private final Project ADD_NEW_PROJECT = Project.newProject(p -> p.setName("+ New Project"));
	
	@Inject
	private PhotoFlow photoFlow;
	
	private final ObservableList<Project> projects = FXCollections.observableArrayList();
	private PopUpHandler popup;

	@FXML
	private ProjectController projectController;
	
	@FXML
	private Pane project;

	@FXML
	private ListView<Project> projectList;

	private String projectName;
	private String projectDescription;
	private List<Tag> tags = new ArrayList<>();
	
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
			projectSelected(newValue);
		});
		
		//Deletelistener
		projectList.setOnKeyPressed((keyevent) -> {
			Project project = projectList.getSelectionModel().getSelectedItem();

			if (KeyCode.DELETE.equals(keyevent.getCode())) {
				deleteProject(project);
			} else if (ADD_NEW_PROJECT.equals(project) && KeyCode.ENTER.equals(keyevent.getCode())) {
				createProjectFromEvents();
			}
		});
		
		projectList.setOnMouseClicked((mouseEvent) ->{
			Project project = projectList.getSelectionModel().getSelectedItem();
			
			if (ADD_NEW_PROJECT.equals(project)) {
				createProjectFromEvents();
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
			// TODO error handling
		}
	}
	
	private void createProjectFromEvents()
	{
		project.setDisable(true);
		popup = new PopUpHandler();
		Optional<Project> newProject = createProject();

		// Update selection
		Platform.runLater(() -> {
			projectList.getSelectionModel().select(newProject.orElse(null));
		});
	}
	
	public void projectSelected(Project selectedProject) {
		if (selectedProject == ADD_NEW_PROJECT) {
//			// TODO: Could we not block the UI here and use a listener instead?
//			projectController.setDisable(true);
//			popup = new PopUpHandler();
//			Optional<Project> newProject = createProject();
//
//			// Update selection
//			Platform.runLater(() -> {
//				projectList.getSelectionModel().select(newProject.orElse(null));
//			});
		} else {
			project.setDisable(false);
			projectController.setProject(selectedProject);
		}
	}

	/**
	 * Processes stuff for object {@link Project} and adds to list.
	 */
	public Optional<Project> createProject() {
		// todo tag handling
		if (popup.getName() != null) {
			setProjectName(popup.getName());
			setProjectDescription(popup.getDesc());
			tags = popup.getTags();

			System.out.println("Tags = " + tags);

			Project project = Project.newProject(p -> {
				p.setName(projectName);
				p.setDescription(projectDescription);
				p.setTags(tags);
			});
			addProject(project);
			System.out.println("project created");
			return Optional.of(project);
		} else {
			System.out.println("canceled project creation");
			return Optional.empty();
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

		if (result.get() != ButtonType.OK){
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

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	private static final class ProjectCell extends ListCell<Project> {
		
		public ProjectCell() {
			// We could probably avoid overriding updateItem below by somehow binding itemProperty() to project.getName() here.
		}
		
		@Override
		public void updateItem(Project project, boolean empty) {
			super.updateItem(project, empty);

			textProperty().unbind();
			if (empty){
				setText("");
				return;
			}

			try {
				StringProperty nameProperty = JavaBeanStringPropertyBuilder.create().bean(project).name("name").build();
				textProperty().bind(nameProperty);
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

}
