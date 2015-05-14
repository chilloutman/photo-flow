package ch.zhaw.photoflow.core.domain;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 *	Implementation of a model for tags.
 */
public class Tag {

	private final String name;
	
	/**
	 * Creates a new immutable instance of {@link Tag}.
	 * @param name {@link #getName()}
	 */
	public Tag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		Tag that = (Tag) object;
		
		return Objects.equals(name, that.name);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name)
			.toString();
	}
	
}
