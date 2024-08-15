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
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.TaskAction;
import org.muonmc.charm.impl.Constants;
import org.muonmc.charm.impl.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DownloadAssetsTask extends DefaultTask {
	private static final String PISTON_META_URL = "https://piston-meta.mojang.com";
	private static final String MANIFEST_NAME = "version_manifest_v2.json";
	private static final String VERSION_MANIFEST_V2 = "mc/game/" + MANIFEST_NAME;
	private static final String DECLARE_MINECRAFT_MESSAGE = "Declare an implementation dependency on " + Constants.MINECRAFT_MODULE + ":<version>";

	@TaskAction
	public void runTask() {
		Project project = getProject();
		Gson gson = new Gson();
		try {
			File charmCache = FileUtil.createDir(project.file(Constants.CHARM_CACHE));
			String manifestContent = downloadManifest();
			JsonObject manifest = gson.fromJson(manifestContent, JsonObject.class);
			JsonArray versions = manifest.get("versions").getAsJsonArray();

			AtomicReference<String> targetVersion = new AtomicReference<>();
			project.getConfigurations().all(configuration -> {
				configuration.getDependencies().stream()
					.filter(DownloadAssetsTask::isMinecraft)
					.limit(1)
					.findAny()
					.ifPresent(dependency -> {
						if (isConfigApi(configuration)) {
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
			JsonObject versionManifest = gson.fromJson(versionManifestContent, JsonObject.class);
			JsonObject downloads = versionManifest.get("downloads").getAsJsonObject();
			JsonObject client = downloads.get("client").getAsJsonObject();
			String clientUrlPath = client.get("url").getAsString();

			String mavenPath = charmCache.toPath() + "/" + getMavenPath(targetVersion.get());

			// Write the Minecraft JAR.
			URL clientUrl = URI.create(clientUrlPath).toURL();
			try (InputStream is = clientUrl.openStream()) {
				FileUtil.createDir(project.file(mavenPath));
				File clientJar = FileUtil.create(project.file(mavenPath + "/" + getJarName(targetVersion.get())));
				Files.write(clientJar.toPath(), is.readAllBytes(), StandardOpenOption.WRITE);
			}

			// Write the Maven POM.
			String pomTemplate;
			try (InputStream is2 = getClass().getClassLoader().getResourceAsStream("pom-template.xml")) {
				pomTemplate = new String(Objects.requireNonNull(is2).readAllBytes(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			pomTemplate = pomTemplate
				.replace("%GROUP%", Constants.MINECRAFT_GROUP)
				.replace("%ARTIFACT%", Constants.MINECRAFT_ARTIFACT)
				.replace("%VERSION%", targetVersion.get());
			File pomTemplateFile = FileUtil.create(project.file(mavenPath + "/" + getPomName(targetVersion.get())));
			Files.writeString(pomTemplateFile.toPath(), pomTemplate, StandardOpenOption.WRITE);
		} catch (IOException e) {
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

	private static boolean isMinecraft(Dependency dependency) {
		return Objects.equals(dependency.getGroup(), Constants.MINECRAFT_GROUP) && dependency.getName().equals(Constants.MINECRAFT_ARTIFACT);
	}

	private static boolean isConfigApi(Configuration configuration) {
		return configuration.getName().equals("api");
	}

	private static String getMavenPath(String targetVersion) {
		return Constants.MINECRAFT_MAVEN + "/" + Constants.MINECRAFT_GROUP.replace(".", "/") + "/" + Constants.MINECRAFT_ARTIFACT + "/" + targetVersion;
	}

	private static String getJarName(String targetVersion) {
		return Constants.MINECRAFT_ARTIFACT + "-" + targetVersion + ".jar";
	}

	private static String getPomName(String targetVersion) {
		return Constants.MINECRAFT_ARTIFACT + "-" + targetVersion + ".pom";
	}
}
