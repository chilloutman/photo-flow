package ch.zhaw.photoflow.core.domain;

import java.util.Collection;

/**
 * Manages state transition in the project workflow states.
 */
public class ProjectWorkflow extends AbstractWorkflow<ProjectState> {
	
	public ProjectWorkflow () {
	}
	
	// TODO: This could be improved by return some kind of message that indicates *why* the transition can't take place.
	public boolean canTransition(Project project, Collection<Photo> photos, ProjectState nextState) {
		return canTransition(project.getState(), nextState, () ->
			photos.stream().map(Photo::getState).allMatch(nextState::isValidPhotoState)
		);
	}
	
	public void transition(Project project, Collection<Photo> photos, ProjectState nextState) {
		transition(canTransition(project, photos, nextState), () -> {
			project.setState(nextState);
		});
	}
	
}
