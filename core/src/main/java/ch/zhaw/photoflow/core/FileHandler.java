package ch.zhaw.photoflow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.io.Files;

import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;

public class FileHandler {
	
	private static final String PHOTO_FLOW = "PhotoFlow";
	private String workingPath;
	private String userHomePath;
	
	public FileHandler(){
		setUserHomePath(System.getProperty("user.home"));
		if(!createWorkingPath()){
			// TODO: WorkingPath schon gesetzt, den efach wiiter
		}else{
			setWorkingPath(getUserHomePath()+PHOTO_FLOW);
		}
	}
	
	/**
	 * Creates working path, where actual Projectfiles are stored.
	 * @return true, if directory can be created, false, if it already exists
	 */
	private boolean createWorkingPath(){
		File f = new File(getUserHomePath()+PHOTO_FLOW+"/");
		if(f.exists() && f.isDirectory()){
			return true; //TODO
		}
		return f.mkdir();
	}

	/**
	 * This method is used to create a new Project (File-Folder on Explorer).
	 * @param project
	 */
	public void createProject(Project project) {
		new File(getWorkingPath()+project.getId()).mkdir();
	}
	
	/**
	 * Function used to import a physical Photo to the Project-Directory and set Photo parameters.
	 * @param photo Logical representation of a Photo.
	 * @param file Physical Photo-File.
	 * @return Photo Updated logical representation of a Photo.
	 * @throws IOException Throws an Error if File already exists in Project-Directory.
	 */
	public Photo importPhoto(Photo photo, File file) throws IOException {
		if(new File(getWorkingPath()+file.getName()).exists()){
			throw new FileAlreadyExistsException("File already exists!");
		}
		File newFile = new File(getWorkingPath()+file.getName());
		Files.copy(file, newFile);
		photo.setFilePath(newFile.getAbsolutePath());
		photo.setCreationDate(LocalDateTime.now());
		photo.setFileSize((int) file.length());
		return photo;
	}
	
	/**
	 * Method to get the physical Photo-File according to the logical Photo.
	 * @param photo Logical representation of a Photo.
	 * @return File physical Photo File.
	 * @throws FileNotFoundException Error is thrown if physical File cannot be found or is invalid.
	 */
	public File loadPhoto(Photo photo) throws FileNotFoundException {
		File file = new File(photo.getFilePath());
		if(!file.isFile()){
			throw new FileNotFoundException("File not found or invalid!");
		}
		return file;
	}
	
	/**
	 * Method to zip a list of physical Project-Photo-Files into one Zip-File.
	 * @param zipName Name of the Zip-File.
	 * @param list List of physical Photos.
	 * @return created Zip-File.
	 * @throws FileNotFoundException If a File in the List cannot be found, this Exception is thrown.
	 * @throws IOException
	 */
	public File exportZip(String zipName, List<Photo> list) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(zipName);
		ZipOutputStream zos = new ZipOutputStream(fos);
		for(Photo photo : list){
			 addToZip(photo.getFilePath(), zos);
		}
		zos.close();
		fos.close();
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
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();		
	}
	

	public String getUserHomePath() {
		return userHomePath;
	}

	public void setUserHomePath(String userHomePath) {
		this.userHomePath = userHomePath;
	}

	public String getWorkingPath() {
		return workingPath;
	}

	public void setWorkingPath(String workingPath) {
		this.workingPath = workingPath;
	}	
}
