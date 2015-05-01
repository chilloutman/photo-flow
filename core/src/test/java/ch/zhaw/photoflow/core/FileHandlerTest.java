package ch.zhaw.photoflow.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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
	private File file = new File("C:/Users/Josh/Documents/Test/test.jpg");
	private File file2 = new File("C:/Users/Josh/Documents/Test/test2.jpg");
	private File file3 = new File("C:/Users/Josh/Documents/Test/test.jpg");
	
	@Before
	public void before() {
		fileHandler = new FileHandler();
		project = Project.newProject();
		photo1 = Photo.newPhoto();
		photo2 = Photo.newPhoto();
		photo3 = Photo.newPhoto();
		photo4 = Photo.newPhoto();
		photo4.setFilePath("C:/Users/Josh/Documents/Test/testNotExist.jpg");
		pList.add(photo1);
		pList.add(photo2);
		pList.add(photo3);
	}
	
	@Test
	public void checkDirectoriesCreated() {
		assertThat(fileHandler.getUserHomePath(), notNullValue());
		assertThat(fileHandler.getWorkingPath(), notNullValue());
		assertFalse(fileHandler.getUserHomePath().isEmpty());
		assertFalse(fileHandler.getWorkingPath().isEmpty());		
	}
	
	@Test
	public void checkExportZip() throws FileNotFoundException, IOException {
		assertThat(fileHandler.exportZip("C:/Users/Josh/Documents/Test/test.zip", pList), notNullValue());
	}
	
	@Test(expected=FileNotFoundException.class)
	public void checkLoadPhoto() throws FileNotFoundException {
		fileHandler.loadPhoto(photo4);
	}
	
	@Test(expected=FileAlreadyExistsException.class)
	public void checkImportPhoto() throws IOException {
		assertThat(fileHandler.importPhoto(photo1, file).getCreationDate(), notNullValue());
		assertThat(fileHandler.importPhoto(photo2, file2).getFilePath(), notNullValue());
		Photo returnPhoto = fileHandler.importPhoto(photo1, file3);
		assertTrue(returnPhoto == null);
	}
}
