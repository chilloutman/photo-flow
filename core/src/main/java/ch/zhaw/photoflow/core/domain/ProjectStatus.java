package ch.zhaw.photoflow.core.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.collect.Sets;

public enum ProjectStatus {
	NEW("New", Sets.newHashSet(PhotoStatus.NEW)),
	IN_WORK("In Work", Sets.newHashSet(PhotoStatus.values())),
	PAUSED("Paused", Sets.newHashSet(PhotoStatus.values())),
	DONE("Done", Sets.newHashSet(PhotoStatus.FLAGGED, PhotoStatus.DISCARDED)),
	ARCHIEVED("Archived", Sets.newHashSet(PhotoStatus.FLAGGED, PhotoStatus.DISCARDED));
	
	private String name;
	
	private Collection<PhotoStatus> photoStatuses;
	
	private ProjectStatus(String name, Collection<PhotoStatus> photoStatuses) {
		this.name = checkNotNull(name);
		this.photoStatuses = checkNotNull(photoStatuses);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isValidPhotoStatus(PhotoStatus status) {
		return photoStatuses.contains(status);
	}
}
