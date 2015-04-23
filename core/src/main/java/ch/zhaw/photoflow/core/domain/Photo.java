package ch.zhaw.photoflow.core.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

public class Photo {
	
	private Optional<Integer> id = Optional.empty();
	private Optional<Integer> projectId = Optional.empty();
	private String filePath;
	private Integer fileSize;
	private FileFormat fileFormat;
	private Date creationDate;
	private PhotoState state = PhotoState.NEW;
	private Photographer photographer;
	private List<Tag> tags = new ArrayList<>();
	
	public static Photo newPhoto () {
		return new Photo();
	}
	
	/**
	 * Conveniently create and configure a new instance.
	 * @param setUpProject configure function.
	 * @return the new instance.
	 */
	public static Photo newPhoto (Consumer<Photo> configurePhoto) {
		Photo p = newPhoto();
		configurePhoto.accept(p);
		return p;
	}
	
	/**
	 * Creates a new instance an copies all properties to it.
	 * @param photo the photo to copy the properties from.
	 * @return the new instance.
	 */
	public static Photo copy (Photo photo) {
		return newPhoto(p -> {
			p.id = photo.id;
			p.projectId = photo.projectId;
			p.filePath = photo.filePath;
			p.fileSize = photo.fileSize;
			p.fileFormat = photo.fileFormat;
			p.creationDate = photo.creationDate;
			p.state = photo.state;
			p.photographer = photo.photographer;
			p.tags = photo.tags;
		});
	}
	
	private Photo() {
		
	}
	
	/************ GETTERS AND SETTERS ************/
	public Optional<Integer> getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = Optional.of(id);
	}
	
	public Optional<Integer> getProjectId() {
		return projectId;
	}
	
	public void setProjectId (int id) {
		this.projectId = Optional.of(id);
	}
	
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

	public PhotoState getState() {
		return state;
	}

	/** Not public, so that only the Workflow can change it. */
	void setState(PhotoState state) {
		this.state = state;
	}

	public Photographer getPhotographer() {
		return photographer;
	}

	public void setPhotographer(Photographer photographer) {
		this.photographer = photographer;
	}

	public ImmutableList<Tag> getTags() {
		return ImmutableList.copyOf(tags);
	}

	public void addTag(Tag tag) {
		//TODO:Check if tag already exists in list. (DAO part does not belong here!)
		this.tags.add(tag);
	}
	
	public void removeTag(Tag tag) {
		this.tags.remove(tag);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	/**
	 * Only the id is relevant for equality.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		Photo that = (Photo) object;

		return Objects.equals(id, that.id);
	}
	
}
