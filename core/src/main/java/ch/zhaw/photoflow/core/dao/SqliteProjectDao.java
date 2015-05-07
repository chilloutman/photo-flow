package ch.zhaw.photoflow.core.dao;

import java.sql.Connection;
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

import static ch.zhaw.photoflow.core.util.GuavaCollectors.toImmutableList;


public class SqliteProjectDao implements ProjectDao {

	@Override
	public ImmutableList<Project> loadAll() {
		
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
			System.out.println("Could not connect to the database.");
			e.printStackTrace();
		}
		
		return projectList;
	}

	@Override
	public Optional<Project> load(int id) throws DaoException {
		Connection sqliteConnection;
		try {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Optional.empty();
	}
	
	@Override
	public Project save(Project project) throws SQLException {
		
			Connection sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			
			Statement stmt = sqliteConnection.createStatement();

			if (project.getId().isPresent()) {
				//Update
				String updateSQL = "UPDATE project " +
				" SET name = '" + project.getName() +
				"', description = '" + project.getDescription() +
				"', status = '" + project.getState().toString() +
				"' WHERE ID = " + project.getId().get();
				
				stmt.executeUpdate(updateSQL);
				sqliteConnection.commit();
				
			}
			else {
				//Insert
				String insertSQL = "INSERT INTO project(name, description, status) " +
				"VALUES( '" + project.getName() + "', '"
				+ project.getDescription() + "', '"
				+ project.getState().toString() + "')";

				stmt.executeUpdate(insertSQL);
				sqliteConnection.commit();

				ResultSet rs = stmt.getGeneratedKeys();
				rs.next();
				project.setId(rs.getInt(1));
			}
		
		return project;
	}

	@Override
	public void delete(Project project) throws SQLException {
		
			if (project.getId().isPresent()) {
				Connection sqliteConnection = SQLiteConnection.getConnection();
				sqliteConnection.setAutoCommit(false);
				Statement stmt = sqliteConnection.createStatement();
			
				String deleteSQL = "DELETE FROM project " +
						" WHERE ID = " + project.getId().get();
				
				stmt.executeUpdate(deleteSQL);
				sqliteConnection.commit();
			}
			
	}

}
