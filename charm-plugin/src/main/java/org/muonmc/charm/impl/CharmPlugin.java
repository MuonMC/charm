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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;
import org.muonmc.charm.task.minecraft.DownloadAssetsTask;

import java.net.URI;
import java.net.URISyntaxException;

public class CharmPlugin implements Plugin<Project> {
	@Override
	public void apply(@NotNull Project target) {
		// Set up the custom Minecraft repository.
		target.getRepositories().maven(repository -> {
			repository.setName(Constants.MINECRAFT_MAVEN_NAME);
			repository.setUrl(target.file(Constants.CHARM_CACHE + "/" + Constants.MINECRAFT_MAVEN).toURI());
			repository.content(content -> content.includeModule(Constants.MINECRAFT_GROUP, Constants.MINECRAFT_ARTIFACT));
		});

		// Set up the Quilt Maven for hashed Mojang mappings.
		target.getRepositories().maven(repository -> {
			repository.setName(Constants.HASHED_MAVEN_NAME);
			try {
				repository.setUrl(new URI(Constants.HASHED_MAVEN));
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			repository.content(content -> content.includeModule(Constants.HASHED_GROUP, Constants.HASHED_ARTIFACT));
		});

		// Run downloadAssets after the project is evaluated.
		// This must be run after the project is evaluated since that is when a list of dependencies is available.
		DownloadAssetsTask downloadAssets = target.getTasks().create("downloadAssets", DownloadAssetsTask.class, ConfigureCharmTask.configure());
		target.afterEvaluate(project -> downloadAssets.runTask());
	}
}
