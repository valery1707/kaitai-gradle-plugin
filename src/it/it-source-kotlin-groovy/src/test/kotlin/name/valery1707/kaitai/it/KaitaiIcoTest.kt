package name.valery1707.kaitai.it

import io.kaitai.struct.ByteBufferKaitaiStream
import io.kaitai.struct.KaitaiStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KaitaiIcoTest {
	private fun getResourceAsStream(resource: String): KaitaiStream {
		return KaitaiIcoTest::class.java
			.getResourceAsStream(resource)
			.use { ByteBufferKaitaiStream(it.readBytes()) }
	}

	@Test
	fun test() {
		val ico = Ico(getResourceAsStream("/document.ico"))
		assertThat(ico).isNotNull()

		assertThat(ico.magic()).isNotNull().hasSize(4).containsExactly(0, 0, 1, 0)

		assertThat(ico.images()).isNotNull().hasSize(23).hasSize(ico.numImages())

		assertThat(ico.images().get(0)).isNotNull()
		assertThat(ico.images().get(0).img()).isNotNull().hasSize(816).startsWith(40, 0, 0, 0, 48)
	}
}
