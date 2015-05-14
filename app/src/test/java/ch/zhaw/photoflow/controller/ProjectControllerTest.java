package ch.zhaw.photoflow.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.dao.DaoException;
import ch.zhaw.photoflow.core.dao.PhotoDao;
import ch.zhaw.photoflow.core.dao.ProjectDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;

public class ProjectControllerTest extends ControllerTest<ProjectController> {

	private ProjectController projectController;
	
	private static final Integer PROJECT_1 = 1;
	private static final Integer PROJECT_2 = 2;
	
	private Project project1;
	private Project project2;
	
	private Photo photo1;
	private Photo photo2;
	private Photo photo3;
	
	@Before
	public void before() throws DaoException, SQLException, IOException {
		projectController = loadController(ProjectController.class.getResource("../view/project.fxml"));
		ProjectDao projectDao = projectController.getPhotoFlow().projectDao();
		PhotoDao photoDao = projectController.getPhotoFlow().photoDao();
		
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
		projectController.setProject(project1);
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
	public void transistState() {
		projectController.setProject(project1);
		
		projectController.transitionState(project1,  ProjectState.DONE);
		assertEquals("Transist to DONE should fail(Not Legal)", ProjectState.NEW, projectController.getProject().getState());

		projectController.transitionState(project1,  ProjectState.IN_WORK);
		assertEquals("Transist to IN_WORK(Legal)", ProjectState.IN_WORK, projectController.getProject().getState());
		
	}
	
}
