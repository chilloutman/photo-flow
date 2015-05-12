package ch.zhaw.photoflow.core.dao;

import static ch.zhaw.photoflow.core.util.GuavaCollectors.toImmutableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.SQLiteConnection;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;
import ch.zhaw.photoflow.core.domain.Todo;

import com.google.common.collect.ImmutableList;

/**
 * SQLite implementation of the {@link ProjectDao}
 */
public class SqliteProjectDao implements ProjectDao {

	@Override
	public ImmutableList<Project> loadAll() throws DaoException {
		
		ImmutableList<Project> projectList = ImmutableList.of();

		try {
			Connection sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			
			DSLContext create = DSL.using(sqliteConnection, SQLDialect.SQLITE);

			//Load Data and create ImmutableList<Project>
			projectList = create.select().from("project").fetch().stream().<Project>map(record -> {
				return Project.newProject(p -> {
					p.setId((int)record.getValue("ID"));
					p.setName((String)record.getValue("name"));
					p.setDescription((String)record.getValue("description"));
					p.setState(ProjectState.valueOf((String)record.getValue("status")));
				});
			}).collect(toImmutableList());
			

			for (Project project : projectList) {
				project.addTodos(loadAllTodosByProject(project));
			}
			
		} catch (SQLException e) {
			throw new DaoException("Error in loading all project elements", e);
		}
		
		return projectList;
	}

	@Override
	public Optional<Project> load(int id) throws DaoException {

		try {
			Connection sqliteConnection;
			sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			
			DSLContext create = DSL.using(sqliteConnection, SQLDialect.SQLITE);

			Optional<Project> project = create.select().from("project").where("ID = " + id).fetch().stream().<Project>map(
					record -> {
						return Project.newProject(p -> {
							p.setId(id);
							p.setName((String)record.getValue("name"));
							p.setDescription((String)record.getValue("description"));
							p.setState(ProjectState.valueOf((String)record.getValue("status")));
						});
					}).findFirst();
			
			return project;
			
		} catch (SQLException e) {
			throw new DaoException("Error in loading project", e);
		}
		
	}
	
	@Override
	public Project save(Project project) throws DaoException {
		
			try {
				Connection sqliteConnection = SQLiteConnection.getConnection();
				sqliteConnection.setAutoCommit(false);
				
				//Store Project
				if (project.getId().isPresent()) {
					//Update
					
					String updateSQL = "UPDATE project SET name = ?, description = ?, status = ? WHERE ID = ?";
					PreparedStatement prepstmt = sqliteConnection.prepareStatement(updateSQL);
					prepstmt.setString(1, project.getName());
					prepstmt.setString(2, project.getDescription());
					prepstmt.setString(3, (project.getState() == null ) ? "" : project.getState().name());
					prepstmt.setInt(4, project.getId().get());
					
					prepstmt.executeUpdate();
					sqliteConnection.commit();
					
				}
				else {
					//Insert
					
					String insertSQL = "INSERT INTO project (name,description,status) VALUES(?,?,?)";
					PreparedStatement prepstmt = sqliteConnection.prepareStatement(insertSQL);
					prepstmt.setString(1, project.getName());
					prepstmt.setString(2, project.getDescription());
					String projectstate = (project.getState() == null ) ? "" : project.getState().name();
					prepstmt.setString(3, projectstate);
					
					prepstmt.executeUpdate();
					sqliteConnection.commit();
					
					ResultSet rs = prepstmt.getGeneratedKeys();
					rs.next();
					project.setId(rs.getInt(1));
				}
				
				return project;
			} catch (SQLException e) {
				throw new DaoException("Error in Saving project", e);
			}
			

	}
	
	@Override
	public void delete(Project project) throws DaoException {
		
		if (project.getId().isPresent()) {
			Connection sqliteConnection;
			try {
				sqliteConnection = SQLiteConnection.getConnection();
				sqliteConnection.setAutoCommit(false);
				Statement stmt = sqliteConnection.createStatement();
				
				String deleteSQL = "DELETE FROM project " +
						" WHERE ID = " + project.getId().get();
				
				stmt.executeUpdate(deleteSQL);
				sqliteConnection.commit();
			} catch (SQLException e) {
				throw new DaoException("Error in deleting project", e);
			}
		}
		
	}
	
