package ch.zhaw.photoflow.core;

/**
 * Thrown if directories(Working or Project-Directory) cannot be created.
 */
@SuppressWarnings("serial")
public class FileHandlerException extends Exception {
	
	/**
	 * @see Exception#Exception(String)
	 */
	public FileHandlerException(String message) {
		super(message);
	}
	
	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public FileHandlerException(String message, Throwable cause) {
		super(message, cause);
	}
}
