//* For integration test
${build-header.template}
// */

/* For direct build
buildscript {
	repositories {
		jcenter()
		//Used for access to snapshot version of dependencies
		mavenLocal()
	}
	dependencies {
		classpath("name.valery1707.kaitai:kaitai-gradle-plugin:0.1.0")
	}
}
// */

//Use case: source directory with KaiTai templates exists and compiled
//Test code is written in Kotlin

plugins {
	java
	id("nebula.kotlin") version "1.3.11"
	id("name.valery1707.kaitai") version "0.1.0"
}

repositories {
	jcenter()
}

val libJunitVersion = "5.3.2"

dependencies {
	implementation("io.kaitai:kaitai-struct-runtime:0.8")

	testImplementation("org.junit.jupiter:junit-jupiter-api:$libJunitVersion")
	testImplementation("org.junit.jupiter:junit-jupiter-params:$libJunitVersion")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:$libJunitVersion")
	testImplementation("org.assertj:assertj-core:3.11.1")
}

configure<name.valery1707.kaitai.KaitaiExtension> {
	packageName = "name.valery1707.kaitai.it"
}

tasks.withType<Test> {
	useJUnitPlatform()
}
