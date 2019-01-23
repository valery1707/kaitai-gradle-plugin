package name.valery1707.kaitai;

import org.buildobjects.process.ProcResult;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static name.valery1707.kaitai.GenerateTask.TASK;
import static name.valery1707.kaitai.GradleUtils.*;
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

	private Path buildDir() {
		return projectDir.getRoot().toPath().resolve("build");
	}

	private Path buildTemplate(String name) {
		return buildDir().resolve("resources/main/kaitai").resolve(name + ".ksy");
	}

	private Path generatedDir() {
		return buildDir().resolve("generated");
	}

	private Path generatedClass(String name) {
		return generatedDir().resolve("kaitai/src/name/valery1707/kaitai/it").resolve(name + ".java");
	}

	@Test
	public void testItSkip() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-skip", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.doesNotContain("Skip KaiTai generation")
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.SKIPPED);

		assertThatCompilerNotDownloaded();

		assertThat(buildDir()).doesNotExist();
	}

	@Test
	public void testItSourceAbsent() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-source-absent", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.NO_SOURCE);

		assertThatCompilerNotDownloaded();

		assertThat(buildDir()).doesNotExist();
	}

	@Test
	public void testItSourceEmpty() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-source-empty", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.NO_SOURCE);

		assertThatCompilerNotDownloaded();

		assertThat(buildDir()).doesNotExist();
	}

	@Test
	public void testItSourceExcluded() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-source-excluded", projectDir.getRoot(), TASK);

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.NO_SOURCE);

		assertThatCompilerNotDownloaded();

		assertThat(buildDir()).doesNotExist();
		assertThat(buildTemplate("ico")).doesNotExist();
		assertThat(generatedDir()).doesNotExist();
	}

	@Test
	public void testItSourceExists() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-source-exists", projectDir.getRoot(), TASK, "build");

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

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("ico")).isRegularFile();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("Ico")).isRegularFile();
	}

	@Test
	public void testItSourceFailed() throws IOException, KaitaiException {
		BuildResult result = gradleTestRunner("it-source-failed", projectDir.getRoot(), TASK, "build").buildAndFail();

		String errorMark = "Fail to execute kaitai command: ";
		String errorMessage = "/types/header/seq/0/id: invalid attribute ID: 'Magic', expected /^[a-z][a-z0-9_]*$/";

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD FAILED")
			.containsOnlyOnce(errorMark)
			.containsOnlyOnce(errorMark + errorMessage)
			.contains(errorMessage)
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.FAILED);
		assertThat(result.task(":" + "compileJava")).isNull();
		assertThat(result.task(":" + "test")).isNull();

		assertThatCompilerWasDownloaded();

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("ico")).doesNotExist();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("Ico")).isRegularFile();
	}

	@Test
	public void testItDoubleCompile() throws IOException, KaitaiException {
		Path project = copyIntegrationProject("it-source-exists", projectDir.getRoot());

		//Build after checkout
		BuildResult first = gradleTest(project, TASK, "build");
		//noinspection ConstantConditions
		assertThat(first.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(first.task(":" + "compileJava").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(first.task(":" + "test").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);

		//Second build without changes
		BuildResult second = gradleTest(project, TASK, "build");
		//noinspection ConstantConditions
		assertThat(second.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.UP_TO_DATE);
		//noinspection ConstantConditions
		assertThat(second.task(":" + "compileJava").getOutcome()).isEqualByComparingTo(TaskOutcome.UP_TO_DATE);
		//noinspection ConstantConditions
		assertThat(second.task(":" + "test").getOutcome()).isEqualByComparingTo(TaskOutcome.UP_TO_DATE);

		assertThatCompilerWasDownloaded();

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("ico")).isRegularFile();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("Ico")).isRegularFile();
	}

	/**
	 * We run simply `build` but want implicitly generate java-files from Kaitai templates.
	 */
	@Test
	public void testKaitaiBeforeJavaOnBuild() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-source-exists", projectDir.getRoot(), "build");

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

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("ico")).isRegularFile();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("Ico")).isRegularFile();
	}

	/**
	 * We run simply `build` but want implicitly generate java-files from Kaitai templates.
	 */
	@Test
	public void testKaitaiBeforeKotlinWithGroovyOnBuild() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-source-kotlin-groovy", projectDir.getRoot(), "build");

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(result.task(":" + "compileJava").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(result.task(":" + "compileKotlin").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(result.task(":" + "test").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);

		assertThatCompilerWasDownloaded();

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("ico")).isRegularFile();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("Ico")).isRegularFile();
	}

	/**
	 * We run simply `build` but want implicitly generate java-files from Kaitai templates.
	 * <p>
	 * При сборке KotlinScript-проекта из тестов возникает ошибка "java.lang.ClassNotFoundException: org.gradle.api.tasks.SourceSet".
	 * При сборке остальных проектов такого не происходит. При сборке вне теста такого тоже не происходит.
	 * <p>
	 * А в тесте оказывается что несколько классов не известны classLoader-у и он всё ломает.
	 */
	@Test
	public void testKaitaiBeforeKotlinWithKotlinOnBuild() throws IOException, KaitaiException {
		ProcResult result = gradleNative("it-source-kotlin-kotlin", projectDir.getRoot(), "build");

		assertThat(result.getOutputString())
			.containsPattern(patternMultiline("Task :" + TASK + "$"))
			.containsPattern(patternMultiline("Task :" + "compileJava" + "$"))
			.containsPattern(patternMultiline("Task :" + "compileKotlin" + "$"))
			.containsPattern(patternMultiline("Task :" + "test" + "$"))
			.contains("BUILD SUCCESSFUL")
		;

		assertThatCompilerWasDownloaded();

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("ico")).isRegularFile();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("Ico")).isRegularFile();
	}

	/**
	 * We run simply `build` but want implicitly generate java-files from Kaitai templates.
	 */
	@Test
	public void testWithOption_fromFileClass() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-withOption-fromFileClass", projectDir.getRoot(), "build");

		assertThat(result.getOutput())
			.contains(":" + TASK)
			.contains("BUILD SUCCESSFUL")
			.containsOnlyOnce("CustomStream.CustomStream(String)")
		;
		//noinspection ConstantConditions
		assertThat(result.task(":" + TASK).getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(result.task(":" + "compileJava").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);
		//noinspection ConstantConditions
		assertThat(result.task(":" + "test").getOutcome()).isEqualByComparingTo(TaskOutcome.SUCCESS);

		assertThatCompilerWasDownloaded();

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("ico")).isRegularFile();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("Ico")).isRegularFile();
	}

	/**
	 * We run simply `build` but want implicitly generate java-files from Kaitai templates.
	 */
	@Test
	public void testWithOption_opaqueTypes() throws IOException, KaitaiException {
		BuildResult result = gradleTest("it-withOption-opaqueTypes", projectDir.getRoot(), "build");

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

		assertThat(buildDir()).isDirectory();
		assertThat(buildTemplate("doc_container")).isRegularFile();
		assertThat(generatedDir()).isDirectory();
		assertThat(generatedClass("DocContainer")).isRegularFile();
	}
}
