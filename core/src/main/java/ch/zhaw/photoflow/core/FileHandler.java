package ch.zhaw.photoflow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.google.common.annotations.VisibleForTesting;

/**
 * Handles file related tasks
 */
public class FileHandler {
	
	private static final String PHOTO_FLOW = "PhotoFlow";
	
	@VisibleForTesting
	static File USER_HOME_DIR = new File(System.getProperty("user.home"));

	private Project project = Project.newProject();
	
	/**
	 * @return {@link File} SQLite database file.
	 * @throws FileHandlerException
	 */
	public static File sqliteFile() throws FileHandlerException {
		return new File(workingDir(), "PhotoFlow.db");
	}
	
	/**
	 * @return {@link File} working directory.
	 * @throws FileHandlerException
	 */
	static File workingDir() throws FileHandlerException {
		return checkDirectory(new File(USER_HOME_DIR, PHOTO_FLOW));
	}
	
	/**
	 * @return {@link File} archive directory
	 * @throws FileHandlerException
	 */
	static File archiveDir() throws FileHandlerException {
		return checkDirectory(new File(workingDir(), "Archive"));
	}
	
	/**
	 * Check if directory exists and try to create it if it doesn't.
	 * @param directory The directory to check.
	 * @return directory
	 * @throws FileHandlerException If something went wrong.
	 */
	private static File checkDirectory (File directory) throws FileHandlerException {
		if (!directory.isDirectory() && !directory.mkdirs()) {
			throw new FileHandlerException("Could not create directory: " + directory);
		}
		return directory;
	}
	
	/**
	 * Constructor initializes userhome and workingPath
	 * IMPORTANT NOTE: A file handler constructed with just an id might not correctly with archived projects.
	 * Use {@link FileHandler#FileHandler(Project)} whenever possible!
	 * @param projectId
	 */
	public FileHandler(Integer projectId) {
		this.project.setId(projectId);
	}
	
	/**
	 * Constructor initializes userhome and workingPath
	 * @param project
	 */
	public FileHandler(Project project) {
		this.project = project;
	}
	
	/**
	 * @return The project direcory.
	 * @throws FileHandlerException If required directories don't exist and can't be created.
	 */
	public File projectDir () throws FileHandlerException {
		if (ProjectState.ARCHIVED.equals(project.getState())) {
			return checkDirectory(new File(archiveDir(), project.getId().get().toString()));
		}
		return checkDirectory(new File(workingDir(), project.getId().get().toString()));
	}
	
