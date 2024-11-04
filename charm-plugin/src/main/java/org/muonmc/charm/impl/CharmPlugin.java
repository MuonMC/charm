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

import org.gradle.api.*;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaProject;
import org.jetbrains.gradle.ext.*;
import org.jetbrains.annotations.NotNull;
import org.muonmc.charm.task.minecraft.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class CharmPlugin implements Plugin<Project> {
	private static final Logger LOGGER = LoggerFactory.getLogger("CharmPlugin");

	@Override
	public void apply(@NotNull Project target) {
		// Run downloadAssets after the project is synched, before all other tasks.
		DownloadManifestTask downloadManifest = registerTask(target, "downloadManifest", DownloadManifestTask.class);
		DownloadAssetsTask downloadAssets = registerTask(target, "downloadAssets", DownloadAssetsTask.class);
		RemapJarsTask remapJars = registerTask(target, "remapJars", RemapJarsTask.class);
		MergeJarsTask mergeJars = registerTask(target, "mergeJars", MergeJarsTask.class);
		SetupEnvironmentTask setupEnvironment = registerTask(target, "setupEnvironment", SetupEnvironmentTask.class);
		// IntelliJ IDEA
		try {
			IdeaProject ideaProject = target.getExtensions().getByType(IdeaModel.class).getProject();
			TaskTriggersConfig taskTriggers = ((ExtensionAware) ((ExtensionAware) ideaProject).getExtensions().getByType(ProjectSettings.class)).getExtensions().getByType(TaskTriggersConfig.class);
			taskTriggers.afterSync("setupEnvironment");
			downloadAssets.dependsOn("downloadManifest");
			remapJars.dependsOn("downloadAssets");
			mergeJars.dependsOn("remapJars");
			setupEnvironment.dependsOn("mergeJars");
			remapJars.mustRunAfter("downloadAssets");
			mergeJars.mustRunAfter("remapJars");
		} catch (UnknownDomainObjectException e) {
			LOGGER.warn("idea-ext plugin not found {}", e.getLocalizedMessage());
		}

		target.getConfigurations().create(Constants.MINECRAFT_CONFIGURATION)
			.extendsFrom(target.getConfigurations().getByName("implementation"));
		target.getConfigurations().create(Constants.MAPPINGS_CONFIGURATION)
			.extendsFrom(target.getConfigurations().getByName("implementation"));

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
	}

	private <T extends Task> T registerTask(Project target, String name, Class<T> task) {
		return target.getTasks().create(name, task, ConfigureCharmTask.configure());
	}
}
