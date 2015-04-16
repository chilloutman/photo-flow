package ch.zhaw.photoflow.core.domain;

import static ch.zhaw.photoflow.core.domain.Project.newProject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ProjectTest {
	
	public static final Integer TEST_ID = 42;
	public static final String TEST_NAME = "TEST_DESCRIPTION";
	public static final String TEST_DESCRIPTION = "TEST_DESCRIPTION";
	public static final List<Todo> TEST_TODOS = ImmutableList.of(
		new Todo("todo 1"),
		new Todo("todo 2")
	);
	
	@Test
	public void idIsAbsentButNotNull() {
		Project project = newProject(p -> {});
		assertThat(project.getId(), notNullValue());
		assertThat(project.getId().isPresent(), is(false));
	}
	
	@Test
	public void copy() {
		Project project = Project.copy(newProject(p -> {
			p.setId(TEST_ID);
			p.setName(TEST_NAME);
			p.setDescription(TEST_DESCRIPTION);
			p.setTodos(TEST_TODOS);
		}));
		
		assertThat(project.getId().get(), is(TEST_ID));
		assertThat(project.getName(), is(TEST_NAME));
		assertThat(project.getDescription(), is(TEST_DESCRIPTION));
		assertThat(project.getTodos(), is(TEST_TODOS));
	}

}
