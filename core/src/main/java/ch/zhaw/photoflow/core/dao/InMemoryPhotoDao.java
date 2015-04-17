package ch.zhaw.photoflow.core.dao;

import static ch.zhaw.photoflow.core.util.GuavaCollectors.toImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.zhaw.photoflow.core.DaoException;
import ch.zhaw.photoflow.core.PhotoDao;
import ch.zhaw.photoflow.core.domain.Photo;

public class InMemoryPhotoDao implements PhotoDao {

	private static int idCounter = 0;
	
	private static int nextId () {
		return idCounter++;
	}
	
	private final List<Photo> photos = new ArrayList<>();
	
	public InMemoryPhotoDao() {
	}
	
	@Override
	public List<Photo> loadAll() {
		return photos.stream().map(Photo::copy).collect(toImmutableList());
	}
	
	@Override
	public Optional<Photo> load(int id) throws DaoException {
		// Search for a photo with the given id.
		List<Photo> found = photos.stream().filter(p -> p.getId().get().equals(id)).collect(Collectors.toList());
		if (found.size() > 1) {
			throw new DaoException("Photo ID " + id + "is not unique! This is bad and should not have happened!");
		}
		return found.stream().findFirst().map(Photo::copy);
	}

	@Override
	public Photo save(Photo photo) {
		if (photo.getId().isPresent()) {
			// Remove old project
			photos.removeIf(p -> p.getId().equals(photo.getId()));
		} else {
			// Generate new ID.
			photo.setId(nextId());
		}
		// Add new project
		photos.add(Photo.copy(photo));
		return photo;
	}

	@Override
	public void delete(Photo photo) {
		photos.remove(photo);
	}

}
