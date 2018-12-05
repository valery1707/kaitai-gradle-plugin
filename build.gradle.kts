plugins {
	groovy
	`java-library`
	`java-gradle-plugin`
}

repositories {
	jcenter()
}

configure<JavaPluginConvention> {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

version = "0.0.1"

dependencies {
	implementation(gradleApi())
	implementation("name.valery1707.kaitai:kaitai-maven-plugin:0.1.1")
	implementation("org.apache.maven:maven-plugin-api:3.5.0")

	testImplementation(gradleTestKit())
	testImplementation("junit:junit:4.12")
	testImplementation("org.assertj:assertj-core:2.9.0")
}
