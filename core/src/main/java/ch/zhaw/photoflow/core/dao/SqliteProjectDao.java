package ch.zhaw.photoflow.core.dao;

import static ch.zhaw.photoflow.core.util.GuavaCollectors.toImmutableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.ProjectDao;
import ch.zhaw.photoflow.core.SQLiteConnection;
import ch.zhaw.photoflow.core.domain.Project;
import ch.zhaw.photoflow.core.domain.ProjectState;

import com.google.common.collect.ImmutableList;


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
			
		} catch (SQLException e) {
			throw new DaoException("Error in loading all elements", e);
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
			throw new DaoException("Error in loading", e);
		}
		
	}
	
	@Override
	public Project save(Project project) throws DaoException {
		
			try {
				Connection sqliteConnection = SQLiteConnection.getConnection();
				sqliteConnection.setAutoCommit(false);
				if (project.getId().isPresent()) {
					//Update
					
					String updateSQL = "UPDATE project SET name = ?, description = ?, status = ? WHERE ID = ?";
					PreparedStatement prepstmt = sqliteConnection.prepareStatement(updateSQL);
					prepstmt.setString(1, project.getName());
					prepstmt.setString(2, project.getDescription());
					prepstmt.setString(3, (project.getState() == null ) ? "" : project.getState().toString());
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
					String projectstate = (project.getState() == null ) ? "" : project.getState().toString();
					prepstmt.setString(3, projectstate);
					
					prepstmt.executeUpdate();
					sqliteConnection.commit();
					
					ResultSet rs = prepstmt.getGeneratedKeys();
					rs.next();
					project.setId(rs.getInt(1));
				}
				
				return project;
			} catch (SQLException e) {
				throw new DaoException("Error in Saving", e);
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
					throw new DaoException("Error in deleting", e);
				}
			}
			
	}

}
