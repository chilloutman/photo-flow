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

import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;


public class FileHandler {
	
	private static final String PHOTO_FLOW = "PhotoFlow";
	private static String userHomePath = System.getProperty("user.home")+"/";
	private static String workingPath = System.getProperty("user.home")+"/"+PHOTO_FLOW+"/";
	private static String archivePath = System.getProperty("user.home")+"/"+PHOTO_FLOW+"/Archive/";
	private static String sqlitePath = System.getProperty("user.home")+"/"+PHOTO_FLOW+"/DB/photoFlow.db";
	private static File sqliteFile = new File(sqlitePath);
	private String projectPath;
	private Project project;
	
	public FileHandler() {
		
	}
	
	/**
	 * Constructor initializes userhome and workingPath
	 * @throws FotoHandlerException 
	 */
	public FileHandler(Project project) throws FileHandlerException{
		this.project = project;
		if(!createWorkingPath()){
			throw new FileHandlerException("Error in creating Working Directory!");
		}
		if(!createProjectPath(project)){
			throw new FileHandlerException("Error in creating Project Directory!");
		}
		setProjectPath(getWorkingPath()+project.getId().get().toString()+"/");
	}
	
	/**
	 * Creates working path, where actual Project-Files are stored.
	 * @return true, if directory can be created, false, if it already exists
	 */
	private boolean createWorkingPath(){
		File f = new File(getWorkingPath());
		if(f.exists() && f.isDirectory()){
			return true;
		}
		return f.mkdir();
	}
	
	/**
	 * Creates sqlite db path, where the actual sqlite db is stored.
	 * @return true, if directory can be created, false, if it already exists
	 */
	public boolean createSQLitePath(){
		File f = new File(getSQLitePath());
		if(f.exists() && f.isDirectory()){
			return true;
		}
		return f.getParentFile().mkdirs();
	}

	/**
	 * This method is used to create a new Project (File-Folder on Explorer).
	 * @param project
	 * @return True if directory already exists or could be created successfully.
	 */
	private boolean createProjectPath(Project project) {
		File f = new File(getWorkingPath()+project.getId().get().toString());
		if(f.exists() && f.isDirectory()){
			return true;
		}
		return f.mkdir();
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
		if(getFileExtension(file).equals("jpg")){
			if(new File(getProjectPath()+file.getName()).exists()){
				throw new FileHandlerException("File already exists!");
			}
			File newFile = new File(getProjectPath()+file.getName());
			try {
				Files.copy(file, newFile);
			} catch (IOException e) {
				throw new FileHandlerException("Could not import File (Copy Fail)!",e);
			}
			photo.setFilePath(newFile.getAbsolutePath());
			photo.setFileFormat(FileFormat.JPEG);	//static at the moment
			photo.setFileSize((int) file.length());
			photo.setCreationDate(LocalDateTime.now());
			return photo;
		}else{
			throw new FileHandlerException("File Extension is invalid!");
		}
	}
	
	/**
	 * Method to get FileExtension to check for valid FileExtension jpg.
	 * @param file
	 * @return Returns the Extension of the File.
	 */
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
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
	 */
	public File exportZip(String zipName, List<Photo> list) throws FileHandlerException {
		try(FileOutputStream fos = new FileOutputStream(zipName);
			ZipOutputStream zos = new ZipOutputStream(fos);){
		
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
	 * 
	 * @throws FileHandlerException
	 */
	public void archiveProject() throws FileHandlerException {
		File archiveDir = new File(archivePath);
		File projectDir = new File(projectPath);
		File targetDir = new File(archivePath+project.getId().get().toString()+"/");
		if(!archiveDir.exists()){
			archiveDir.mkdir();
		}
		if(!targetDir.exists()){
			try {
				Files.move(projectDir, targetDir);
			} catch (IOException e) {
				throw new FileHandlerException("Projectfiles could not be moved to archive!",e);
			}
		}
		System.out.println("Project successfully archived!");
	}
	

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public String getSQLitePath() {
		return sqlitePath;
	}
	
	public File getSQLiteFile() {
		return sqliteFile;
	}

	public String getUserHomePath() {
		return userHomePath;
	}

	public String getWorkingPath() {
		return workingPath;
	}

}
