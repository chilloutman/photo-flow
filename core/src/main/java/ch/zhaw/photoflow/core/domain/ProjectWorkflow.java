package ch.zhaw.photoflow.core.domain;

import java.util.Collection;

/**
 * Manages state transition in the project workflow states.
 */
public class ProjectWorkflow extends AbstractWorkflow<ProjectState> {
	
	public ProjectWorkflow () {
	}
	
	/**
	 * Validates the transition to the next state.
	 * @param project
	 * @param photos
	 * @param nextState
	 * @return <b>true</b> if transition to the next state is valid else <b>false</b>
	 */
	public boolean canTransition(Project project, Collection<Photo> photos, ProjectState nextState) {
		return canTransition(project.getState(), nextState, () ->
			!photos.isEmpty() && photos.stream().map(Photo::getState).allMatch(nextState::isValidPhotoState)
		);
	}
	
	/**
	 * Transists if valid to the next desired state.
	 * @param project
	 * @param photos
	 * @param nextState
	 */
	public void transition(Project project, Collection<Photo> photos, ProjectState nextState) {
		transition(canTransition(project, photos, nextState), () -> {
			project.setState(nextState);
		});
	}
	
}
