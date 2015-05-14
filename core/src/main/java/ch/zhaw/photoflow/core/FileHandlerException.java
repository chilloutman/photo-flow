package ch.zhaw.photoflow.core;

/**
 * Thrown if directories(Working or Project-Directory) cannot be created.
 */
@SuppressWarnings("serial")
public class FileHandlerException extends Exception {
	
	/**
	 * @param message The error message.
	 * @see Exception#Exception(String)
	 */
	public FileHandlerException(String message) {
		super(message);
	}
	
	/**
	 * @param message The error message.
	 * @param cause the exception that caused this exception to be thrown.
	 * @see Exception#Exception(String, Throwable)
	 */
	public FileHandlerException(String message, Throwable cause) {
		super(message, cause);
	}
}
