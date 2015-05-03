package ch.zhaw.photoflow.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ch.zhaw.photoflow.Main;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;

public class MainController extends AbstractController implements Initializable {

	ProjectDao projectDao;
	List<Project> projects;
	Project project;
	PopUpHandler popup;
	Stage stage; 
	Parent root;
	
	//FMXL-stuff
	@FXML
	private ProjectController projectController;
	
	@FXML
    private ListView projectList;

	
	
	private String projectName;
	private String projectDescription;
	private List<Tag> tags = new ArrayList<>();
	
	public MainController(ProjectDao projectDao) {
		this.projects = new ArrayList<Project>();
		this.projectDao = projectDao;
		loadProjects();
	}
	
	private void loadProjects() {
		List<Project> tempProjects = new ArrayList<Project>(projects);
		try {
			this.projects.clear();
			this.projects.addAll(projectDao.loadAll());
		} catch (DaoException e) {
			this.projects = tempProjects;
		}
	}
	
	/**
	 * Processes stuff for object {@link Project} and adds to list.
	 */
	public void createProject() {

		//todo tag handling
		project = Project.newProject(p -> {
			p.setName(projectName);
			p.setDescription(projectDescription);
			p.setTags(tags);
		});
		addProject(project);
		try {
			projectDao.save(project);
		} catch (DaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addProject(Project project) {
		try {
			projectDao.save(project);
			this.projects.add(project);
		} catch (DaoException e) {
			// TODO: Warn user
		}
	}
	
	public void deleteProject(Project project) {
		try {
			projectDao.delete(project);
			this.projects.remove(project);
		} catch (DaoException e) {
			// TODO: Warn user
		}
	}
	
	public void createPopUp()
	{
//		System.out.println("make se gui");
//		//spawn gui	
//		   stage = new Stage();
//		   try {
//			root = FXMLLoader.load(getClass().getResource("../view/create_project.fxml"));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		   stage.setScene(new Scene(root));
//		   stage.setTitle("Create Project");
//		   stage.initModality(Modality.APPLICATION_MODAL);
//		   //stage.initOwner(btn1.getScene().getWindow());
//		   
//		   stage.showAndWait();
		
	}

	/*
	 * Getter and Setter
	 */
	public List<Project> getProjects() {
		return this.projects;
	}
	
	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		//add "new Project" entry
		List<String> values = Arrays.asList("+ new Project");
		projectList.setItems(FXCollections.observableList(values));

		//handle selecting Mouse Selection
		projectList.setOnMouseClicked(this::handleMouseClick);
	}
	
	public void handleMouseClick(MouseEvent arg0) {
	    System.out.println("clicked on " + projectList.getSelectionModel().getSelectedItem());
	    
	    if(projectList.getSelectionModel().getSelectedItem() == "+ new Project")
	    {
	    	popup = new PopUpHandler();
	    }
	    else
	    {
	    	//loadProject( projectList.getSelectionModel().getSelectedItem());
	    }
	}
	
}
