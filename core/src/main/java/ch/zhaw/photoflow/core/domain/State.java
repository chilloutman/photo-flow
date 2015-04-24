package ch.zhaw.photoflow.core.domain;

public interface State<T extends State<T>> {
	
	public boolean isValidNextState(T state);
	
}
