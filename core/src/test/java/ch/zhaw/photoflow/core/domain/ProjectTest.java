package ch.zhaw.photoflow.core.domain;

import static ch.zhaw.photoflow.core.domain.Project.newProject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ProjectTest {
	
	public static final Integer ID = 42;
	public static final String NAME = "TEST_DESCRIPTION";
	public static final String DESCRIPTION = "TEST_DESCRIPTION";
	public static final List<Todo> TODOS = ImmutableList.of(
		new Todo("todo 1"),
		new Todo("todo 2")
	);
	
	private Project project;
	
	@Before
	public void before() {
		project = Project.copy(newProject(p -> {
			p.setId(ID);
			p.setName(NAME);
			p.setDescription(DESCRIPTION);
			TODOS.forEach(p::addTodo);
		}));

	}
	
	@Test
	public void idIsAbsentButNotNull() {
		Project project = newProject(p -> {});
		assertThat(project.getId(), notNullValue());
		assertThat(project.getId().isPresent(), is(false));
	}
	
	@Test
	public void copy() {
		Project copy = Project.copy(project);
		assertThat(copy.getId().get(), is(ID));
		assertThat(copy.getName(), is(NAME));
		assertThat(copy.getDescription(), is(DESCRIPTION));
		assertThat(copy.getTodos(), is(TODOS));
	}
	
}
