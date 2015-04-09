package ch.zhaw.photoflow.core.dummy;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Project;

public class DummyProjectDao implements ProjectDao {

	private final List<Project> projects = new ArrayList<>();
	
	public DummyProjectDao() {
		projects.add(new Project());
		projects.add(new Project());
		projects.add(new Project());
	}
	
	@Override
	public List<Project> load() {
		return new ArrayList<>(projects);
	}

	@Override
	public Project save(Project project) {
		// TODO:
		// 1. Check project id:
		//    - If there is already a project with that id, replace it.
		//    - Otherwise add a new project and generate an id.
		projects.add(project);
		return project;
	}

	@Override
	public void delete(Project project) {
		projects.remove(project);
	}

}
