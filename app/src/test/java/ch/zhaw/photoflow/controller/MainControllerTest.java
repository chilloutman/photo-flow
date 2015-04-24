package ch.zhaw.photoflow.controller;

import static ch.zhaw.photoflow.core.domain.Project.newProject;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;


public class MainControllerTest {

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
	
	//add failed
	@Test
	public void toGetProjects()
	{
		
	}
	
	//create
	@Test (expected = DaoException.class)
	public void failDuringCreateProject()
	{
				
	}
	
	@Test
	public void toCreateProject()
	{
				
	}
	
	//delete
	@Test (expected = DaoException.class)
	public void failDuringDeleteProject()
	{
		
	}
	
	@Test
	public void toDeleteProject()
	{
		
	}


}
