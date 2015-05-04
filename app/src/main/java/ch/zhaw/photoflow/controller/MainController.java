package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
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
    private ListView<String> projectList;

	
	
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
		if(popup.getName() != null)
		{
			setProjectName(popup.getName());
			setProjectDescription(popup.getDesc());
			tags = popup.getTags();
			
			System.out.println("Tags = "+tags);
			
			
			project = Project.newProject(p -> {
				p.setName(projectName);
				p.setDescription(projectDescription);
				p.setTags(tags);
			});
			addProject(project);
			System.out.println("project created");
		}
		else
		{
			System.out.println("canceled project creation");
		}
		
	}
	
	public void addProject(Project project) {
		try {
			projectDao.save(project);
			this.projects.add(project);
			System.out.println(this.projects);
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
	
	public void updateList()
	{
		//add "new Project" entry
		List<String> np = Arrays.asList("+ new Project");
		List<String> values = projects.stream().map((project)->{return project.getName();}).collect(Collectors.toList());
		List<String> newList = new ArrayList<String>();
		newList.addAll(np);
		newList.addAll(values);
		
		projectList.setItems(FXCollections.observableList(newList));
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
		updateList();
		//handle selecting by mouse or keyboard
		projectList.setOnMouseClicked(this::handleMouseClick);
		projectList.setOnKeyPressed(this::handleEnter);
	}
	
	public void handleMouseClick(MouseEvent arg0) {
	    System.out.println("clicked on " + projectList.getSelectionModel().getSelectedItem());
	    
	    if(projectList.getSelectionModel().getSelectedItem() == "+ new Project")
	    {
	    	popup = new PopUpHandler();
	    	createProject();
	    	updateList();
	    }
	    else
	    {
	    	//todo
	    	//loadProject( projectList.getSelectionModel().getSelectedItem());
	    }
	}
	
	//work in Progess!
	public void handleEnter(KeyEvent arg0) {
		if(arg0.getCharacter() == KeyCode.ENTER.toString())
		{
		    System.out.println("selected on " + projectList.getSelectionModel().getSelectedItem());
		    
		    if(projectList.getSelectionModel().getSelectedItem() == "+ new Project")
		    {
		    	popup = new PopUpHandler();
		    	createProject();
		    	updateList();
		    }
		    else
		    {
		    	//todo
		    	//loadProject( projectList.getSelectionModel().getSelectedItem());
		    }
		}
	}
	
	
}
