package ch.zhaw.photoflow.controller;

import org.controlsfx.control.Notifications;

/**
 * Used for notifying the user about application events.
 */
public class EventHandler {

	/**
	 * @param text The warning message to be displayed to the user.
	 */
	public static void spawnWarning(String text) {
		Notifications.create()
			.darkStyle()
			.title("Warning")
			.text(text)
			.showWarning();
	}
	
	/**
	 * @param text The information message to be displayed to the user.
	 */
	public static void spawnInformation(String text) {
		Notifications.create()
			.darkStyle()
			.title("Success")
			.text(text)
			.showInformation();
	}
	
	/**
	 * @param text The error message to be displayed to the user.
	 */
	public static void spawnError(String text) {
		Notifications.create()
			.darkStyle()
			.title("Error")
			.text(text)
			.showError();
	}
	
	private EventHandler() {
		// Static utility class.
	}
}
