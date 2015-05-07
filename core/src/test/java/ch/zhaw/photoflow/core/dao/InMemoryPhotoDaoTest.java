package ch.zhaw.photoflow.core.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoTest;

public class InMemoryPhotoDaoTest {
	
	private static final Integer PROJECT_1 = 1;
	private static final Integer PROJECT_2 = 2;
	
	private PhotoDao dao;
	
	@Before
	public void before() throws DaoException, SQLException {
		dao = new InMemoryPhotoDao();
		
		dao.save(Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("swag.jpg");
		}));
		
		dao.save(Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_1);
			p.setFilePath("yolo.jpg");
		}));
		
		dao.save(Photo.newPhoto(p -> {
			p.setProjectId(PROJECT_2);
			p.setFilePath("pimp.jpg");
		}));
	}
	
	@Test
	public void loadReturnsEmptyList () throws DaoException {
		assertThat(dao.loadAll(1337), hasSize(0));
	}
	
	@Test
	public void loadReturnsAllPhotosForProject () throws DaoException {
		assertThat(dao.loadAll(PROJECT_1), hasSize(2));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void listIsImmutable () throws DaoException {
		List<Photo> photos = dao.loadAll(PROJECT_1);
		photos.clear();
	}
	
	/**
	 * Changing the path here does not change the actual photo instance without calling save().
	 */
	@Test
	public void photosAreNotLive () throws DaoException {
		dao.loadAll(PROJECT_1).get(0).setFilePath(PhotoTest.FILE_PATH);
		assertThat(dao.loadAll(PROJECT_1).get(0).getFilePath(), not(PhotoTest.FILE_PATH));
	}
	
	@Test
	public void savePhoto () throws DaoException, SQLException {
		Photo photo = dao.loadAll(PROJECT_1).get(0);
		photo.setFilePath(PhotoTest.FILE_PATH);
		dao.save(photo);
		assertThat(dao.load(photo.getId().get()).get().getFilePath(), is(PhotoTest.FILE_PATH));
	}
	
	@Test
	public void deletePhoto () throws DaoException, SQLException {
		int size = dao.loadAll(PROJECT_1).size();
		int id = dao.loadAll(PROJECT_1).get(0).getId().get();
		dao.delete(Photo.newPhoto(p -> {
			p.setId(id);
		}));
		assertThat(dao.loadAll(PROJECT_1), hasSize(size-1));
	}
	
}
