plugins {
	groovy
	`java-library`
	`java-gradle-plugin`
//	`maven-publish`
	maven
}

repositories {
	jcenter()
}

configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

version = "0.1.0"

dependencies {
	implementation(gradleApi())
	implementation("name.valery1707.kaitai:kaitai-maven-plugin:0.1.2") {
		exclude(group = "com.jcabi")
	}

	testImplementation(gradleTestKit())
	testImplementation("junit:junit:4.12")
	testImplementation("org.assertj:assertj-core:2.9.0")
}
