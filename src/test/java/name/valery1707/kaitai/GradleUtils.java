package name.valery1707.kaitai;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static java.util.stream.Collectors.joining;
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

	@SuppressWarnings("UnnecessarySemicolon")
	public static Path copyWithSubstitute(Path src, Path dst, Map<String, String> filter) {
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
		Map<String, String> filter = new HashMap<>(1);

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
			.map(s -> String.format("classpath files(\"%s\")", s.replace("\\", "\\\\")))
			.collect(joining(GradleUtils.CRLF_ + "\t\t"))
		);

		Path source = Paths.get(".")
			.resolve("src").resolve("it")
			.resolve(name)
			.toAbsolutePath().normalize();
		assertThat(source).exists().isReadable().isDirectory();

		assertThat(target).exists().isWritable().isDirectory();

		scanFiles(source, new String[]{"*"}, new String[0]).forEach(src -> {
			Path dst = target.resolve(source.relativize(src));
			assertThat(mkdirs(dst.getParent())).exists().isWritable().isDirectory();
			assertThat(GradleUtils.copyWithSubstitute(src, dst, filter)).exists().isReadable().isRegularFile();
		});
		return target;
	}

	public static Path copyIntegrationProject(String name, File target) throws IOException, KaitaiException {
		return copyIntegrationProject(name, target.toPath());
	}

	public static BuildResult gradleBuild(Path root, String task) {
		return GradleRunner.create()
			.withProjectDir(root.toFile())
			.withPluginClasspath()
//			.withArguments("tasks", "--all")
			.withArguments(task, "--stacktrace")
			.withDebug(true)
			.build();
	}

	public static BuildResult gradleBuild(String name, File directory, String task) throws IOException, KaitaiException {
		return gradleBuild(copyIntegrationProject(name, directory), task);
	}
}
