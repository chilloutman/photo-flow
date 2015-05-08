package ch.zhaw.photoflow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;

import com.google.common.io.Files;


public class FileHandler {
	
	private static final String PHOTO_FLOW = "PhotoFlow";
	private static final File USER_HOME_DIR = new File(System.getProperty("user.home"));
	static final File WORKING_DIR = new File(USER_HOME_DIR, PHOTO_FLOW);
	private static final File ARCHIVE_DIR = new File(WORKING_DIR, "Aarchive");
	private static final File SQLITE_DIR = new File(WORKING_DIR, "DB");
	private static final File SQLITE_FILE = new File(SQLITE_DIR, "/photoFlow.db");
	
	private final File projectDir;
	private final Project project;
	
	public static File getSqliteFile() throws FileHandlerException {
		checkDirectory(SQLITE_FILE.getParentFile());
		return SQLITE_FILE;
	}
	
	/**
	 * Check if directory exists and try to create it if it doesn't.
	 * @param directory The directory to check.
	 * @throws FileHandlerException If something went wrong.
	 */
	private static void checkDirectory (File directory) throws FileHandlerException {
		if (!directory.isDirectory() && !directory.mkdirs()) {
			throw new FileHandlerException("Could not create directory: " + directory);
		}
	}
	
	/**
	 * Constructor initializes userhome and workingPath
	 * @throws FotoHandlerException 
	 */
	public FileHandler(Project project) throws FileHandlerException {
		this.project = project;
		projectDir = new File(WORKING_DIR, project.getId().get().toString());
		checkDirectory(WORKING_DIR);
		checkDirectory(projectDir);
	}
	
	/**
	 * Method used to import a physical Photo to the Project-Directory and set Photo parameters.
	 * @param photo Logical representation of a Photo.
	 * @param file Physical Photo-File.
	 * @return Photo Updated logical representation of a Photo.
	 * @throws IOException Throws an Error if File already exists in Project-Directory.
	 * @throws FileHandlerException 
	 */
	public Photo importPhoto(Photo photo, File file) throws FileHandlerException {
		if(FileFormat.get(file.getName()) != null){
			if (new File(projectDir, file.getName()).exists()) {
				// TODO change name and import anyway.
				throw new FileHandlerException("File already exists!");
			}
			File newFile = new File(projectDir, file.getName());
			try {
				Files.copy(file, newFile);
			} catch (IOException e) {
				throw new FileHandlerException("Could not import File (Copy Fail)!",e);
			}
			photo.setFilePath(newFile.getName());
			Optional<FileFormat> fileFormat = FileFormat.get(file.getName());
			if (!fileFormat.isPresent()) {
				throw new FileHandlerException("File format of photo is not supported: " + file);
			}
			photo.setFileFormat(fileFormat.get());
			photo.setFileSize((int) file.length());
			photo.setCreationDate(LocalDateTime.now()); // TODO: Get file timestamp
			return photo;
		} else {
			throw new FileHandlerException("File Extension is invalid!");
		}
	}

	public InputStream loadPhoto(Photo photo) throws FileHandlerException {
		try {
			File file = getPhotoFile(photo);
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new FileHandlerException("Could not find file for photo: " + photo, e);
		}
	}
	
	/**
	 * Method to get the physical Photo-File according to the logical Photo.
	 * @param photo Logical representation of a Photo.
	 * @return File physical Photo File.
	 * @throws FileNotFoundException Error is thrown if physical File cannot be found or is invalid.
	 */
	File getPhotoFile(Photo photo) throws FileHandlerException {
		return new File(projectDir, photo.getFilePath());
	}
	
	/**
	 * Method to zip a list of physical Project-Photo-Files into one Zip-File.
	 * @param zipName Name of the Zip-File.
	 * @param list List of physical Photos.
	 * @return created Zip-File.
	 * @throws FileNotFoundException If a File in the List cannot be found, this Exception is thrown.
	 */
	public File exportZip(String zipName, List<Photo> list) throws FileHandlerException {
		try (
			FileOutputStream fos = new FileOutputStream(zipName);
			ZipOutputStream zos = new ZipOutputStream(fos);
		) {
			for(Photo photo : list){
				 try {
					addToZip(photo.getFilePath(), zos);
				} catch (IOException e) {
					throw new FileHandlerException("Could not load Photo to Zip!", e);
				}
			}
		} catch (IOException e1) {
			throw new FileHandlerException("FileHandler: Streams could not be opened!", e1);
		}
		System.out.println("Project exported!");
		return new File(zipName);
	}
	
	/**
	 * Method used to add a single physical Photo into a Zip-File.
	 * @param fileName Path to the physical Photo.
	 * @param zos ZipOutputStream
	 * @throws FileNotFoundException If the physical File cannot be found, this Exception is thrown.
	 * @throws IOException
	 */
	private void addToZip(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {
		File file = new File(fileName);
		try(FileInputStream fis = new FileInputStream(file);){
			ZipEntry zipEntry = new ZipEntry(file.getName());
			zos.putNextEntry(zipEntry);
	
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
	
			zos.closeEntry();
		}		
	}
	
	/**
	 * Method used to archive a Project, move all Files to Archive-Folder
	 * @throws FileHandlerException
	 */
	public void archiveProject() throws FileHandlerException {
		File targetDir = new File(ARCHIVE_DIR, project.getId().get().toString()+"/");
		if (!targetDir.exists()) {
			try {
				targetDir.mkdirs();
				Files.move(projectDir, targetDir);
			} catch (IOException e) {
				throw new FileHandlerException("Projectfiles could not be moved to archive!",e);
			}
		}
		System.out.println("Project successfully archived!");
	}
	
	public void deleteProject() throws FileHandlerException {
		deleteDirectory(projectDir);
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
	
	File getProjectDir () {
		return projectDir;
	}
	
}
