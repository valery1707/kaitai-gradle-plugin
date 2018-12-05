package name.valery1707.kaitai

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateTask extends DefaultTask {
	public static final String TASK = "kaitai";
	public static final String DESC = "Generate java-classes for Kaitai structures";

	@TaskAction
	def action() {
		def conf = project.extensions.getByType(GenerateConfig)
		if (conf.skip) {
			logger.warn("Skip KaiTai generation: skip=true")
		}
	}
}
