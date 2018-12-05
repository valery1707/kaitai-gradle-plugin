package name.valery1707.kaitai

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

class GenerateTask extends DefaultTask {
	public static final String TASK = "kaitai"
	public static final String DESC = "Generate java-classes for Kaitai structures"

	@Nested
	GenerateConfig config

	@Override
	Task configure(Closure closure) {
		config = project.extensions.getByType(GenerateConfig)
		onlyIf = {
			return !config.skip
		}
		return super.configure(closure)
	}

	@TaskAction
	def action() {
	}
}
