package ch.zhaw.photoflow.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;

public class FileHandlerTest {
	
	private static File TEST_DIR;
	
	private FileHandler fileHandler;
	private Project project;
	private Photo photo1, photo2, photo3, photo4;
	private List<Photo> photos;
	private File file1, file2, file3, file4;
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		TEST_DIR = Files.createTempDirectory("FileHandlerTest").toFile();
		TEST_DIR.deleteOnExit();
		FileHandler.USER_HOME_DIR = TEST_DIR;
	}
	
	@Before
	public void before() throws IOException, FileHandlerException {
		// Prepare Test with dummy Testdata
		project = Project.newProject();
		project.setId(1234);
		fileHandler = new FileHandler(project);
		
		file1 = new File(TEST_DIR, "test1.jpg");
		file2 = new File(TEST_DIR, "test2.jpg");
		file3 = new File(TEST_DIR, "test3.jpg");
		file4 = new File(TEST_DIR, "test4.jpg");
		
		file1.createNewFile();
		file2.createNewFile();
		file3.createNewFile();
		file4.createNewFile();
		
		photo1 = Photo.newPhoto(p -> p.setFilePath(file1.getName()));
		photo2 = Photo.newPhoto(p -> p.setFilePath(file2.getName()));
		photo3 = Photo.newPhoto(p -> p.setFilePath(file3.getName()));
		photo4 = Photo.newPhoto(p -> p.setFilePath("testNotExist.jpg"));
		
		photos = Arrays.asList(photo1, photo2, photo3);
	}
	
	@After
	public void after () throws FileHandlerException {
		fileHandler.deleteProject();
	}
	
	/**
	 * Checks that after the FileHandler is created, the corresponding Working Directories are created.
	 */
	@Test
	public void checkDirectoriesCreated() {
		assertTrue(fileHandler.getProjectDir().isDirectory());
	}
	
	/**
	 * Checks that the Zip File could be generated.
	 * @throws FileNotFoundException If one of the Photo's in the List cannot be found (physically).
	 * @throws IOException
	 */
	@Test
	public void checkExportZip() throws FileHandlerException {
		fileHandler.importPhoto(photo1, file1);
		fileHandler.importPhoto(photo2, file2);
		fileHandler.importPhoto(photo3, file3);
		assertTrue(fileHandler.exportZip(TEST_DIR + "test.zip", photos).isFile());
	}
	
	/**
	 * Loads a Photo-File according the Photo-Object. Checks that exception is thrown if File cannot be found.
	 * @throws FileNotFoundException
	 */
	@Test(expected=FileHandlerException.class)
	public void checkLoadPhoto() throws FileHandlerException {
		fileHandler.importPhoto(photo3, file3);
		assertTrue(fileHandler.getPhotoFile(photo3).isFile());
		fileHandler.loadPhoto(photo4);
	}
	
	/**
	 * Checks the archive functionality.
	 * Project Folder is moved to Archive Folder and should not be existing in old Directory anymore.
	 * @throws IOException
	 * @throws FileHandlerException
	 */
	@Test
	public void checkArchiveProject() throws IOException, FileHandlerException {
		fileHandler.importPhoto(photo1, file1);
		fileHandler.importPhoto(photo2, file2);
		fileHandler.archiveProject();
		assertFalse(fileHandler.getProjectDir().isDirectory());
		assertFalse(fileHandler.getProjectDir().exists());
		if (file1.exists()&&file2.exists()) {
			file1.delete();
			file2.delete();
		}
	}

}
