package name.valery1707.kaitai

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

import static org.apache.commons.lang3.StringUtils.isBlank

class KaitaiExtension {
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
	String version = "0.10"

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

	/**
	 * Specify a timeout in millis for the execution operations.
	 * If not specified the default is 5 seconds.
	 *
	 * <p>
	 * For disabling timeout use any negative value.
	 *
	 * @since 0.1.1
	 */
	long executionTimeout = 5_000

	/**
	 * Classname of custom KaitaiStream implementation which will be used in static builder {@code fromFile(...)}.
	 *
	 * @since 0.1.1
	 */
	String fromFileClass

	/**
	 * Configure compiler to usage opaque (external) types.
	 *
	 * <p>
	 * Read more at <a href="https://doc.kaitai.io/user_guide.html#opaque-types">Kaitai documentation</a>
	 *
	 * @since 0.1.1
	 */
	Boolean opaqueTypes

	KaitaiExtension(Project project) {
		initDefaults(project)
	}

	private void initDefaults(Project project) {
		url = KaitaiUtils.prepareUrl(url, version)

		if (!sourceDirectory) {
			if (project.hasProperty("sourceSets")) {
				try {
					sourceDirectory = (project.property("sourceSets") as SourceSetContainer)
						.findByName("main")
						?.resources?.sourceDirectories?.first()?.toPath()?.resolve("kaitai")?.toFile()
						?: null
				} catch (Throwable e) {
					project.logger.warn("Property 'sourceSets' is not accessible", e)
				}
			}
			if (!sourceDirectory) {
				project.logger.debug("Build assumed path from project directory")
				sourceDirectory = project
					.projectDir.toPath()
					.resolve("src").resolve("main").resolve("resources").resolve("kaitai")
					.toFile()
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
