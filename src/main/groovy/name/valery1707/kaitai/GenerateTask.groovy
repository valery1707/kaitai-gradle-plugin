package name.valery1707.kaitai

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.*

import java.nio.file.Files
import java.nio.file.Path

import static java.util.Collections.emptyList
import static name.valery1707.kaitai.KaitaiUtils.*

class GenerateTask extends DefaultTask {
	public static final String TASK = "kaitai"
	public static final String DESC = "Generate java-classes for Kaitai structures"

	@Internal
	KaitaiExtension config

	@Override
	Task configure(Closure closure) {
		config = project.extensions.getByType(KaitaiExtension)
		onlyIf = {
			return !config.skip
		}
		//Add generated directory into Gradle's build scope
		if (project.hasProperty("sourceSets")) {
			def generatedRoot = output.toPath()
			(project.property("sourceSets") as SourceSetContainer)
				.findByName("main")
				?.java { it.srcDir(generatedRoot.toAbsolutePath().normalize().toString()) }
		}
		//Add this stage as dependency for compile scopes
		config.runBefore.each {
			project.getTasksByName(it, false).forEach {
				it.dependsOn(TASK)
			}
		}
		return super.configure(closure)
	}

	@Input
	def getUrl() {
		config.url
	}

	@Input
	def getVersion() {
		config.version
	}

	/**
	 * Skip KaiTai generation: Source directory does not exists
	 * <p/>
	 * Skip KaiTai generation: Source directory does not contain KaiTai templates
	 * @return
	 */
	@InputFiles
	@SkipWhenEmpty
	List<Path> getSource() {
		//todo Maybe executed many times and should be optimized
		def source = config.sourceDirectory.toPath()
		if (Files.exists(source)) {
			scanFiles(
				source,
				config.includes,
				config.excludes
			)
		} else {
			emptyList()
		}
	}

	@OutputDirectory
	def getOutput() {
		config.output
	}

	@Input
	def getPackageName() {
		config.packageName
	}

	@Internal
	def getCacheDir() {
		config.cacheDir.toPath()
	}

	@Input
	@Optional
	def getFromFileClass() {
		config.fromFileClass
	}

	@Input
	@Optional
	Boolean getOpaqueTypes() {
		config.opaqueTypes
	}

	@TaskAction
	def action() {
		//Download Kaitai distribution into cache and unzip it
		URL url = prepareUrl(getUrl(), getVersion())
		Path cacheDir = prepareCache(getCacheDir(), logger)
		Path kaitai = downloadKaitai(url, cacheDir, logger)

		//Generate Java sources
		Path output = mkdirs(output.toPath())
		KaitaiGenerator
			.generator(kaitai, output, packageName)
			.withSource(source)
			.executionTimeout(config.executionTimeout)
			.fromFileClass(fromFileClass)
			.opaqueTypes(opaqueTypes)
			.generate(logger)
	}
}
