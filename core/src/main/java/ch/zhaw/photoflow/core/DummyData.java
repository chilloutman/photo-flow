package ch.zhaw.photoflow.core;

import static ch.zhaw.photoflow.core.domain.Photo.newPhoto;
import static ch.zhaw.photoflow.core.domain.Project.newProject;

import java.time.LocalDateTime;

import ch.zhaw.photoflow.core.dao.DaoException;
import ch.zhaw.photoflow.core.dao.PhotoDao;
import ch.zhaw.photoflow.core.dao.ProjectDao;
import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.Project;

import com.google.common.collect.ImmutableList;

/**
 * Generates dummy data.
 * Useful for developers.
 */
public final class DummyData {
	
	private static final ImmutableList<Project> PROJECTS = ImmutableList.of(
		newProject(p -> {
			p.setName("Secret Project");
			p.setDescription("TOP SECRET, MAN!");
		}),
		newProject(p -> {
			p.setName("Awesome Project");
			p.setDescription("Blah Blah Blah.");
		}),
		newProject(p -> {
			p.setName("Boring Project");
		})
	);
	
	private static final ImmutableList<Photo> PHOTOS = ImmutableList.of(
		newPhoto(p -> {
			p.setCreationDate(LocalDateTime.now());
			p.setFileFormat(FileFormat.JPEG);
			p.setProjectId(1);
		}),
		newPhoto(p -> {
			p.setCreationDate(LocalDateTime.now());
			p.setFileFormat(FileFormat.JPEG);
			p.setProjectId(1);
		}),
		newPhoto(p -> {
			p.setCreationDate(LocalDateTime.now());
			p.setFileFormat(FileFormat.JPEG);
			p.setProjectId(2);
		})
	);
	
	private DummyData() { }
	
	public static void addProjects(ProjectDao dao) {
		PROJECTS.forEach(p -> {
			try {
					dao.save(p);
			} catch (DaoException e) { throw new RuntimeException(e); }
		});
	}
	
	public static void addPhotos(PhotoDao dao, Project project) {
		PHOTOS.forEach(p -> {
			p.setProjectId(project.getId().get());
			try {
				dao.save(p);
			} catch (DaoException e) { throw new RuntimeException(e); }
		});
	}
	
}
