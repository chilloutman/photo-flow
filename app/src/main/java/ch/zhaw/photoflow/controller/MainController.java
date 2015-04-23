package ch.zhaw.photoflow.controller;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Project;

public class MainController extends AbstractController {

	ProjectDao projectDao;
	List<Project> projects;
	
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
	 * @param project
	 */
	public void createProject(Project project) {
		addProject(project);
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
	
}
