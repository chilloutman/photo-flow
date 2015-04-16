package ch.zhaw.photoflow.core.domain;

import java.util.Date;
import java.util.List;

public class Photo {

	private String filePath;
	private int fileSize;
	private FileFormat fileFormat;
	private Date creationDate;
	private PhotoStatus status;
	private Photographer photographer;
	private List<Tag> tags;
	
	public Photo() {
		
	}
	
	
	
	/************ GETTERS AND SETTERS ************/
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public FileFormat getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(FileFormat fileFormat) {
		this.fileFormat = fileFormat;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public PhotoStatus getStatus() {
		return status;
	}

	public void setStatus(PhotoStatus status) {
		this.status = status;
	}

	public Photographer getPhotographer() {
		return photographer;
	}

	public void setPhotographer(Photographer photographer) {
		this.photographer = photographer;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
}
