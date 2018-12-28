package name.valery1707.kaitai

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

import static org.apache.commons.lang3.StringUtils.isBlank

class GenerateConfig {
	/**
	 * Skip plugin execution (don't read/validate any files, don't generate any java types).
	 *
	 * @since 0.1.0
	 */
	boolean skip = false

	/**
	 * Direct link onto <a href="http://kaitai.io/#download">KaiTai universal zip archive</a>.
	 *
	 * @since 0.1.0
	 */
	URL url

	/**
	 * Version of <a href="http://kaitai.io/#download">KaiTai</a> library.
	 *
	 * @since 0.1.0
	 */
	String version = "0.8"

	/**
	 * Cache directory for download KaiTai library.
	 *
	 * @see KaitaiMojo#version
	 * @see KaitaiMojo#url
	 * @since 0.1.0
	 */
	File cacheDir

	/**
	 * Source directory with <a href="http://formats.kaitai.io/">Kaitai Struct language</a> files.
	 * <p/>
	 * Default: <code>src/main/resources/kaitai</code>
	 *
	 * @since 0.1.0
	 */
	File sourceDirectory

	/**
	 * Include wildcard pattern list.
	 *
	 * @since 0.1.0
	 */
	String[] includes = ["*.ksy"]

	/**
	 * Exclude wildcard pattern list.
	 *
	 * @since 0.1.0
	 */
	String[] excludes = []

	/**
	 * Target directory for generated Java source files.
	 *
	 * @since 0.1.0
	 */
	File output

	/**
	 * Target package for generated Java source files.
	 *
	 * @since 0.1.0
	 */
	String packageName

	/**
	 * Which goals need to depends on kaitai generation result
	 *
	 * @since 0.1.0
	 */
	String[] runBefore = ["compileJava", "compileKotlin", "compileScala"]

	GenerateConfig(Project project) {
		initDefaults(project)
	}

	private void initDefaults(Project project) {
		url = KaitaiUtils.prepareUrl(url, version)

		if (!sourceDirectory) {
			def sourceRoot = project
				.projectDir.toPath()
				.resolve("src").resolve("main").resolve("resources").resolve("kaitai")
				.toFile()
			if (project.hasProperty("sourceSets")) {
				sourceDirectory = (project.property("sourceSets") as SourceSetContainer)
					.findByName("main")
					?.resources?.sourceDirectories?.first()?.toPath()?.resolve("kaitai")?.toFile()
					?: sourceRoot
			} else {
				sourceDirectory = sourceRoot
			}
		}

		if (!packageName) {
			packageName = project.group.toString()
		}
		if (isBlank(packageName)) {
			packageName = "kaitai"
		}

		if (!cacheDir) {
			//todo Use more global cache directory?
			cacheDir = project
				.buildDir.toPath()
				.resolve("tmp").resolve("kaitai-cache")
				.toFile()
		}

		if (!output) {
			output = project
				.buildDir.toPath()
				.resolve("generated").resolve("kaitai")
				.toFile()
		}
	}
}
