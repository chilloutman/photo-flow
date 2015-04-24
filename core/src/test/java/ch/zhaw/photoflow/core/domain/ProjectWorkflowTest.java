package ch.zhaw.photoflow.core.domain;

import static java.util.Collections.emptyList;

import org.junit.Before;
import org.junit.Test;

public class ProjectWorkflowTest {

	private ProjectWorkflow workflow;
	private Project project;
	private Photo newPhoto;
	private Photo flaggedPhoto;
	private Photo discardedPhoto;
	
	@Before
	public void before() {
		workflow = new ProjectWorkflow();
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
		workflow.transition(project, emptyList(), ProjectState.IN_WORK);
		workflow.transition(project, emptyList(), ProjectState.DONE);
	}
	
	@Test
	public void toDonewithPause() {
		workflow.transition(project, emptyList(), ProjectState.IN_WORK);
		workflow.transition(project, emptyList(), ProjectState.PAUSED);
		workflow.transition(project, emptyList(), ProjectState.IN_WORK);
		workflow.transition(project, emptyList(), ProjectState.DONE);
	}
	
	@Test
	public void toDoneAndBack() {
		workflow.transition(project, emptyList(), ProjectState.IN_WORK);
		workflow.transition(project, emptyList(), ProjectState.DONE);
		workflow.transition(project, emptyList(), ProjectState.IN_WORK);
	}

}
