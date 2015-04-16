package ch.zhaw.photoflow.core.domain;

public class Photographer {

	private String firstName;
	private String lastName;
	
	public Photographer() {
		
	}

	
	/************ GETTERS AND SETTERS ************/
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
