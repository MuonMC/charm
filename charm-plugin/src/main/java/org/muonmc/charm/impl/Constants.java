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

package org.muonmc.charm.impl;

public final class Constants {
	public static final String NAME = "charm";
	public static final String CHARM_CACHE = ".gradle/charm-cache";

	public static final String MINECRAFT_MAVEN = "minecraftMaven";
	public static final String MINECRAFT_MAVEN_NAME = "CharmMinecraftMaven";
	public static final String MINECRAFT_GROUP = "net.minecraft";
	public static final String MINECRAFT_ARTIFACT = "minecraft";
	public static final String MINECRAFT_MODULE = MINECRAFT_GROUP + ":" + MINECRAFT_ARTIFACT;

	public static final String HASHED_MAVEN = "https://maven.quiltmc.org/repository/release";
	public static final String HASHED_MAVEN_NAME = "CharmHashedMaven";
	public static final String HASHED_GROUP = "org.quiltmc";
	public static final String HASHED_ARTIFACT = "hashed";

	public static final String MINECRAFT_CONFIGURATION = "minecraft";

	private Constants() {}
}
