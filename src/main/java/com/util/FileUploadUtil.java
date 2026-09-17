package com.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Part;

/**
 * The original upload code stored files under the client-supplied file name
 * as-is. That's unsafe on two counts:
 *  - the "file name" a client sends is attacker-controlled and could contain
 *    path traversal (e.g. "../../../../evil.jsp") to write outside the
 *    intended image folder;
 *  - nothing stopped uploading a non-image file (e.g. a .jsp), which - if it
 *    landed under the webapp folder - could be executed by the server.
 *
 * This class validates the extension against a fixed allow-list and always
 * generates the on-disk file name itself, so the client's file name is only
 * ever used to recover the extension.
 */
public final class FileUploadUtil {

	// Project targets Java 8, so Set.of(...) (Java 9+) isn't available here.
	private static final Set<String> ALLOWED_EXTENSIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp")));

	private FileUploadUtil() {
	}

	/**
	 * Validates {@code part}'s file extension and returns a new, safe file name
	 * to store it under (a random name with the original, lower-cased
	 * extension). Returns null if no file was actually submitted (empty file
	 * input), so callers can fall back to keeping the existing image on updates.
	 *
	 * @throws IllegalArgumentException if the submitted file's extension isn't
	 *                                  one of the allowed image types.
	 */
	public static String buildSafeFileName(Part part) {
		String submittedFileName = part.getSubmittedFileName();
		if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
			return null;
		}

		String extension = getExtension(submittedFileName).toLowerCase();
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException(
					"Unsupported file type: ." + extension + ". Allowed types are: " + ALLOWED_EXTENSIONS);
		}

		return UUID.randomUUID() + "." + extension;
	}

	/** Writes {@code part} under {@code baseUploadPath + subfolder}, creating the folder if needed. */
	public static void writeToUploadDir(Part part, String baseUploadPath, String subfolder, String safeFileName)
			throws IOException {
		File dir = new File(baseUploadPath + subfolder);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		part.write(new File(dir, safeFileName).getAbsolutePath());
	}

	private static String getExtension(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return (dot >= 0 && dot < fileName.length() - 1) ? fileName.substring(dot + 1) : "";
	}
}
