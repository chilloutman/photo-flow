package ch.zhaw.photoflow.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.Tag;


public class MainControllerTest {

	private MainController main;
	
	private List<Project> emptyProjects;
	private List<Project> getProjects;
	private String projectName;
	private String projectDescription;
	private List<Tag> tags;
	private Tag tag1;
	private Tag tag2;
	
	
	@Before
	public void before() throws DaoException {
	
		main = new MainController(new InMemoryProjectDao());
		projectName ="Xmas '14";
		projectDescription = "This is a very fine project";
		tag1 = new Tag("xmas");
		tag2 = new Tag("swiss");
		tags = Arrays.asList(tag1, tag2);
		
		//empty Project for testing the delete function
		emptyProjects = new ArrayList<Project>();
		
		
		//Projects to fill for get()
		getProjects = new ArrayList<Project>();
		
		main.setProjectDescription(projectDescription);
		main.setProjectName(projectName);
		main.setTags(tags);
		main.createProject();
		
		//
		getProjects = main.getProjects();
		System.out.println(getProjects);
	}
	
	
	@Test
	public void toCreateProject()
	{
				
			assertEquals("Project-Name 'Xmas '14'", "Xmas '14", main.getProjectName());
			assertEquals("Project-Description 'This is a very fine project'", "This is a very fine project", main.getProjectDescription());
			assertEquals("Tags 'xmas' and 'swiss'", 2, main.getTags().size());
			assertEquals("Tags 'xmas'", new Tag("xmas"), main.getTags().get(0));
			assertEquals("Tags 'swiss'", new Tag("swiss"), main.getTags().get(1));
	}
	
	
	//not yet working
	@Test
	public void toGetProjects()
	{
			assertThat(main.getProjects(), is(not(empty())));
	}

	
	@Test
	public void toDeleteProject()
	{
			Project project = main.getProjects().get(0);
			main.deleteProject(project);
			
			
			assertEquals("Project to be delete", emptyProjects, main.getProjects());
	}


}
