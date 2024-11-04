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

import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.*;
import org.muonmc.charm.impl.util.RemapUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@CacheableTask
public abstract class RemapJarsTask extends DefaultTask {
	@Input
	public abstract ListProperty<String> getEnvironments();

	@Input
	public abstract String getFromNamespace();

	@Input
	public abstract String getToNamespace();

	@InputFile
	public abstract RegularFileProperty getClientJar();

	@InputFile
	public abstract RegularFileProperty getServerJar();

	@InputFile
	public abstract Optional<RegularFileProperty> getGenericMappings();

	@InputFile
	public abstract Optional<RegularFileProperty> getClientMappings();

	@InputFile
	public abstract Optional<RegularFileProperty> getServerMappings();

	@OutputFiles
	public abstract FileCollection getMappedJars();

	@TaskAction
	public void runTask() throws IOException {
		// Check if we are using generic mappings or sided mappings.
		boolean genericMappings = getGenericMappings().isPresent();

		// Remap jars

		if (!genericMappings && getClientMappings().isPresent()) {
			remapJar(getClientMappings().get(), getClientJar());
		}

		if (!genericMappings && getServerMappings().isPresent()) {
			remapJar(getClientMappings().get(), getServerJar());
		}

		if (genericMappings) {
			remapJar(getGenericMappings().get(), getClientJar());
			remapJar(getGenericMappings().get(), getServerJar());
		}
	}

	private void remapJar(RegularFileProperty mappings, RegularFileProperty jar) throws IOException {
		getMappedJars().getFiles().add(getJarPath(jar).toFile());
		try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(getJarPath(jar)).build()) {
			TinyRemapper remapper = RemapUtil.createRemapper(
				mappings.getAsFile().get(),
				getFromNamespace(),
				getToNamespace(),
				true
			);
			remapper.readInputs(jar.getAsFile().get().toPath());
			remapper.apply(outputConsumer);
		}
	}

	private Path getJarPath(RegularFileProperty jar) {
		String jarName = getJarName(jar);
		return jar.get().getAsFile().toPath().getParent().resolve(jarName);
	}

	private String getJarName(RegularFileProperty jar) {
		return jar.getAsFile().get()
			.getName()
			.replace("\\.jar", "")
			.replace("-" + getFromNamespace(), "")
			+ "-" + getToNamespace() + ".jar";
	}
}
