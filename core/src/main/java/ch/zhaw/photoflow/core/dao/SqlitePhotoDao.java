package ch.zhaw.photoflow.core.dao;

import static ch.zhaw.photoflow.core.util.GuavaCollectors.toImmutableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.SQLiteConnection;
import ch.zhaw.photoflow.core.domain.FileFormat;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoState;

import com.google.common.collect.ImmutableList;

/**
 * SQLite implementation of the {@link PhotoDao}
 */
public class SqlitePhotoDao implements PhotoDao {

	@Override
	public ImmutableList<Photo> loadAll(int projectId) throws DaoException {

		ImmutableList<Photo> photoList = ImmutableList.of();

		try {

			Connection sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			DSLContext create = DSL.using(sqliteConnection, SQLDialect.SQLITE);
			
			
			//Load Data and create ImmutableList<Photo>
			photoList = create.select().from("photo").where("project_fk = " + projectId).fetch().stream().<Photo>map(record -> {
				return Photo.newPhoto(p -> {
					p.setId((int)record.getValue("ID"));
					p.setProjectId(projectId);
					//photographer
					p.setFilePath((String)record.getValue("file_path_to_original"));
					p.setFileSize((int)record.getValue("file_size"));
					p.setFileFormat(FileFormat.valueOf((String)record.getValue("file_format")));
					p.setCreationDate(LocalDateTime.parse((String)record.getValue("timestamp")));
					//location_lat
					//location_lon
					p.setState(PhotoState.valueOf((String)record.getValue("status")));
				});
			}).collect(toImmutableList());
			
		} catch (SQLException e) {
			throw new DaoException("Error in loading all photo", e);
		}
		
		return photoList;
	}

	@Override
	public Optional<Photo> load(int id) throws DaoException {
		
		try {

			Connection sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			DSLContext create = DSL.using(sqliteConnection, SQLDialect.SQLITE);
			
			
			//Load Data and create ImmutableList<Photo>
			Optional<Photo> photo = create.select().from("photo").where("ID = " + id).fetch().stream().<Photo>map(record -> {
				return Photo.newPhoto(p -> {
					p.setId((int)record.getValue("ID"));
					p.setProjectId((int)record.getValue("project_fk"));
					//photographer
					p.setFilePath((String)record.getValue("file_path_to_original"));
					p.setFileSize((int)record.getValue("file_size"));
					p.setFileFormat(FileFormat.valueOf((String)record.getValue("file_format")));
					p.setCreationDate(LocalDateTime.parse((String)record.getValue("timestamp")));
					//location_lat
					//location_lon
					p.setState(PhotoState.valueOf((String)record.getValue("status")));
				});
			}).findFirst();
			
			return photo;
			
		} catch (SQLException e) {
			throw new DaoException("Error in loading photo", e);
		}
		
	}
	
	@Override
	public Photo save(Photo photo) throws DaoException {
		
		try {
			Connection sqliteConnection = SQLiteConnection.getConnection();
			sqliteConnection.setAutoCommit(false);
			
			Statement stmt = sqliteConnection.createStatement();

			if (photo.getId().isPresent()) {
				//Update
				
				String updateSQL = "UPDATE photo SET project_fk = ?, "
						+ "file_path_to_original = ?, "
						+ "file_size = ?, "
						+ "file_format = ?, "
						+ "timestamp = ?, "
						+ "status = ? "
						+ "WHERE ID = ?";
				
				PreparedStatement prepstmt = sqliteConnection.prepareStatement(updateSQL);
				prepstmt.setInt(1, photo.getProjectId().get());
				prepstmt.setString(2, photo.getFilePath());
				prepstmt.setInt(3, photo.getFileSize());
				prepstmt.setString(4, (photo.getFileFormat() == null ) ? "" : photo.getFileFormat().name());
				prepstmt.setString(5, (photo.getCreationDate() == null ) ? "" : photo.getCreationDate().toString());
				prepstmt.setString(6, (photo.getState() == null ) ? "" : photo.getState().name());
				prepstmt.setInt(7, photo.getId().get());
				
				prepstmt.executeUpdate();
				sqliteConnection.commit();
				
			}
			else {
				//Insert
				String insertSQL = "INSERT INTO photo(project_fk, "
						+ "file_path_to_original, "
						+ "file_size,"
						+ "file_format, "
						+ "timestamp, "
						+ "status) "
						+ "VALUES(?, ?, ?, ?, ?, ?)";
				PreparedStatement prepstmt = sqliteConnection.prepareStatement(insertSQL);
				prepstmt.setInt(1, photo.getProjectId().get());
				prepstmt.setString(2, photo.getFilePath());
				prepstmt.setInt(3, photo.getFileSize());
				prepstmt.setString(4, (photo.getFileFormat() == null ) ? "" : photo.getFileFormat().name());
				prepstmt.setString(5, (photo.getCreationDate() == null ) ? "" : photo.getCreationDate().toString());
				prepstmt.setString(6, (photo.getState() == null ) ? "" : photo.getState().name());
				
				prepstmt.executeUpdate();
				sqliteConnection.commit();

				ResultSet rs = stmt.getGeneratedKeys();
				rs.next();
				photo.setId(rs.getInt(1));
			}
			
		} catch (SQLException e) {
			throw new DaoException("Error in saving photo", e);
		}
		
		return photo;
	}

	@Override
	public void delete(Photo photo) throws DaoException {
		try {
			
			if (photo.getId().isPresent()) {
				Connection sqliteConnection = SQLiteConnection.getConnection();
				sqliteConnection.setAutoCommit(false);
				Statement stmt = sqliteConnection.createStatement();
			
				String deleteSQL = "DELETE FROM photo " +
						" WHERE ID = " + photo.getId().get();
				
				stmt.executeUpdate(deleteSQL);
				sqliteConnection.commit();
			}
			
			
		} catch (SQLException e) {
			throw new DaoException("Error in deleting photo", e);
		}
	}

}
