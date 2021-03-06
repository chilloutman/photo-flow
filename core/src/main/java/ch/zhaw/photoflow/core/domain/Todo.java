package ch.zhaw.photoflow.core.domain;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;

/**
 * Implementation of a model for todo tasks.
 */
public class Todo {
	
	private Optional<Integer> id = Optional.empty();
	private final String description;
	private boolean checked = false;
	
	public Todo(String description) {
		this.description = description;
	}
	
	public Optional<Integer> getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = Optional.of(id);
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	public int hashCode() {
		return description.hashCode();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		Todo that = (Todo) object;
		
		return Objects.equals(description, that.description);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("description", description)
			.toString();
	}

}
