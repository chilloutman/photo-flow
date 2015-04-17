package ch.zhaw.photoflow.core.domain;

import static java.util.Collections.emptyList;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class ProjectWorkflowTest {

	private Project project;
	private Photo newPhoto;
	private Photo flaggedPhoto;
	private Photo discardedPhoto;
	
	@Before
	public void before() {
		project = Project.newProject();
		newPhoto = Photo.newPhoto();
		flaggedPhoto = Photo.newPhoto(p -> {
			p.setState(PhotoState.FLAGGED);
		});
		discardedPhoto = Photo.newPhoto(p -> {
			p.setState(PhotoState.DISCARDED);
		});
	}
	
	@Test
	public void toDone() {
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.IN_WORK);
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.DONE);
	}
	
	@Test
	public void toDonewithPause() {
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.IN_WORK);
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.PAUSED);
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.IN_WORK);
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.DONE);
	}
	
	@Test
	public void toDoneAndBack() {
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.IN_WORK);
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.DONE);
		ProjectWorkflow.transistion(project, emptyList(), ProjectState.IN_WORK);
	}

}
