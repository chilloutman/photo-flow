package ch.zhaw.photoflow.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.domain.*;


public class MainControllerTest {

	private MainController main;
	private Project dummyProject;
	private Project emptyProject;
	
	private String name;
	private String description;
	
	
	
	@Before
	public void before() throws DaoException {
	
		main = new MainController(new InMemoryProjectDao());
			
		 dummyProject = Project.newProject(p -> {
			   p.setId(1);
			   p.setName("Secret Project");
			   p.setDescription("TOP SECRET, MAN!");
			  });
	}
	
	
	//Test 1
	@Test(expected = NullPointerException.class)
	public void addProjectandFails()
	{		
			emptyProject = Project.newProject(p -> {
			   p.setId(1);
			   p.setName(name);
			   p.setDescription(description);
			  });
		
			main.addProject(emptyProject);
			assertEquals("to be equal", emptyProject, main.getProjects().get(0));
	}
	
	//Test 2
	@Test
	public void addProject()
	{		
			main.addProject(dummyProject);
			assertEquals("to be equal", dummyProject, main.getProjects().get(0));
	}
	
	//Test 3
	@Test
	public void toGetProjects()
	{
			main.addProject(dummyProject);
			assertThat(main.getProjects(), is(not(empty())));
	}

	
	//Test 4
	@Test
	public void toDeleteProject()
	{
			main.addProject(dummyProject);
		
			Project project = main.getProjects().get(0);
			main.deleteProject(project);

			assertThat(main.getProjects(), is(empty()));
	}
	
	//Test 5
	@Test(expected = IndexOutOfBoundsException.class)
	public void toDeleteProjectandFail()
	{
			main.addProject(dummyProject);
		
			Project project = main.getProjects().get(99);
			main.deleteProject(project);

			assertThat(main.getProjects(), is(empty()));
	}
}
