package ch.zhaw.photoflow.core.dummy;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.domain.Photo;

public class DummyPhotoDao implements PhotoDao {

	private final List<Photo> photos = new ArrayList<>();
	
	public DummyPhotoDao() {
		// TODO: Generate better dummy data.
		photos.add(new Photo());
		photos.add(new Photo());
		photos.add(new Photo());
	}
	
	@Override
	public List<Photo> load() {
		return new ArrayList<>(photos);
	}

	@Override
	public Photo save(Photo photo) {
		// TODO:
		// 1. Check photo id:
		//    - If there is already a project with that id, replace it.
		//    - Otherwise add a new project and generate an id.
		photos.add(photo);
		return photo;
	}

	@Override
	public void delete(Photo photo) {
		photos.remove(photo);
	}

}
