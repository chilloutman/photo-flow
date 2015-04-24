package ch.zhaw.photoflow.core.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PhotoWorkflowTest {

	private PhotoWorkflow workflow;
	private Project project;
	private Photo photo;
	
	@Before
	public void before() {
		workflow = new PhotoWorkflow();
		project = Project.newProject();
		photo = Photo.newPhoto();
	}
	
	@Test(expected = IllegalStateException.class)
	public void toFlaggedDuringNew() {
		workflow.transition(project, photo, PhotoState.FLAGGED);
	}
	
	@Test
	public void toFlaggedDuringInWork() {
		project.setState(ProjectState.IN_WORK);
		
		workflow.transition(project, photo, PhotoState.FLAGGED);
		assertThat(photo.getState(), is(PhotoState.FLAGGED));
	}
	
	@Test
	public void toDiscardedDuringInWork() {
		project.setState(ProjectState.IN_WORK);
		
		workflow.transition(project, photo, PhotoState.DISCARDED);
		assertThat(photo.getState(), is(PhotoState.DISCARDED));
	}
	
	@Test
	public void fromFlaggedToEditDuringInWork() {
		project.setState(ProjectState.IN_WORK);
		photo.setState(PhotoState.FLAGGED);
		
		workflow.transition(project, photo, PhotoState.EDITING);
		assertThat(photo.getState(), is(PhotoState.EDITING));
	}

}
