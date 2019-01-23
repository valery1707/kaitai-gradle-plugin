package name.valery1707.kaitai;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;
import org.gradle.internal.installation.CurrentGradleInstallation;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static name.valery1707.kaitai.KaitaiUtils.scanFiles;
import static org.apache.commons.io.filefilter.FileFilterUtils.directoryFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.nameFileFilter;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("WeakerAccess")
public final class GradleUtils {
	private GradleUtils() {
	}

	private static Path mkdirs(Path target) {
		try {
			return KaitaiUtils.mkdirs(target);
		} catch (KaitaiException e) {
			throw new IllegalStateException(e);
		}
	}

	private static final char[] CRLF = {'\r', '\n'};
	static final String CRLF_ = new String(CRLF);
	private static List<Predicate<String>> NEED_SUBSTITUTE = Stream.of(
		".*\\.gradle"
	)
		.map(Pattern::compile)
		.map(Pattern::asPredicate)
		.collect(toList());

	@SuppressWarnings("UnnecessarySemicolon")
	public static Path copyWithSubstitute(Path src, Path dst, Map<String, String> filter) {
		String srcFilename = src.getFileName().toString();
		if (NEED_SUBSTITUTE.stream().noneMatch(p -> p.test(srcFilename))) {
			try {
				return Files.copy(src, dst);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		StrSubstitutor substitute = new StrSubstitutor(filter);
		try (
			BufferedReader reader = newBufferedReader(src, UTF_8);
			BufferedWriter writer = newBufferedWriter(dst, UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(substitute.replace(line));
				writer.write(CRLF);
			}
			writer.flush();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return dst;
	}

	public static Path copyIntegrationProject(String name, Path target) throws IOException, KaitaiException {
		Map<String, String> filter = new HashMap<>();

		List<Path> main = new ArrayList<>();
		FileFilter isMain = FileFilterUtils.and(directoryFileFilter(), nameFileFilter("main"));
		walkFileTree(Paths.get(".").resolve("build"), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (isMain.accept(dir.toFile())) {
					main.add(dir);
				}
				return super.preVisitDirectory(dir, attrs);
			}
		});
		filter.put("dependencies", main
			.stream()
			.map(Path::toAbsolutePath).map(Path::normalize)
			.map(Path::toString)
			.map(s -> String.format("classpath(files(\"%s\"))", s.replace("\\", "\\\\")))
			.collect(joining(GradleUtils.CRLF_ + "\t\t"))
		);

		Path source = Paths.get(".")
			.resolve("src").resolve("it")
			.resolve(name)
			.toAbsolutePath().normalize();
		assertThat(source).exists().isReadable().isDirectory();
		boolean isKotlinScript = Files.exists(source.resolve("build.gradle.kts"));
		filter.put("excludeJcabi", isKotlinScript
			? "exclude(group = \"com.jcabi\")" // Kotlin-style
			: "exclude(group : \"com.jcabi\")" // Groovy-style
		);

		scanFiles(source.getParent(), new String[]{"*.template"}, new String[0]).forEach(template -> filter.put(
			template.getFileName().toString(),
			readFileLines(template).map(new StrSubstitutor(filter)::replace).collect(joining(CRLF_))
		));

		assertThat(target).exists().isWritable().isDirectory();

		scanFiles(source, new String[]{"*"}, new String[0]).forEach(src -> {
			Path dst = target.resolve(source.relativize(src));
			assertThat(mkdirs(dst.getParent())).exists().isWritable().isDirectory();
			assertThat(copyWithSubstitute(src, dst, filter)).exists().isReadable().isRegularFile();
		});
		return target;
	}

	private static Stream<String> readFileLines(Path template) {
		try {
			return Files.lines(template, UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Fail to read lines of " + template.toAbsolutePath().normalize(), e);
		}
	}

	public static Path copyIntegrationProject(String name, File target) throws IOException, KaitaiException {
		return copyIntegrationProject(name, target.toPath());
	}

	public static GradleRunner gradleTestRunner(Path root, String... tasks) {
		List<String> taskList = new ArrayList<>(Arrays.asList(tasks));
		taskList.add("--stacktrace");
		taskList.add("--info");
//		taskList.add("--debug");
//		taskList = Arrays.asList("tasks", "--all");
		return GradleRunner.create()
			.withProjectDir(root.toFile())
			.withPluginClasspath()
			.withArguments(taskList)
			.withDebug(true)
			;
	}

	public static GradleRunner gradleTestRunner(String name, File directory, String... tasks) throws IOException, KaitaiException {
		return gradleTestRunner(copyIntegrationProject(name, directory), tasks);
	}

	public static BuildResult gradleTest(Path root, String... tasks) {
		return gradleTestRunner(root, tasks)
			.build();
	}

	public static BuildResult gradleTest(String name, File directory, String... tasks) throws IOException, KaitaiException {
		return gradleTest(copyIntegrationProject(name, directory), tasks);
	}

	public static ProcResult gradleNative(Path root, String... tasks) {
		Path gradle = Objects.requireNonNull(CurrentGradleInstallation.get())
			.getGradleHome().toPath()
			.resolve("bin").resolve(SystemUtils.IS_OS_WINDOWS ? "gradle.bat" : "gradle");
		ProcBuilder builder = new ProcBuilder(gradle.toAbsolutePath().normalize().toString())
			.withExpectedExitStatuses(0)
			.withNoTimeout()
			.withWorkingDirectory(root.toFile())
			.withArgs(tasks);
		return builder.run();
	}

	public static ProcResult gradleNative(String name, File directory, String... tasks) throws IOException, KaitaiException {
		return gradleNative(copyIntegrationProject(name, directory), tasks);
	}

	public static Pattern patternMultiline(String pattern) {
		return Pattern.compile(pattern, Pattern.MULTILINE);
	}
}
