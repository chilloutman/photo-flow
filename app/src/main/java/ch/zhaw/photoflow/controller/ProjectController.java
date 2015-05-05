package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.google.common.annotations.VisibleForTesting;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import ch.zhaw.photoflow.Main;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.PhotoWorkflow;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;
import ch.zhaw.photoflow.core.domain.ProjectWorkflow;

public class ProjectController extends Pane implements Initializable {
	private final ProjectWorkflow projectWorkflow;
	private final PhotoWorkflow photoWorkflow;
	private final ProjectDao projectDao;
	private final PhotoDao photoDao;
	private Project project;
	private List<Photo> photos;

	@FXML
	TextField projectNameField;
	@FXML
	Button workflowNextButton, workflowPauseButton, workflowBackButton, archiveProjectButton;
	@FXML
	MenuButton todoButton;

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
	}
	
	public void setProject(Project project) {
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

	public void importPhotos() {
		// TODO: Usecase Import photos
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

		// inline
		workflowNextButton.setOnAction(event -> {
			System.out.println("clicked Next");
		});

		// external method
		workflowNextButton.setOnAction(this::test);

		archiveProjectButton.setOnAction(this::archiveProject);

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
