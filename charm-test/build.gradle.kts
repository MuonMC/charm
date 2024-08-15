plugins {
	java
	`java-library`
	id("org.muonmc.charm")
}

base.archivesName = "charm-test"
group = "org.muonmc"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
	implementation("net.minecraft:minecraft:1.21")
}

tasks.test {
    useJUnitPlatform()
}
