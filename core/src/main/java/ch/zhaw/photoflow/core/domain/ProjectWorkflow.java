package ch.zhaw.photoflow.core.domain;

import java.util.Collection;

public class ProjectWorkflow {
	
	// TODO: This could be improved by return some kind of message that indicates *why* the transition can't take place.
	public static boolean canTransition(Project project, Collection<Photo> photos, ProjectState nextState) {
		if (!project.getState().isValidNextState(nextState)) {
			return false;
		}
		
		if (!photos.stream().map(Photo::getState).allMatch(nextState::isValidPhotoState)) {
			return false;
		}
		
		return true;
	}
	
	public static void transition(Project project, Collection<Photo> photos, ProjectState nextState) {
		if (!canTransition(project, photos, nextState)) {
			throw new IllegalStateException("Invalid transition from " + project.getState() + " to " + nextState + ". Call canTransition() first!");
		}
		project.setState(nextState);
	}
	
}
