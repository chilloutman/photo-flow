package core;

import static ch.zhaw.photoflow.core.domain.Project.newProject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectTest;


public class InMemoryProjectDaoTest {
	
	private ProjectDao dao;
	
	@Before
	public void before() throws DaoException {
		dao = new InMemoryProjectDao();
		
		dao.save(newProject(p -> {
			p.setName("Secret Project");
			p.setDescription("TOP SECRET, MAN!");
		}));
		dao.save(newProject(p -> {
			p.setName("Awesome Project");
			p.setDescription("Blah Blah Blah.");
		}));
		dao.save(newProject(p -> {
			p.setName("Boring Project");
		}));
	}
	
	@Test
	public void loadReturnsAllProjects () throws DaoException {
		assertThat(dao.loadAll(), hasSize(3));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void listIsImmutable () throws DaoException {
		List<Project> projects = dao.loadAll();
		projects.clear();
	}
	
	/**
	 * Changing the description here does not change the actual project instance without calling save().
	 */
	@Test
	public void projectsAreNotLive () throws DaoException {
		dao.loadAll().get(0).setDescription(ProjectTest.DESCRIPTION);
		assertThat(dao.loadAll().get(0).getDescription(), not(ProjectTest.DESCRIPTION));
	}
	
	@Test
	public void saveProject () throws DaoException {
		Project project = dao.loadAll().get(0);
		project.setDescription(ProjectTest.DESCRIPTION);
		dao.save(project);
		assertThat(dao.load(project.getId().get()).get().getDescription(), is(ProjectTest.DESCRIPTION));
	}
	
	@Test
	public void deleteProject () throws DaoException {
		Integer id = dao.loadAll().get(0).getId().get();
		dao.delete(Project.newProject(p -> {
			p.setId(id);
		}));
		assertThat(dao.loadAll(), hasSize(2));
	}
	
}
