rootProject.name = "charm-test"

pluginManagement {
	includeBuild("../charm-plugin")

	repositories {
		gradlePluginPortal()
		mavenCentral()

		maven {
			name = "MuonMC"
			url = uri("https://maven.muonmc.org/releases")
			content {
				includeModule("net.fabricmc", "tiny-remapper")
				includeModule("net.fabricmc", "stitch")
			}
		}

		maven {
			name = "MuonMC Snapshots"
			url = uri("https://maven.muonmc.org/snapshots")
			content {
				includeModule("net.fabricmc", "tiny-remapper")
			}
		}

		maven {
			name = "FabricMC"
			url = uri("https://maven.fabricmc.net/")
			content {
				includeModule("net.fabricmc", "tiny-mappings-parser")
			}
		}
	}
}
