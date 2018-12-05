package name.valery1707.kaitai

import org.gradle.api.tasks.Internal

class GenerateConfig {
	/**
	 * Skip plugin execution (don't read/validate any files, don't generate any java types).
	 *
	 * @since 0.0.1
	 */
	@Internal
	boolean skip = false;
}
