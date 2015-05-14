package ch.zhaw.photoflow.core.domain;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import ch.zhaw.photoflow.core.util.GuavaCollectors;

import com.google.common.collect.ImmutableList;

/**
 * Photo file formats.
 */
public enum FileFormat {
	/** For Photos in the JPEG format. */
	JPEG("jpg", "jpeg"),
	/** For Photos in the PNG format.*/
	PNG("png");

	private final String[] extensions;

	private FileFormat(String... extensions) {
		this.extensions = extensions;
	}

	/**
	 * Gets a {@link FileFormat} by the file name.
	 * @param fileName The name of the file to use to determine the format.
	 * @return {@link Optional}<{@link FileFormat}>
	 */
	public static Optional<FileFormat> get(String fileName) {
		Stream<FileFormat> s = Arrays.stream(values()).filter(fileFormat -> {
			return Arrays.stream(fileFormat.extensions)
				.map(e -> "." + e)
				.anyMatch(fileName.toLowerCase()::endsWith);
		});
		
		return s.findFirst();
	}
	
	/**
	 * Gets all declared file formats.
	 * @return {@link ImmutableList} containing all declared file formats.
	 */
	public static ImmutableList<String> getAllFileExtensions () {
		return Arrays.stream(values())
			.flatMap(fileFormat -> Arrays.stream(fileFormat.extensions))
			.collect(GuavaCollectors.toImmutableList());
	}
	
}
