import sun.plugin.dom.exception.InvalidStateException
import java.awt.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class BitmapReader(path: Path) {
    private val bytes: ByteArray = Files.readAllBytes(path)

    fun doThing(): Bitmap {
        val size = ByteBuffer.wrap(bytes.drop(2).take(4).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        val offset = ByteBuffer.wrap(bytes.drop(10).take(4).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        val dib_size = ByteBuffer.wrap(bytes.drop(14).take(4).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        val width = ByteBuffer.wrap(bytes.drop(18).take(4).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        val height = ByteBuffer.wrap(bytes.drop(22).take(4).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        val bytesPerPixel = ByteBuffer.wrap(bytes.drop(28).take(2).toByteArray()).order(ByteOrder.LITTLE_ENDIAN).short/8

        println("Size: $size bytes")
        println("Offset: $offset bytes")
        println("Dib Size: $dib_size bytes")
        println("Dim: $width x $height")
        println("Bytes Per Pixel: $bytesPerPixel")

        val colors = bytes.drop(offset)
        var pixelOffset = 0
        val bitmap = RawBitmap(width, height)
        for (y in 0..height-1) {
            for (x in 0..width-1) {
                val pixelBytes = colors.subList(pixelOffset, pixelOffset + bytesPerPixel).toByteArray()
                val c = if (bytesPerPixel == 4)
                    ByteBuffer.wrap(pixelBytes).order(ByteOrder.LITTLE_ENDIAN).int
                else
                    ByteBuffer.wrap(pixelBytes).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

                bitmap.set(x, (height - 1) - y, Color(c, bytesPerPixel == 4))
                pixelOffset += bytesPerPixel
            }
        }

        return bitmap
    }
}

interface Bitmap {
    val dimX: Int
    val dimY: Int
    fun get(x: Int, y: Int): Color
    fun set(x: Int, y: Int, c: Color)
}

interface Historical {
    fun save(): Unit
    fun undo(): Unit
}

interface SaveableBitmap: Bitmap, Historical

class RawBitmap(override val dimX: Int, override val dimY: Int): Bitmap {
    val data: Array<Array<Color>> = Array(dimX, { Array(dimY, { Color.BLACK })})

    override fun get(x: Int, y: Int): Color = data[x][y]

    override fun set(x: Int, y: Int, c: Color) {
        data[x][y] = c
    }
}

class MemoryBitmap(val axiom: Bitmap): Bitmap by axiom, SaveableBitmap {
    var head = 0
    val data: Array<Array<SaveHistory<Color>>> =
            Array(axiom.dimX, { x -> Array(axiom.dimY, { y -> SaveHistory(axiom.get(x, y)) })})
    val changes: MutableMap<Pair<Int, Int>, Color> = mutableMapOf()

    override fun set(x: Int, y: Int, c: Color) {
        changes[Pair(x, y)] = c
    }

    override fun undo() {
        head = Math.max(0, head - 1)
    }

    override fun save() {
        head += 1
        for ((point, color) in changes) {
            val (x, y) = point
            data[x][y].pushCommit(head, color)
        }
        changes.clear()
    }

    override fun get(x: Int, y: Int): Color = data[x][y].getCommit(head)
}

class SaveHistory<T>(initial: T) {
    val commitChain: Stack<Pair<Int, T>> = Stack()

    init {
        commitChain.push(Pair(0, initial))
    }

    fun pushCommit(id: Int, item: T) {
        val lastCommitId = commitChain.peek().first
        if (id <= lastCommitId) throw InvalidStateException("Commit history is already ahead of commit id $id")
        commitChain.push(Pair(id, item))
    }

    fun getCommit(commit: Int): T {
        while (commitChain.peek().first > commit) {
            commitChain.pop()
        }

        return commitChain.peek().second
    }
}
