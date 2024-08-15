/*
 * Charm - Muon Loader's Gradle build system
 * Copyright (C) 2024  MuonMC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.muonmc.charm.impl.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class FileUtil {
	private FileUtil() {}

	public static File create(File file) throws IOException {
		if (!file.exists()) {
			Files.createFile(file.toPath());
		}
		return file;
	}

	public static File createDir(File file) throws IOException {
		if (!file.exists() || !file.isDirectory()) {
			Files.createDirectories(file.toPath());
		}
		return file;
	}
}
