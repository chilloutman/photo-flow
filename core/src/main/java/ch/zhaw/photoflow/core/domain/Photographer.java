package ch.zhaw.photoflow.core.domain;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Model Representation of a photographer.
 */
public class Photographer {

	private final String firstName;
	private final String lastName;
	
	/**
	 * Create a new immutable instance of {@link Photographer}.
	 * @param firstName {@link #getFirstName()}.
	 * @param lastName {@link #getLastName()}.
	 */
	public Photographer(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		Photographer that = (Photographer) object;
		
		return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("firstName", firstName)
			.add("lastName", lastName)
			.toString();
	}
	
}
