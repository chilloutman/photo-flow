package ch.zhaw.photoflow.core.dao;

import static ch.zhaw.photoflow.core.util.GuavaCollectors.toImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.domain.Project;

import com.google.common.collect.ImmutableList;

public class InMemoryProjectDao implements ProjectDao {
	
	private static int idCounter = 0;
	
	private static int nextId () {
		return idCounter++;
	}
	
	private final List<Project> projects = new ArrayList<>();
	
	public InMemoryProjectDao() {
	}
	
	/**
	 * Load all projects.
	 * @return An immutable list of all projects. {@link #save(Project)} must be called after changing projects.
	 */
	@Override
	public ImmutableList<Project> loadAll() {
		return projects.stream().map(Project::copy).collect(toImmutableList());
	}
	
	@Override
	public Optional<Project> load(int id) throws DaoException {
		// Search for a project with the given id.
		List<Project> found = projects.stream().filter(p -> p.getId().get().equals(id)).collect(Collectors.toList());
		if (found.size() > 1) {
			throw new DaoException("Project ID " + id + "is not unique! This is bad and should not have happened!");
		}
		return found.stream().findFirst().map(Project::copy);
	}

	@Override
	public Project save(Project project) {
		if (project.getId().isPresent()) {
			// Remove old project
			projects.removeIf(p -> p.getId().equals(project.getId()));
		} else {
			// Generate new ID.
			project.setId(nextId());
		}
		// Add new project
		projects.add(Project.copy(project));
		return project;
	}

	@Override
	public void delete(Project project) {
		projects.remove(project);
	}

}