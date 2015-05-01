package ch.zhaw.photoflow.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;

public class MainController extends AbstractController implements Initializable {

	ProjectDao projectDao;
	List<Project> projects;
	Project project;
	
	@FXML
	private ProjectController projectController;

	
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
		//use variables
		//
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
		// TODO Auto-generated method stub
		
	}
	
}
