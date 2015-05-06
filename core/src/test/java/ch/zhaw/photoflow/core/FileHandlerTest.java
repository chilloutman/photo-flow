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
	private static String userHome = System.getProperty("user.home");
	private File file, file2, file3, file4;
	private static boolean cleanUpDone = false;
	
	@Before
	public void before() throws IOException {
		
		// Working directories
		File f = new File(userHome+"/Test/");
		File f2 = new File(userHome+"/PhotoFlow/");
		
		// Execute CleanUp only once to check Files at the End in Explorer
		if(!cleanUpDone){
			// Clean first, that after Test can be manually checked if Files get created
			if(f.exists()){
				deleteDirectory(f);
			}
			if(f2.exists()){
				deleteDirectory(f2);
			}
			// SetUp only Once
			cleanUpDone = true;
		}
		
		// Prepare Test with dummy Testdata
		project = Project.newProject();
		project.setId(1234);
		try {
			fileHandler = new FileHandler(project);
		} catch (FileHandlerException e) {
			e.printStackTrace();
		}
		
		// Test directory(For Zip and Import) and PhotoFlow directory
		f.mkdir();
		f2.mkdir();
		
		file = new File(userHome+"/Test/test.jpg");
		file2 = new File(userHome+"/Test/test2.jpg");
		file3 = new File(userHome+"/Test/test.jpg");
		file4 = new File(userHome+"/Test/test3.jpg");
		file.createNewFile();
		file2.createNewFile();
		file3.createNewFile();
		file4.createNewFile();
		photo1 = Photo.newPhoto();
		photo2 = Photo.newPhoto();
		photo3 = Photo.newPhoto();
		photo4 = Photo.newPhoto();
		photo1.setFilePath(userHome+"/Test/test.jpg");
		photo2.setFilePath(userHome+"/Test/test2.jpg");
		photo3.setFilePath(userHome+"/Test/test3.jpg");
		photo4.setFilePath(userHome+"/Test/testNotExist.jpg");
		pList = new ArrayList<Photo>();
		pList.add(photo1);
		pList.add(photo2);
		pList.add(photo3);
	}
	
	/**
	 * Checks that after the FileHandler is created, the corresponding Working Directories are created.
	 */
	@Test
	public void checkDirectoriesCreated() {
		assertTrue(fileHandler.getUserHomePath() != null);
		assertTrue(fileHandler.getWorkingPath() != null);
		assertTrue(fileHandler.getProjectPath() != null);
		assertFalse(fileHandler.getUserHomePath().isEmpty());
		assertFalse(fileHandler.getWorkingPath().isEmpty());
		assertFalse(fileHandler.getProjectPath().isEmpty());
	}
	
	/**
	 * Checks that the Zip File could be generated.
	 * @throws FileNotFoundException If one of the Photo's in the List cannot be found (physically).
	 * @throws IOException
	 */
	@Test
	public void checkExportZip() throws FileNotFoundException, FileHandlerException{
		assertTrue(fileHandler.exportZip(userHome+"/Test/test.zip", pList).isFile());
	}
	
	/**
	 * Loads a Photo-File according the Photo-Object. Checks that exception is thrown if File cannot be found.
	 * @throws FileNotFoundException
	 */
	@Test(expected=FileNotFoundException.class)
	public void checkLoadPhoto() throws FileNotFoundException {
		assertTrue(fileHandler.loadPhoto(photo3).isFile());
		fileHandler.loadPhoto(photo4);
	}
	
	/**
	 * Checks import of new photos. Throws an exception if the file already exists.
	 * @throws FileHandlerException 
	 */
	@Test(expected=FileHandlerException.class)
	public void checkImportPhoto() throws FileHandlerException {
		assertThat(fileHandler.importPhoto(photo1, file).getCreationDate(), notNullValue());
		assertThat(fileHandler.importPhoto(photo2, file2).getFilePath(), notNullValue());
		Photo returnPhoto = fileHandler.importPhoto(photo1, file3);
		assertTrue(returnPhoto == null);
	}
	
	/**
	 * Checks the archive functionality.
	 * Project Folder is moved to Archive Folder and should not be existing in old Directory anymore.
	 * @throws IOException
	 * @throws FileHandlerException
	 */
	@Test
	public void checkArchiveProject() throws IOException, FileHandlerException {
		fileHandler.importPhoto(photo1, file);
		fileHandler.importPhoto(photo2, file2);
		fileHandler.archiveProject();
		assertFalse(new File(fileHandler.getProjectPath()).isDirectory());
		assertFalse(new File(fileHandler.getProjectPath()).exists());
		if(file.exists()&&file2.exists()){
			file.delete();
			file2.delete();
		}
	}
	
	/**
	 * Private method used to cleanup Testdata Directories and Files
	 * @param path
	 * @return
	 */
	private boolean deleteDirectory(File path) {
	    if (path.exists()) {
	        File[] files = path.listFiles();
	        for (int i = 0; i < files.length; i++) {
	            if (files[i].isDirectory()) {
	                deleteDirectory(files[i]);
	            } else {
	                files[i].delete();
	            }
	        }
	    }
	    return (path.delete());
	}
}
