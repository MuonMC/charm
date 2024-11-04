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

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class RemapUtil {
	private RemapUtil() {}

	public static TinyRemapper createRemapper(File mappings, String fromNs, String toNs, boolean remapLVT) throws IOException {
		try (FileReader fileReader = new FileReader(mappings)) {
			@Nullable MappingFormat mappingFormat = MappingReader.detectFormat(fileReader);
			if (mappingFormat == null) throw new UnsupportedOperationException("Unknown mappings format.");
			MemoryMappingTree mappingTree = new MemoryMappingTree();
			MappingReader.read(fileReader, mappingFormat, mappingTree);

			return TinyRemapper.newRemapper()
				.withMappings(createMappingProvider(mappingTree, fromNs, toNs, remapLVT))
				.renameInvalidLocals(true)
				.rebuildSourceFilenames(true)
				.build();
		}
	}

	public static IMappingProvider createMappingProvider(MappingTree mappingTree, String fromNs, String toNs, boolean remapLVT) {
		return (acceptor) -> {
			for (MappingTree.ClassMapping classMapping : mappingTree.getClasses()) {
				String fromClassName = classMapping.getName(fromNs);
				String toClassName = classMapping.getName(toNs);

				if (fromClassName == null) {
					continue;
				}

				if (toClassName == null) {
					toClassName = fromClassName;
				}

				acceptor.acceptClass(fromClassName, toClassName);

				for (MappingTree.FieldMapping fieldMapping : classMapping.getFields()) {
					MemberResult result = remapMember(fromNs, toNs, fieldMapping);
					if (result == null) {
						continue;
					}

					acceptor.acceptField(
						memberOf(
							fromClassName,
							result.fromMemberName(),
							result.fromMemberDesc()
						),
						result.toMemberName()
					);
				}

				for (MappingTree.MethodMapping methodMapping : classMapping.getMethods()) {
					MemberResult result = remapMember(fromNs, toNs, methodMapping);
					if (result == null) {
						continue;
					}

					IMappingProvider.Member methodMember = memberOf(
						fromClassName,
						result.fromMemberName(),
						result.fromMemberDesc()
					);

					acceptor.acceptMethod(
						methodMember,
						result.toMemberName()
					);

					if (remapLVT) {
						for (MappingTree.MethodArgMapping methodArgMapping : methodMapping.getArgs()) {
							String toArgName = methodArgMapping.getName(toNs);

							if (toArgName == null) {
								continue;
							}

							acceptor.acceptMethodArg(
								methodMember,
								methodArgMapping.getLvIndex(),
								toArgName
							);
						}

						for (MappingTree.MethodVarMapping methodVarMapping : methodMapping.getVars()) {
							String toVarName = methodVarMapping.getName(toNs);

							if (toVarName == null) {
								continue;
							}

							acceptor.acceptMethodVar(
								methodMember,
								methodVarMapping.getLvIndex(),
								methodVarMapping.getStartOpIdx(),
								methodVarMapping.getLvtRowIndex(),
								toVarName
							);
						}
					}
				}
			}
		};
	}

	private static @Nullable RemapUtil.MemberResult remapMember(String fromNs, String toNs, MappingTree.MemberMapping memberMapping) {
		String fromMemberName = memberMapping.getName(fromNs);
		String fromMemberDesc = memberMapping.getDesc(fromNs);
		String toMemberName = memberMapping.getName(toNs);

		if (fromMemberName == null) {
			return null;
		}

		if (toMemberName == null) {
			toMemberName = fromMemberName;
		}

		return new MemberResult(fromMemberName, fromMemberDesc, toMemberName);
	}

	private record MemberResult(String fromMemberName, String fromMemberDesc, String toMemberName) {
	}

	public static IMappingProvider.Member memberOf(String className, String memberName, String descriptor) {
		return new IMappingProvider.Member(className, memberName, descriptor);
	}
}
