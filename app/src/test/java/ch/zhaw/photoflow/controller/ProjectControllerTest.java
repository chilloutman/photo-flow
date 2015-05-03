package ch.zhaw.photoflow.controller;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.PhotoFlow;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.dao.InMemoryPhotoDao;
import ch.zhaw.photoflow.core.dao.InMemoryProjectDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;

public class ProjectControllerTest {

	private final PhotoFlow photoFlow = new PhotoFlow();
	private ProjectController projectController;
	
	private static final Integer PROJECT_1 = 1;
	private static final Integer PROJECT_2 = 2;
	
	private ProjectDao projectDao;
	private PhotoDao photoDao;
	
	private Project project1;
	private Project project2;
	
	private Photo photo1;
	private Photo photo2;
	private Photo photo3;
	
	@Before
	public void before() throws DaoException {
		projectDao = new InMemoryProjectDao();
		photoDao = new InMemoryPhotoDao();
		projectController = new ProjectController(projectDao, photoDao, photoFlow.getProjectWorkflow(), photoFlow.getPhotoWorkflow());
		
		//Initialize ProjectDao Testdata
		project1 = Project.newProject(p -> {
			p.setId(1);
			p.setName("Secret Project");
			p.setDescription("TOP SECRET, MAN!");
		});
		projectDao.save(project1);
		
		project2 = Project.newProject(p -> {
			p.setId(2);
			p.setName("Awesome Project");
			p.setDescription("Blah Blah Blah.");
		});
		projectDao.save(project2);
		
		
		//Initialize PhotoDao Testdata
		photo1 = Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("swag.jpg");
		});
		photoDao.save(photo1);
		
		photo2 = Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("yolo.jpg");
		});
		photoDao.save(photo2);
		
		photo3 = Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_2);
			p.setFilePath("pimp.jpg");
		});
		photoDao.save(photo3);
	}
	
	@Test
	public void loadPhotos() {
		//Load photos
		projectController.loadPhotos(project1);
		List<Photo> effectiveLoadedPhotos = projectController.getPhotos();
		
		//Tests
		assertEquals("Number of Photos", 2, effectiveLoadedPhotos.size());
		effectiveLoadedPhotos.stream().forEach( (photo) -> {
			assertEquals("PhotoID", project1.getId().get(), photo.getProjectId().get());
		});
	}
	
	@Test
	public void addPhoto() {
		Photo photo = Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("Kleid.jpg");
		});
		
		projectController.setProject(project1);
		projectController.addPhoto(photo);
		Photo photoInList = projectController.getPhotos().get(projectController.getPhotos().size() - 1);
		
		photo.setProjectId(project1.getId().get());
		assertEquals("Photo has been added", photo, photoInList);
	}
	
	@Test
	public void deletePhoto() {
		Photo photo = Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("yolo.jpg");
		});
		
		projectController.deletePhoto(photo);
		assertFalse(projectController.getPhotos().contains(photo));
	}
	
	@Test
	public void flagPhotos() {
		Photo photo = Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("yolo.jpg");
		});		
		
		projectController.setProject(project1);
		projectController.loadPhotos(project1);
		
		projectController.flagPhoto(photo);
		assertEquals("Photo has not been flagged(Illegal Project State)", PhotoState.NEW, photo.getState());
		photoFlow.getProjectWorkflow().transition(project1, projectController.getPhotos(), ProjectState.IN_WORK);
		projectController.flagPhoto(photo);
		assertEquals("Photo has been flagged(Legal Project State)", PhotoState.FLAGGED, photo.getState());
	}

	@Test
	public void transistState() {
		projectController.setProject(project1);
		
		projectController.transistState(project1,  ProjectState.DONE);
		
		assertEquals("Transist to DONE should fail(Not Legal)", projectController.getProject().getState(), ProjectState.NEW);

		projectController.transistState(project1,  ProjectState.IN_WORK);
		assertEquals("Transist to IN_WORK(Legal)", projectController.getProject().getState(), ProjectState.IN_WORK);
		
	}
	
}
