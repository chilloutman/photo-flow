package ch.zhaw.photoflow.core.domain;

public class Todo {
	
	private String description;
	
	public Todo(String description) {
		this.description = description;
	}
	
	/************ GETTERS AND SETTERS ************/
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
