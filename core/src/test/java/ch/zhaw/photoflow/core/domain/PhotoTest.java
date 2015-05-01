package ch.zhaw.photoflow.core.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class PhotoTest {
	
	public static final Integer ID = 42;
	public static final Integer PROJECT_ID = 100;
	public static final String FILE_PATH = "/some/file/path";
	public static final Integer FILE_SIZE = 9000;
	public static final FileFormat FILE_FORMAT = FileFormat.JPEG;
	public static final LocalDateTime CREATION_DATE = LocalDateTime.now();
	public static final PhotoState STATE = PhotoState.FLAGGED;
	public static final Photographer PHOTOGRAPHER = new Photographer("Chuck", "Norris");
	public static final ImmutableList<Tag> TAGS = ImmutableList.of(new Tag("AWESOME"), new Tag("TEST"));
	
	@Test
	public void idIsAbsentButNotNull() {
		Photo photo = Photo.newPhoto(p -> {});
		assertThat(photo.getId(), notNullValue());
		assertThat(photo.getId().isPresent(), is(false));
	}
	
	@Test
	public void copy() {
		Photo photo = Photo.copy(Photo.newPhoto(p -> {
			p.setId(ID);
			p.setProjectId(PROJECT_ID);
			p.setFilePath(FILE_PATH);
			p.setFileSize(FILE_SIZE);
			p.setFileFormat(FILE_FORMAT);
			p.setCreationDate(CREATION_DATE);
			p.setState(STATE);
			p.setPhotographer(PHOTOGRAPHER);
			TAGS.forEach(p::addTag);
		}));
		
		assertThat(photo.getId().get(), is(ID));
		assertThat(photo.getProjectId().get(), is(PROJECT_ID));
		assertThat(photo.getFilePath(), is(FILE_PATH));
		assertThat(photo.getFileSize(), is(FILE_SIZE));
		assertThat(photo.getFileFormat(), is(FILE_FORMAT));
		assertThat(photo.getCreationDate(), is(CREATION_DATE));
		assertThat(photo.getState(), is(STATE));
		assertThat(photo.getPhotographer(), is(PHOTOGRAPHER));
		assertThat(photo.getTags(), is(TAGS));
	}
	
}
