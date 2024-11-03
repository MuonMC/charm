plugins {
	java
	`java-library`
	id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.9"
	id("org.muonmc.charm")
}

base.archivesName = "charm-test"
group = "org.muonmc"
version = "0.1.0"

idea {}

repositories {
	mavenCentral()
}

dependencies {
	implementation("net.minecraft:minecraft:1.21")
}

tasks.test {
	useJUnitPlatform()
}
