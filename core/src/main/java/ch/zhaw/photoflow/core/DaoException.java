package ch.zhaw.photoflow.core;

@SuppressWarnings("serial")
public class DaoException extends Exception {
	
	public DaoException(String message) {
		super(message);
	}
	
	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
