package ch.zhaw.photoflow.core.dao;

/**
 * Thrown for any unrecoverable underlying problem with a storage layer.
 */
@SuppressWarnings("serial")
public class DaoException extends Exception {
	
	/**
	 * @see Exception#Exception(String)
	 */
	public DaoException(String message) {
		super(message);
	}
	
	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
