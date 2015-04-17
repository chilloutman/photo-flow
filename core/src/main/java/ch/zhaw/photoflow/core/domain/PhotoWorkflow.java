package ch.zhaw.photoflow.core.domain;

public class PhotoWorkflow {
	
	// TODO: This could be improved by return some kind of message that indicates *why* the transition can't take place.
	public static boolean canTransition(Project project, Photo photo, PhotoState nextState) {
		if (!project.getState().isValidPhotoState(nextState)) {
			return false;
		}
		
		if (!photo.getState().isValidNextState(nextState)) {
			return false;
		}
		
		return true;
	}
	
	public static void transistion(Project project, Photo photo, PhotoState nextState) {
		if (!canTransition(project, photo, nextState)) {
			throw new IllegalStateException("Invalid photos state transition from " + photo.getState() + " to " + nextState + " during project state " + project.getState() + ". Call canTransition() first!");
		}
		photo.setState(nextState);
	}
	
}
