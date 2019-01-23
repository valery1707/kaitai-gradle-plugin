import java.util.concurrent.Callable

plugins {
	groovy
	`java-library`
	`java-gradle-plugin`
	maven
	signing
	id("com.gradle.plugin-publish") version "0.10.0"
}

repositories {
	jcenter()
}

configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

group = "name.valery1707.kaitai"
version = "0.1.1"
extra["isReleaseVersion"] = !version.toString().endsWith("SNAPSHOT")

dependencies {
	implementation(gradleApi())
	implementation("name.valery1707.kaitai:kaitai-maven-plugin:0.1.3") {
		exclude(group = "com.jcabi")
	}

	testImplementation(gradleTestKit())
	testImplementation("junit:junit:4.12")
	testImplementation("org.assertj:assertj-core:2.9.0")
}

tasks {
	val javadocJar by register("javadocJar", Jar::class) {
		classifier = "javadoc"
		from(groovydoc)
	}

	val sourcesJar by register("sourcesJar", Jar::class) {
		classifier = "sources"
		from(sourceSets["main"].allSource)
	}
}

artifacts {
	add("archives", tasks["javadocJar"])
	add("archives", tasks["sourcesJar"])
}

signing {
	setRequired(Callable {
		(project.extra["isReleaseVersion"] as Boolean) && gradle.taskGraph.hasTask("uploadArchives")
	})
	sign(configurations.archives.get())
}

//Deploy: clean build uploadArchives
//https://central.sonatype.org/pages/gradle.html
//https://docs.gradle.org/current/userguide/signing_plugin.html
tasks.getByName<Upload>("uploadArchives") {
	repositories {
		withConvention(MavenRepositoryHandlerConvention::class) {
			mavenDeployer {
				beforeDeployment { signing.signPom(this) }
			}
		}
	}
	repositories.withGroovyBuilder {
		"mavenDeployer" {
			"repository"("url" to "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
				"authentication"("userName" to project.property("ossrh.username"), "password" to project.property("ossrh.password"))
			}
			"snapshotRepository"("url" to "https://oss.sonatype.org/content/repositories/snapshots/") {
				"authentication"("userName" to project.property("ossrh.username"), "password" to project.property("ossrh.password"))
			}
			"pom" {
				"project" {
					setProperty("name", "Kaitai Gradle Plugin")
					setProperty("packaging", "jar")
					setProperty("description", "Gradle plugin for [Kaitai](http://kaitai.io/): declarative language to generate binary data parsers")
					setProperty("url", "https://github.com/valery1707/kaitai-gradle-plugin")
					setProperty("inceptionYear", "2018")
					"scm" {
						setProperty("url", "https://github.com/valery1707/kaitai-gradle-plugin")
						setProperty("connection", "scm:git:https://github.com/valery1707/kaitai-gradle-plugin")
						setProperty("developerConnection", "scm:git|git@github.com:valery1707/kaitai-gradle-plugin.git")
					}
					"licenses" {
						"license" {
							setProperty("name", "MIT License")
							setProperty("url", "http://opensource.org/licenses/MIT")
							setProperty("distribution", "repo")
						}
					}
					"developers" {
						"developer" {
							setProperty("id", "valery1707")
							setProperty("name", "Valeriy Vyrva")
							setProperty("email", "valery1707@gmail.com")
							setProperty("timezone", "+3")
							//todo roles
						}
					}
				}
			}
		}
	}
}

// Use java-gradle-plugin to generate plugin descriptors and specify plugin ids
gradlePlugin {
	plugins {
		create("kaitaiPlugin") {
			id = "name.valery1707.kaitai"
			implementationClass = "name.valery1707.kaitai.KaitaiPlugin"
		}
	}
}
pluginBundle {
	website = "https://kaitai.io/"
	vcsUrl = "https://github.com/valery1707/kaitai-gradle-plugin"
	(plugins) {
		"kaitaiPlugin" {
			// id is captured from java-gradle-plugin configuration
			displayName = "Kaitai Gradle plugin"
			description = "Automatic compile kaitai format specifications into Java-files"
			tags = listOf("kaitai", "kaitai-struct", "java", "scala", "kotlin")
		}
	}
}
