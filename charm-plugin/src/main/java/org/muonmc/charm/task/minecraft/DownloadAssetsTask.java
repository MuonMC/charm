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
import com.google.gson.JsonObject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.muonmc.charm.impl.Constants;
import org.muonmc.charm.impl.util.CharmUtil;
import org.muonmc.charm.impl.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@CacheableTask
public abstract class DownloadAssetsTask extends DefaultTask {
	public static final String PISTON_META_URL = "https://piston-meta.mojang.com";
	public static final String MANIFEST_NAME = "version_manifest_v2.json";
	public static final String VERSION_MANIFEST_V2 = "mc/game/" + MANIFEST_NAME;
	public static final String DECLARE_MINECRAFT_MESSAGE = "Declare an implementation dependency on " + Constants.MINECRAFT_MODULE + ":<version>";

	@Input
	public abstract Property<String> getTargetVersion();

	@Input
	public abstract Property<String> getVersionManifestV2();

	@OutputDirectory
	public abstract DirectoryProperty getOutput();

	public DownloadAssetsTask() {
		getOutput().set(getProject().file(Constants.CHARM_CACHE));
	}

	@TaskAction
	public void runTask() {
		Project project = getProject();
		Gson gson = new Gson();
		try {
			File charmCache = project.file(Constants.CHARM_CACHE);
			String targetVersion = getTargetVersion().get();
			JsonObject versionManifest = gson.fromJson(getVersionManifestV2().get(), JsonObject.class);
			JsonObject downloads = versionManifest.get("downloads").getAsJsonObject();
			JsonObject client = downloads.get("client").getAsJsonObject();
			String clientUrlPath = client.get("url").getAsString();

			String mavenPath = charmCache.toPath() + "/" + CharmUtil.getMavenPath(targetVersion);

			// Write the client Minecraft JAR.
			URL clientUrl = URI.create(clientUrlPath).toURL();
			try (InputStream is = clientUrl.openStream()) {
				FileUtil.createDir(project.file(mavenPath));
				File clientJar = FileUtil.create(project.file(mavenPath + "/" + getClientJarName(targetVersion)));
				Files.write(clientJar.toPath(), is.readAllBytes(), StandardOpenOption.WRITE);
			}

			JsonObject server = downloads.get("server").getAsJsonObject();
			String serverUrlPath = server.get("url").getAsString();

			// Write the server Minecraft JAR.
			URL serverUrl = URI.create(serverUrlPath).toURL();
			try (InputStream is = serverUrl.openStream()) {
				FileUtil.createDir(project.file(mavenPath));
				File serverJar = FileUtil.create(project.file(mavenPath + "/" + getServerJarName(targetVersion)));
				Files.write(serverJar.toPath(), is.readAllBytes(), StandardOpenOption.WRITE);
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
				.replace("%VERSION%", targetVersion);
			File pomTemplateFile = FileUtil.create(project.file(mavenPath + "/" + getPomName(targetVersion)));
			Files.writeString(pomTemplateFile.toPath(), pomTemplate, StandardOpenOption.WRITE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getJarName(String targetVersion, String environment) {
		return Constants.MINECRAFT_ARTIFACT + "-" + targetVersion + "-" + environment + ".jar";
	}

	private static String getClientJarName(String targetVersion) {
		return getJarName(targetVersion, Constants.CLIENT_ENVIRONMENT);
	}

	private static String getServerJarName(String targetVersion) {
		return getJarName(targetVersion, Constants.SERVER_ENVIRONMENT);
	}

	public static String getFinalJarName(String targetVersion) {
		return getJarName(targetVersion, "");
	}

	private static String getPomName(String targetVersion) {
		return Constants.MINECRAFT_ARTIFACT + "-" + targetVersion + ".pom";
	}
}
