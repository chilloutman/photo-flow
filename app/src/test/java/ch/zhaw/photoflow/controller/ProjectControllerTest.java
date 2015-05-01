package ch.zhaw.photoflow.controller;

import static ch.zhaw.photoflow.core.domain.Project.newProject;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.dao.InMemoryPhotoDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;

public class ProjectControllerTest {

	private static final Integer PROJECT_1 = 1;
	private static final Integer PROJECT_2 = 2;
	
	private ProjectDao projectDao;
	private PhotoDao photoDao;
	
	
	@Before
	public void before() throws DaoException {
		//Initialize ProjectDao Testdata
		projectDao = new InMemoryProjectDao();
		
		projectDao.save(Project.newProject(p -> {
			p.setName("Secret Project");
			p.setDescription("TOP SECRET, MAN!");
		}));
		projectDao.save(Project.newProject(p -> {
			p.setName("Awesome Project");
			p.setDescription("Blah Blah Blah.");
		}));
		projectDao.save(Project.newProject(p -> {
			p.setName("Boring Project");
		}));
		
		//Initialize PhotoDao Testdata
		photoDao = new InMemoryPhotoDao();
		
		photoDao.save(Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("swag.jpg");
		}));
		
		photoDao.save(Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("yolo.jpg");
		}));
		
		photoDao.save(Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_2);
			p.setFilePath("pimp.jpg");
		}));
	}
	
	@Test
	public void photosLoadedAfterConstruct() {
		
	}
	
	@Test
	public void loadPhotos() {
		
	}
	
	@Test
	public void addPhoto() {
		
	}
	
	@Test
	public void deletePhoto() {
		
	}
	
	@Test
	public void flagPhotos() {
		
	}

	@Test
	public void transistState() {
		
	}
	
}
