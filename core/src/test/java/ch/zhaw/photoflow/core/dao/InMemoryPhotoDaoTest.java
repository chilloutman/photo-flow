package ch.zhaw.photoflow.core.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.domain.Photo;
import ch.zhaw.photoflow.core.domain.PhotoTest;

public class InMemoryPhotoDaoTest {

	private PhotoDao dao;
	
	@Before
	public void before() throws DaoException {
		dao = new InMemoryPhotoDao();
		
		dao.save(Photo.newPhoto(p -> {
			p.setFilePath("swag.jpg");
		}));
		
		dao.save(Photo.newPhoto(p -> {
			p.setFilePath("yolo.jpg");
		}));
		
		dao.save(Photo.newPhoto(p -> {
			p.setFilePath("pimp.jpg");
		}));
	}
	
	@Test
	public void loadReturnsAllProjects () throws DaoException {
		assertThat(dao.loadAll(), hasSize(3));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void listIsImmutable () throws DaoException {
		List<Photo> photos = dao.loadAll();
		photos.clear();
	}
	
	/**
	 * Changing the path here does not change the actual photo instance without calling save().
	 */
	@Test
	public void photosAreNotLive () throws DaoException {
		dao.loadAll().get(0).setFilePath(PhotoTest.FILE_PATH);
		assertThat(dao.loadAll().get(0).getFilePath(), not(PhotoTest.FILE_PATH));
	}
	
	@Test
	public void savePhoto () throws DaoException {
		Photo photo = dao.loadAll().get(0);
		photo.setFilePath(PhotoTest.FILE_PATH);
		dao.save(photo);
		assertThat(dao.load(photo.getId().get()).get().getFilePath(), is(PhotoTest.FILE_PATH));
	}
	
	@Test
	public void deletePhoto () throws DaoException {
		Integer id = dao.loadAll().get(0).getId().get();
		dao.delete(Photo.newPhoto(p -> {
			p.setId(id);
		}));
		assertThat(dao.loadAll(), hasSize(2));
	}
	
}
