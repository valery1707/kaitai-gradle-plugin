package name.valery1707.kaitai;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static name.valery1707.kaitai.GenerateTask.TASK;
import static name.valery1707.kaitai.GradleUtils.gradleBuild;
import static name.valery1707.kaitai.KaitaiUtils.scanFiles;
import static org.assertj.core.api.Assertions.assertThat;

public class GenerateTaskTest {
	@Rule
	public final TemporaryFolder projectDir = new TemporaryFolder();

	private List<Path> compiler() throws KaitaiException {
		return scanFiles(
			projectDir.getRoot().toPath(),
			new String[]{"kaitai-struct-compiler*", "*.zip"},
			new String[0]
		);
	}

	private void assertThatCompilerNotDownloaded() throws KaitaiException {
		assertThat(compiler()).isEmpty();
	}

	private void assertThatCompilerWasDownloaded() throws KaitaiException {
		assertThat(compiler()).isNotEmpty();
	}

	@Test
	public void testItSkip() throws IOException, KaitaiException {
		BuildResult result = gradleBuild("it-skip", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.doesNotContain("Skip KaiTai generation")
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.SKIPPED);
		assertThatCompilerNotDownloaded();
	}

	@Test
	public void testItSourceAbsent() throws IOException, KaitaiException {
		BuildResult result = gradleBuild("it-source-absent", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.NO_SOURCE);
		assertThatCompilerNotDownloaded();
	}

	@Test
	public void testItSourceEmpty() throws IOException, KaitaiException {
		BuildResult result = gradleBuild("it-source-empty", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.NO_SOURCE);
		assertThatCompilerNotDownloaded();
	}

	@Test
	public void testItSourceExcluded() throws IOException, KaitaiException {
		BuildResult result = gradleBuild("it-source-excluded", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.NO_SOURCE);
		assertThatCompilerNotDownloaded();
	}

	@Test
	public void testItSourceExists() throws IOException, KaitaiException {
		BuildResult result = gradleBuild("it-source-exists", projectDir.getRoot(), TASK, "build");

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(result.task(":" + "compileJava").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(result.task(":" + "test").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		assertThatCompilerWasDownloaded();
	}
}
