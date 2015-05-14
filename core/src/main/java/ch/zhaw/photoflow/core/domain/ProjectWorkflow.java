package ch.zhaw.photoflow.core.domain;

import java.util.Collection;

/**
 * Manages state transition in the project workflow states.
 */
public class ProjectWorkflow extends AbstractWorkflow<ProjectState> {
	
	/**
	 * Validates the transition to the next state.
	 * @param project The project to validate.
	 * @param photos Used for additional validation.
	 * @param nextState Candidate for next state.
	 * @return {@code true} if transition to the next state is valid and {@link #transition(Project, Collection, ProjectState)} can be called.
	 */
	public boolean canTransition(Project project, Collection<Photo> photos, ProjectState nextState) {
		return canTransition(project.getState(), nextState, () ->
			!photos.isEmpty() && photos.stream().map(Photo::getState).allMatch(nextState::isValidPhotoState)
		);
	}
	
	/**
	 * Transitions the project to the given next state.
	 * This method should only be called when {@link #canTransition(Project, Collection, ProjectState)} returns {@code true}.
	 * @param project The project to transition.
	 * @param photos Used for additional validation.
	 * @param nextState The next state to transition the project to.
	 */
	public void transition(Project project, Collection<Photo> photos, ProjectState nextState) {
		transition(canTransition(project, photos, nextState), () -> {
			project.setState(nextState);
		});
	}
	
}
