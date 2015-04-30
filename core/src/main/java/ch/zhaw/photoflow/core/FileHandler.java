package ch.zhaw.photoflow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
		
	private boolean createWorkingPath(){
		File f = new File(getUserHomePath()+PHOTO_FLOW);
		if(f.exists() && f.isDirectory()){
			return true; //TODO
		}
		return new File(getUserHomePath()+PHOTO_FLOW).mkdir();
	}

	/**
	 * This method is used to create a new Project (File-Folder on Explorer)
	 * @param project
	 */
	public void createProject(Project project) {
		new File(getWorkingPath()+project.getId()).mkdir();
	}
	
	public Photo importPhoto(Photo photo, File file) throws IOException {
		File newFile = new File("./"+photo.getProjectId());
		Files.copy(file, newFile);
		photo.setFilePath(newFile.getAbsolutePath());
		photo.setCreationDate(LocalDateTime.now());
		photo.setFileSize((int) file.length());
		return photo;
	}
	
	public File loadPhoto(Photo photo) throws FileNotFoundException {
		File file = new File(photo.getFilePath());
		if(!file.isFile()){
			throw new FileNotFoundException("File not found or invalid!");
		}
		return file;
	}
	
	public File exportZip(String zipName, List<Photo> list) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(zipName);
		ZipOutputStream zos = new ZipOutputStream(fos);
		for(Photo photo : list){
			 addToZip(photo.getFilePath(), zos);
		}
		zos.close();
		fos.close();
		return null;
	}
	
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
