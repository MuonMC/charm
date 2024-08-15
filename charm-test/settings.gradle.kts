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
			}
		}
	}
}
