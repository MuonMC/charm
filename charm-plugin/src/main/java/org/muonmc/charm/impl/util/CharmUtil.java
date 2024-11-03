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

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.muonmc.charm.impl.Constants;

import java.util.Objects;

public final class CharmUtil {
	private CharmUtil() {}

	public static String getMavenPath(String targetVersion) {
		return Constants.MINECRAFT_MAVEN + "/" + Constants.MINECRAFT_GROUP.replace(".", "/") + "/" + Constants.MINECRAFT_ARTIFACT + "/" + targetVersion;
	}

	public static boolean isMinecraft(Dependency dependency) {
		return Objects.equals(dependency.getGroup(), Constants.MINECRAFT_GROUP) && dependency.getName().equals(Constants.MINECRAFT_ARTIFACT);
	}

	public static boolean isConfigApi(Configuration configuration) {
		return configuration.getName().equals("api");
	}
}