	/**
	 * Method used to import a physical Photo to the Project-Directory and set Photo parameters.
	 * @param photo Logical representation of a Photo.
	 * @param file Physical Photo-File.
	 * @return Photo Updated logical representation of a Photo.
	 * @throws FileHandlerException If File already exists in Project-Directory. 
	 */
	public Photo importPhoto(Photo photo, File file) throws FileHandlerException {
		if (FileFormat.get(file.getName()) == null) {
			throw new FileHandlerException("File Extension is invalid!");
		}
		if (new File(projectDir(), file.getName()).exists()) {
			// TODO change name and import anyway.
			throw new FileHandlerException("File already exists: " + file);
		}

		File newFile = new File(projectDir(), file.getName());
		try {
			Files.copy(file.toPath(), newFile.toPath());
		} catch (IOException e) {
			throw new FileHandlerException("Could not import File (Copy Fail)!", e);
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
	}

	/**
	 * Gets an {@link InputStream} from a photo-file corresponding to the given {@link Photo photo} object.
	 * @param photo
	 * @return {@link InputStream}
	 * @throws FileHandlerException
	 */
	public InputStream loadPhoto(Photo photo) throws FileHandlerException {
		try {
			File file = getPhotoFile(photo);
			return new FileInputStream(file);
		} catch (IOException e) {
			throw new FileHandlerException("Could not find file for photo: " + photo, e);
		}
	}
	
	/**
	 * Loads the meta data of a photo-file.
	 * @param photo
	 * @return String dump of all photo-file meta datas.
	 * @throws FileHandlerException
	 */
	public String loadPhotoMetadata(Photo photo) throws FileHandlerException {
		try (InputStream photoInputStream = loadPhoto(photo)) {
			Metadata metadata = ImageMetadataReader.readMetadata(photoInputStream);
			
			String s = StreamSupport.stream(metadata.getDirectories().spliterator(), false)
				.flatMap(directory -> directory.getTags().stream())
				.filter(this::filterMetadataTag)
				.map(tag -> tag.getTagName() + ": " + tag.getDescription())
				.reduce("", (result, tagString) -> result + tagString + "\n");
			return s;
		} catch (ImageProcessingException | IOException e) {
			throw new FileHandlerException("Could load photo metadata: " + photo, e);
		}
	}
	
	private boolean filterMetadataTag(Tag tag) {
		return (
			!tag.getTagName().contains("TRC") &&
			!tag.getTagName().startsWith("Red") &&
			!tag.getTagName().startsWith("Green") &&
			!tag.getTagName().startsWith("Blue")
		);
	}
	
	/**
	 * Method to get the physical Photo-File according to the logical Photo.
	 * @param photo Logical representation of a Photo.
	 * @return File physical Photo File.
	 * @throws FileNotFoundException Error is thrown if physical File cannot be found or is invalid.
	 */
	File getPhotoFile(Photo photo) throws FileHandlerException {
		return new File(projectDir(), photo.getFilePath());
	}
	
	/**
	 * Method to zip a list of physical project photo files into one zip file.
	 * @param zipName Name of the zip file.
	 * @param list List of physical Photos.
	 * @return created zip file.
	 * @throws FileHandlerException If a File in the List cannot be found, this Exception is thrown.
	 */
	public File exportZip(String zipName, List<Photo> list) throws FileHandlerException {
		try (
			FileOutputStream fos = new FileOutputStream(zipName);
			ZipOutputStream zos = new ZipOutputStream(fos);
		) {
			for (Photo photo : list) {
				 try {
					if(photo.getState() == PhotoState.FLAGGED){
						addToZip(photo, zos);
					}
				} catch (IOException e) {
					throw new FileHandlerException("Could not load Photo to Zip: " + photo, e);
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
	 * @throws FileHandlerException 
	 */
	private void addToZip(Photo photo, ZipOutputStream zos) throws FileNotFoundException, IOException, FileHandlerException {
		try (InputStream fis = loadPhoto(photo)){
			ZipEntry zipEntry = new ZipEntry(photo.getFilePath());
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
		File targetDir = new File(archiveDir(), project.getId().get().toString());
		if (!targetDir.isDirectory()) {
			try {
				Files.move(projectDir().toPath(), targetDir.toPath());
			} catch (IOException e) {
				throw new FileHandlerException("Projectfiles could not be moved to archive!",e);
			}
		}
		System.out.println("Project successfully archived!");
	}
	
	/**
	 * Method used to archive a Project, move all Files to Archive-Folder
	 * @throws FileHandlerException
	 */
	public void unArchiveProject() throws FileHandlerException {
		File targetDir = new File(workingDir(), project.getId().get().toString());
		if (!targetDir.isDirectory()) {
			try {
				Files.move(projectDir().toPath(), targetDir.toPath());
			} catch (IOException e) {
				throw new FileHandlerException("Projectfiles could not be moved to archive!",e);
			}
		}
		System.out.println("Project successfully archived!");
	}
	
	/**
	 * Deletes the photo in project directory.
	 * @param photo the photo to delete.
	 * @return {@code true} if the deletion completed successfully.
	 * @throws FileHandlerException
	 */
	public boolean deletePhoto(Photo photo) throws FileHandlerException {
		File file = new File(projectDir(), photo.getFilePath());
		if(!file.exists()){
			System.out.println("File could not be found on disk!");
		}
		return file.delete();
	}
	
	/**
	 * Deletes the project directory.
	 * @throws FileHandlerException
	 */
	public void deleteProject() throws FileHandlerException {
		deleteDirectory(projectDir());
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
