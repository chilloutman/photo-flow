package ch.zhaw.photoflow.core.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PhotoWorkflowTest {

	private Project project;
	private Photo photo;
	
	@Before
	public void before() {
		project = Project.newProject();
		photo = Photo.newPhoto();
	}
	
	@Test(expected = IllegalStateException.class)
	public void toFlaggedDuringNew() {
		PhotoWorkflow.transistion(project, photo, PhotoState.FLAGGED);
	}
	
	@Test
	public void toFlaggedDuringInWork() {
		project.setState(ProjectState.IN_WORK);
		
		PhotoWorkflow.transistion(project, photo, PhotoState.FLAGGED);
		assertThat(photo.getState(), is(PhotoState.FLAGGED));
	}
	
	@Test
	public void toDiscardedDuringInWork() {
		project.setState(ProjectState.IN_WORK);
		
		PhotoWorkflow.transistion(project, photo, PhotoState.DISCARDED);
		assertThat(photo.getState(), is(PhotoState.DISCARDED));
	}
	
	@Test
	public void fromFlaggedToEditDuringInWork() {
		project.setState(ProjectState.IN_WORK);
		photo.setState(PhotoState.FLAGGED);
		
		PhotoWorkflow.transistion(project, photo, PhotoState.EDITING);
		assertThat(photo.getState(), is(PhotoState.EDITING));
	}

}
