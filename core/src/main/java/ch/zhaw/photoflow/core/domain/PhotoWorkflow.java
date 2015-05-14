package ch.zhaw.photoflow.core.domain;

/**
 * Manages state transition in the photo workflow states.
 */
public class PhotoWorkflow extends AbstractWorkflow<PhotoState> {
	
	/**
	 * Check if a transition is possible.
	 * @param project The project that the photo belongs to.
	 * @param photo The photo to check the transition for.
	 * @param nextState The state to check the transition to.
	 * @return {@code true} if {@link #transition(Project, Photo, PhotoState)} could be executed.
	 */
	// TODO: This could be improved by return some kind of message that indicates *why* the transition can't take place.
	public boolean canTransition(Project project, Photo photo, PhotoState nextState) {
		return canTransition(photo.getState(), nextState, () ->
			project.getState().isValidPhotoState(nextState)
		);
	}
	
	/**
	 * Execute a transition.
	 * @param project The project that the photo belongs to.
	 * @param photo The photo to transition.
	 * @param nextState The state to transition to.
	 */
	public void transition(Project project, Photo photo, PhotoState nextState) {
		transition(canTransition(project, photo, nextState), () -> {
			photo.setState(nextState);
		});
	}
	
	public boolean canFlag(Project project, Photo photo) {
		return canTransition(project, photo, PhotoState.FLAGGED);
	}
	
	public boolean canDiscard(Project project, Photo photo) {
		return canTransition(project, photo, PhotoState.DISCARDED);
	}
	
	public void flag(Project project, Photo photo) {
		transition(project, photo, PhotoState.FLAGGED);
	}
	
	public void discard(Project project, Photo photo) {
		transition(project, photo, PhotoState.DISCARDED);
	}
	
}
