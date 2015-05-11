package ch.zhaw.photoflow.core.domain;

/**
 * Interface for all possible states of objects.
 * @param <T> Class which is a representer for object states.
 */
public interface State<T extends State<T>> {
	
	/**
	 * Checks if step to the next desired state is valid.
	 * @param {@link T} state
	 * @return <b>true</b> if step is valid else <b>false</b>.
	 */
	public boolean isValidNextState(T state);
	
}
