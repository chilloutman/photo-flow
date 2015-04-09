package ch.zhaw.photoflow.core.domain;

public enum PhotoStatus {
	NEW("New"),
	FLAGGED("Flagged"),
	EDITING("Editing"),
	DISCARDED("Discarded");
	
	private String name;
	
	private PhotoStatus(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
