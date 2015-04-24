package ch.zhaw.photoflow.core;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
		}
		setWorkingPath(userHomePath+PHOTO_FLOW);
	}
		
	private boolean createWorkingPath(){
		return new File(userHomePath+PHOTO_FLOW).mkdir();
	}

	/**
	 * This method is used to create a new Project (File-Folder on Explorer)
	 * @param project
	 */
	public void createProject(Project project) {
		new File("./"+project.getId()).mkdir();
	}
	
	public Photo importPhoto(Photo photo, File file) throws IOException {
		File newFile = new File("./"+photo.getProjectId());
		Files.copy(file, newFile);
		photo.setFilePath(newFile.getAbsolutePath());
		photo.setCreationDate(LocalDateTime.now());
		photo.setFileSize((int) file.length());
		//photo.setFileFormat();
		// TODO: Save Photo to DB
		return photo;
	}
	
	public File loadPhoto(Photo photo) {
		File file = new File(photo.getFilePath());
		// TODO: Load Photo from DB
		if(!file.isFile()){
			// TODO: baamm error ^^
		}
		return file;
	}
	
	public File exportZip(List<Photo> list) {
		// TODO: Alli Föteli vom Dao lade und Zip erstelle
		return null;
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
