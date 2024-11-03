plugins {
    id("java")
    id("dev.yumi.gradle.licenser") version "1.2.+"
	`maven-publish`
	`java-gradle-plugin`
}

val pluginVersion: String by project
val mavenGroup: String by project
val pluginId: String by project

base.archivesName = pluginId
group = mavenGroup
version = pluginVersion

repositories {
    mavenCentral()

	maven {
		url = uri("https://plugins.gradle.org/m2/")
	}

    maven {
        name = "MuonMC Releases"
        url = uri("https://maven.muonmc.org/releases")
		content {
			includeGroup("org.muonmc")
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

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

	api(libs.google.gson)
	api(libs.fabric.tiny.remapper)
	api(libs.fabric.mapping.io)
	api(libs.fabric.stitch)
	api("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.1.9")

	runtimeOnly("ch.qos.logback:logback-classic:1.5.5")
	runtimeOnly("org.apache.logging.log4j:log4j-to-slf4j:2.24.1")
}

gradlePlugin {
	plugins {
		create("charm") {
			id = "$mavenGroup.$pluginId"
			implementationClass = "org.muonmc.charm.impl.CharmPlugin"
		}
	}
}

license {
    rule(file("codeformat/LHEADER"))

    include("**/*.java")
    exclude("**/*.properties")
}

tasks.test {
    useJUnitPlatform()
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = mavenGroup
			artifactId = pluginId
			version = pluginVersion

			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
		maven {
			url = uri(System.getenv("MAVEN_URL").orEmpty())
			credentials {
				username = System.getenv("MAVEN_USERNAME")
				password = System.getenv("MAVEN_PASSWORD")
			}
		}

		maven {
			url = uri(System.getenv("SNAPSHOTS_URL").orEmpty())
			credentials {
				username = System.getenv("SNAPSHOTS_USERNAME")
				password = System.getenv("SNAPSHOTS_PASSWORD")
			}
		}
	}
}
