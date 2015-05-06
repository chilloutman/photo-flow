package ch.zhaw.photoflow.core.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

public class FileFormatTest {

	@Test
	public void jpg() {
		assertIsFileFormat(FileFormat.JPEG, "photo.jpg", "photo.jpeg", "photo.JPG");
	}
	
	@Test
	public void png() {
		assertIsFileFormat(FileFormat.PNG, "photo.png", "photo.PNG", "photo.PnG");
	}
	
	@Test
	public void invalidFileExtensions() {
		assertIsInvalidFileFormat("photo.txt", "photo.tiff", "photo.raw");
	}
	
	@Test
	public void allFileExtensions () {
		assertThat(FileFormat.getAllFileExtensions(), containsInAnyOrder("jpg", "jpeg", "png"));
	}
	
	private void assertIsFileFormat(FileFormat fileFormat, String... fileNames) {
		Arrays.stream(fileNames)
			.forEach(name -> assertThat(FileFormat.get(name).get(), is(fileFormat)));
	}
	
	private void assertIsInvalidFileFormat(String... fileNames) {
		Arrays.stream(fileNames)
			.forEach(name -> assertThat(FileFormat.get(name), is(Optional.empty())));
	}
	
}
