package ch.zhaw.photoflow.core.domain;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import ch.zhaw.photoflow.core.util.GuavaCollectors;

import com.google.common.collect.ImmutableList;

/**
 * Picture file formats.
 */
public enum FileFormat {
	JPEG("jpg", "jpeg"),
	PNG("png");

	private final String[] extensions;

	private FileFormat(String... extensions) {
		this.extensions = extensions;
	}

	public static Optional<FileFormat> get(String fileName) {
		Stream<FileFormat> s = Arrays.stream(values()).filter(fileFormat -> {
			return Arrays.stream(fileFormat.extensions)
				.map(e -> "." + e)
				.anyMatch(fileName.toLowerCase()::endsWith);
		});
		
		return s.findFirst();
	}
	
	public static ImmutableList<String> getAllFileExtensions () {
		return Arrays.stream(values())
			.flatMap(fileFormat -> Arrays.stream(fileFormat.extensions))
			.collect(GuavaCollectors.toImmutableList());
	}
	
}