	@Override
	public List<Todo> loadAllTodosByProject(Project project) throws DaoException {
		List<Todo> todoList = new ArrayList<Todo>();

		if (project == null) {
			return todoList;
		}
		
		if (! project.getId().isPresent()) {
			return todoList;
		}
		
		try {
			Connection sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			
			DSLContext create = DSL.using(sqliteConnection, SQLDialect.SQLITE);
			
			//Load Data and create ImmutableList<Project>
			todoList = create.select().from("todo").where("project_fk = " + project.getId().get()).fetch().stream().<Todo>map(record -> {
				Todo todo = new Todo((String)record.getValue("description"));
				todo.setId((int)record.getValue("project_fk"));
				todo.setId((int)record.getValue("ID"));
				todo.setChecked(((int)record.getValue("checked")) == 1 ? true : false);
				
				return todo;
				
			}).collect(Collectors.toList());
			
		} catch (SQLException e) {
			throw new DaoException("Error in loading all todo elements", e);
		}
		
		return todoList;
	}
	
	@Override
	public Optional<Todo> loadTodo(int id) throws DaoException {

		try {
			Connection sqliteConnection;
			sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			
			DSLContext create = DSL.using(sqliteConnection, SQLDialect.SQLITE);

			Optional<Todo> todo = create.select().from("todo").where("ID = " + id).fetch().stream().<Todo>map(
					record -> {
						Todo tempTodo = new Todo((String)record.getValue("description"));
						tempTodo.setId((int)record.getValue("project_fk"));
						tempTodo.setId((int)record.getValue("ID"));
						tempTodo.setChecked(((int)record.getValue("checked")) == 1 ? true : false);
						
						return tempTodo;
						
					}).findFirst();
			
			return todo;
			
		} catch (SQLException e) {
			throw new DaoException("Error in loading todo", e);
		}
		
	}
	
	@Override
	public Todo saveTodo(Project project, Todo todo) throws DaoException {
		
		Connection sqliteConnection;
		
		try {
			sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			
			if (todo.getId().isPresent()) {
				//Update
				String updateSQL = "UPDATE todo SET project_fk = ?, description = ?, checked = ? WHERE ID = ?";
				PreparedStatement prepstmt = sqliteConnection.prepareStatement(updateSQL);
				prepstmt.setInt(1, project.getId().get());
				prepstmt.setString(2, todo.getDescription());
				prepstmt.setInt(3, (todo.isChecked()) ? 1 : 0);
				prepstmt.setInt(4, todo.getId().get());
				
				prepstmt.executeUpdate();
				sqliteConnection.commit();
			}
			else {
				//Insert
				String insertSQL = "INSERT INTO todo (project_fk, description, checked) VALUES(?,?,?)";
				PreparedStatement prepstmt = sqliteConnection.prepareStatement(insertSQL);
				
				prepstmt.setInt(1, project.getId().get());
				prepstmt.setString(2, todo.getDescription());
				prepstmt.setInt(3, (todo.isChecked()) ? 1 : 0);
				
				prepstmt.executeUpdate();
				sqliteConnection.commit();
				
				ResultSet rs = prepstmt.getGeneratedKeys();
				rs.next();
				todo.setId(rs.getInt(1));
			}
			
			return todo;
			
		} catch (SQLException e) {
			throw new DaoException("Error in Saving todo", e);
		}
	}
	
	@Override
	public void deleteTodo(Todo todo) throws DaoException {
		
		if (todo.getId().isPresent()) {
			Connection sqliteConnection;
			try {
				sqliteConnection = SQLiteConnection.getConnection();
				sqliteConnection.setAutoCommit(false);
				Statement stmt = sqliteConnection.createStatement();
				
				String deleteSQL = "DELETE FROM todo " +
						" WHERE ID = " + todo.getId().get();
				
				stmt.executeUpdate(deleteSQL);
				sqliteConnection.commit();
			} catch (SQLException e) {
				throw new DaoException("Error in deleting todo", e);
			}
		}
		
}


}
