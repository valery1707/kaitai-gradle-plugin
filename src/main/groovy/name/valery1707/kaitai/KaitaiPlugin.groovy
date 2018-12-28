package name.valery1707.kaitai

import org.gradle.api.Plugin
import org.gradle.api.Project

class KaitaiPlugin implements Plugin<Project> {
	public static final String GROUP = "Kaitai"

	@Override
	void apply(Project project) {
		project.extensions.create(GenerateTask.TASK, GenerateConfig, project)
		project.task(GenerateTask.TASK, type: GenerateTask) {
			group = GROUP
			description = GenerateTask.DESC
		}
	}
}
