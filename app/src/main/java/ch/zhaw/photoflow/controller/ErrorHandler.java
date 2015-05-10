package ch.zhaw.photoflow.controller;

import org.controlsfx.control.Notifications;

public class ErrorHandler {

	public void spawnWarining(String text)
	{
		Notifications.create()
		.darkStyle()
        .title("Warning")
        .text(text)
        .showWarning();
	}
	
	public void spawnInformation(String text)
	{
		Notifications.create()
		.darkStyle()
        .title("Success")
        .text(text)
        .showInformation();
	}
	
	public void spawnError(String text)
	{
		Notifications.create()
		.darkStyle()
        .title("Error")
        .text(text)
        .showError();
	}
}