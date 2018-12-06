package name.valery1707.kaitai;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static name.valery1707.kaitai.GenerateTask.TASK;
import static name.valery1707.kaitai.GradleUtils.gradleBuild;
import static org.assertj.core.api.Assertions.assertThat;

public class GenerateTaskTest {
	@Rule
	public final TemporaryFolder projectDir = new TemporaryFolder();

	@Test
	public void testItSkip() throws KaitaiException, IOException {
		BuildResult result = gradleBuild("it-skip", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.doesNotContain("Skip KaiTai generation")
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.SKIPPED);
	}
}
