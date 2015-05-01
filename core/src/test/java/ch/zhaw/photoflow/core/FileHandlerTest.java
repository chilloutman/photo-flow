package ch.zhaw.photoflow.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;

public class FileHandlerTest {

	private FileHandler fileHandler;
	private Project project;
	private Photo photo1, photo2, photo3, photo4;
	private List<Photo> pList;
	private File file = new File(System.getProperty("user.home")+"/Test/test.jpg");
	private File file2 = new File(System.getProperty("user.home")+"/Test/test2.jpg");
	private File file3 = new File(System.getProperty("user.home")+"/Test/test.jpg");
	
	@Before
	public void before() {
		project = Project.newProject();
		project.setId(1234);
		try {
			fileHandler = new FileHandler(project);
		} catch (FileHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		photo1 = Photo.newPhoto();
		photo2 = Photo.newPhoto();
		photo3 = Photo.newPhoto();
		photo4 = Photo.newPhoto();
		photo4.setFilePath(System.getProperty("user.home")+"/Test/testNotExist.jpg");
		pList = new ArrayList<Photo>();
		pList.add(photo1);
		pList.add(photo2);
		pList.add(photo3);
	}
	
	@Test
	public void checkDirectoriesCreated() {
		assertTrue(fileHandler.getUserHomePath() != null);
		assertTrue(fileHandler.getWorkingPath() != null);
		assertTrue(fileHandler.getProjectPath() != null);
		assertFalse(fileHandler.getUserHomePath().isEmpty());
		assertFalse(fileHandler.getWorkingPath().isEmpty());
		assertFalse(fileHandler.getProjectPath().isEmpty());
	}
	
	@Test
	public void checkExportZip() throws FileNotFoundException, IOException {
		photo1.setFilePath(System.getProperty("user.home")+"/Test/test.jpg");
		photo2.setFilePath(System.getProperty("user.home")+"/Test/test2.jpg");
		photo3.setFilePath(System.getProperty("user.home")+"/Test/test3.jpg");
		assertTrue(fileHandler.exportZip(System.getProperty("user.home")+"/Test/test.zip", pList).isFile());
	}
	
	@Test(expected=FileNotFoundException.class)
	public void checkLoadPhoto() throws FileNotFoundException {
		fileHandler.loadPhoto(photo4);
	}
	
	@Test(expected=FileAlreadyExistsException.class)
	public void checkImportPhoto() throws IOException, FileHandlerException {
		assertThat(fileHandler.importPhoto(photo1, file).getCreationDate(), notNullValue());
		assertThat(fileHandler.importPhoto(photo2, file2).getFilePath(), notNullValue());
		Photo returnPhoto = fileHandler.importPhoto(photo1, file3);
		assertTrue(returnPhoto == null);
	}
}