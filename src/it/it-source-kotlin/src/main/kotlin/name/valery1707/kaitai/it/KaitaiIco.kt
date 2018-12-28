package name.valery1707.kaitai.it

import io.kaitai.struct.ByteBufferKaitaiStream
import java.io.InputStream

fun readIco(src: InputStream): Ico =
	src.use { Ico(ByteBufferKaitaiStream(it.readBytes())) }
