package ch.zhaw.photoflow.core;

import static ch.zhaw.photoflow.core.domain.Project.newProject;
import ch.zhaw.photoflow.core.domain.Project;

import com.google.common.collect.ImmutableList;

public final class DummyData {
	
	public static final ImmutableList<Project> PROJECTS = ImmutableList.of(
		newProject(p -> {
			p.setName("Secret Project");
			p.setDescription("TOP SECRET, MAN!");
		}),
		newProject(p -> {
			p.setName("Awesome Project");
			p.setDescription("Blah Blah Blah.");
		}),
		newProject(p -> {
			p.setName("Boring Project");
		})
	);
	
	private DummyData() {
	}
	
	public static void addProjects(ProjectDao dao) {
			PROJECTS.forEach(p -> {
				try {
					dao.save(p);
				} catch (DaoException e) {
					throw new RuntimeException(e);
				}
			});
	}
	
}
