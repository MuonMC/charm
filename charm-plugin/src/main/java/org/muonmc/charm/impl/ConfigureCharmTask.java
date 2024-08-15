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

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

public class ConfigureCharmTask<T extends Task> implements Action<T> {
	private ConfigureCharmTask() {}

	public static <U extends Task> ConfigureCharmTask<U> configure() {
		return new ConfigureCharmTask<>();
	}

	@Override
	public void execute(@NotNull T task) {
		task.setGroup(Constants.NAME);
	}
}
