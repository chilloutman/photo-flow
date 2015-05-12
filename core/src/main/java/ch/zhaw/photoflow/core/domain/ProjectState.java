package ch.zhaw.photoflow.core.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

/**
 * Represents the state of a {@link Project} inside the {@link ProjectWorkflow}.
 */
public enum ProjectState implements State<ProjectState> {
	NEW("New") {
		@Override
		Collection<ProjectState> nextStates() {
			return ImmutableSet.of(IN_WORK);
		}
		
		@Override
		Collection<PhotoState> photoStates() {
			return ImmutableSet.of(PhotoState.NEW);
		}
	},
	IN_WORK("In Work") {
		@Override
		Collection<ProjectState> nextStates() {
			return ImmutableSet.of(PAUSED, DONE);
		}
		
		@Override
		Collection<PhotoState> photoStates() {
			return ImmutableSet.copyOf(PhotoState.values());
		}
	},
	PAUSED("Paused") {
		@Override
		Collection<ProjectState> nextStates() {
			return ImmutableSet.of(IN_WORK);
		}
		
		@Override
		Collection<PhotoState> photoStates() {
			return ImmutableSet.copyOf(PhotoState.values());
		}
	},
	DONE("Done") {
		@Override
		Collection<ProjectState> nextStates() {
			return ImmutableSet.of(IN_WORK, ARCHIVED);
		}
		
		@Override
		Collection<PhotoState> photoStates() {
			return ImmutableSet.of(PhotoState.FLAGGED, PhotoState.DISCARDED);
		}
	},
	ARCHIVED("Archived") {
		@Override
		Collection<ProjectState> nextStates() {
			return ImmutableSet.of(IN_WORK);
		}
		
		@Override
		Collection<PhotoState> photoStates() {
			return ImmutableSet.of(PhotoState.FLAGGED, PhotoState.DISCARDED);
		}
	};
	
	private String name;
	
	private ProjectState(String name) {
		this.name = checkNotNull(name);
	}
	
	abstract Collection<ProjectState> nextStates();
	
	abstract Collection<PhotoState> photoStates();
	
	@Override
	public boolean isValidNextState(ProjectState state) {
		return nextStates().contains(state);
	}
	
	/**
	 * Checks if a photo may step to the next desired state.
	 * @param {@link PhotoState} state
	 * @return
	 */
	public boolean isValidPhotoState(PhotoState state) {
		return photoStates().contains(state);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
