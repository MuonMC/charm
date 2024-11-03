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

package org.muonmc.charm.task.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.muonmc.charm.impl.util.CharmUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.muonmc.charm.task.minecraft.DownloadAssetsTask.*;

public abstract class DownloadManifestTask extends DefaultTask {
	@TaskAction
	public void runTask() {
		Project project = getProject();
		Gson gson = new Gson();
		try {
			String manifestContent = downloadManifest();
			JsonObject manifest = gson.fromJson(manifestContent, JsonObject.class);
			JsonArray versions = manifest.get("versions").getAsJsonArray();

			AtomicReference<String> targetVersion = new AtomicReference<>();
			project.getConfigurations().all(configuration -> {
				configuration.getDependencies().stream()
					.filter(CharmUtil::isMinecraft)
					.limit(1)
					.findAny()
					.ifPresent(dependency -> {
						if (CharmUtil.isConfigApi(configuration)) {
							throw new RuntimeException("Minecraft cannot be included via an api dependency! " + DECLARE_MINECRAFT_MESSAGE);
						}
						targetVersion.set(dependency.getVersion());
					});
			});
			if (targetVersion.get() == null) {
				throw new RuntimeException("Minecraft version not specified! " + DECLARE_MINECRAFT_MESSAGE);
			}

			AtomicReference<String> versionManifestUrl = new AtomicReference<>();
			versions.forEach(element -> {
				JsonObject version = element.getAsJsonObject();
				String id = version.get("id").getAsString();
				if (id.equals(targetVersion.get())) {
					versionManifestUrl.set(version.get("url").getAsString());
				}
			});

			String versionManifestContent = downloadTextFile(versionManifestUrl.get());
			DownloadAssetsTask downloadAssets = project.getTasks()
				.withType(DownloadAssetsTask.class)
				.getByName("downloadAssets");
			downloadAssets.getTargetVersion().set(targetVersion.get());
			downloadAssets.getVersionManifestV2().set(versionManifestContent);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private String downloadManifest() throws MalformedURLException {
		return downloadTextFile(PISTON_META_URL + "/" + VERSION_MANIFEST_V2);
	}

	private String downloadTextFile(String path) throws MalformedURLException {
		URL url = URI.create(path).toURL();
		try (InputStream is = url.openStream()) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
