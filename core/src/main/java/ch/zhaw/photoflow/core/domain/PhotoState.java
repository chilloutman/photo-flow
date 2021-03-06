package ch.zhaw.photoflow.core.domain;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

/**
 * Represents the state of a {@link Photo} inside the {@link PhotoWorkflow}.
 */
public enum PhotoState implements State<PhotoState> {
	NEW("New") {
		@Override
		Collection<PhotoState> nextStates() {
			return ImmutableSet.of(FLAGGED, DISCARDED);
		}
	},
	FLAGGED("Flagged") {
		@Override
		Collection<PhotoState> nextStates() {
			return ImmutableSet.of(EDITING, DISCARDED);
		}
	},
	EDITING("Editing") {
		@Override
		Collection<PhotoState> nextStates() {
			return ImmutableSet.of(FLAGGED, DISCARDED);
		}
	},
	DISCARDED("Discarded") {
		@Override
		Collection<PhotoState> nextStates() {
			return ImmutableSet.of(FLAGGED);
		}
	};
	
	private String name;
	
	private PhotoState(String name) {
		this.name = name;
	}
	
	abstract Collection<PhotoState> nextStates();
	
	@Override
	public boolean isValidNextState(PhotoState state) {
		return nextStates().contains(state);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
