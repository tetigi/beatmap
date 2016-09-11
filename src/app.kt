import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.swing.JFrame
import javax.swing.JPanel

internal class Surface(val bitmap: Bitmap): JPanel(), ActionListener {

    private fun doDrawing(g: Graphics) {

        val g2d = g as Graphics2D

        for (x in 0..bitmap.dimX-1) {
            for (y in 0..bitmap.dimY-1) {
                g2d.paint = bitmap.get(x, y)
                g2d.drawLine(x, y, x, y)
            }
        }
    }

    override fun paintComponent(g: Graphics) {

        super.paintComponent(g)
        doDrawing(g)
    }

    override fun actionPerformed(e: ActionEvent) {
        repaint()
    }
}

class Window(bitmap: Bitmap) : JFrame() {
    init {
        val surface = Surface(bitmap)
        add(surface)

        title = "Points"
        setSize(bitmap.dimX, bitmap.dimY)
        setLocationRelativeTo(null)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    fun waitForSpace() {
        val latch = CountDownLatch(1)
        val dispatcher = KeyEventDispatcher { e ->
            // Anonymous class invoked from EDT
            if (e.keyCode === KeyEvent.VK_SPACE)
                latch.countDown()
            false
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        latch.await()  // current thread waits here until countDown() is called
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher)
    }
}

fun main(args: Array<String>) {
    val reader = BitmapReader(File("resources/font.bmp").toPath())
    val bitmap = MemoryBitmap(reader.doThing())
    val window = Window(bitmap)
    window.isVisible = true
    val r = Random()
    while (true) {
        window.waitForSpace()
        val h1 = 90 + { h: Int -> if (h >= 0) h else -h } (r.nextInt() % (bitmap.dimY - 50))
        val ratio: Double = 2 * ((h1.toDouble() / bitmap.dimY) - 0.5)
        val amount = 100 * ratio
        val decay = Math.max(1, (10 * Math.abs(ratio)).toInt())
        DrawingUtils.warpX(bitmap, h1, amount.toInt(), decay)
        DrawingUtils.warpX(bitmap, h1 - 30, - amount.toInt(), decay)
        DrawingUtils.warpX(bitmap, h1 - 60, amount.toInt(), decay)
        DrawingUtils.warpX(bitmap, h1 - 90, - amount.toInt(), decay)
        DrawingUtils.whiteNoise(bitmap, 0.2)
        window.repaint()
        Thread.sleep(100)
        bitmap.undo()
        bitmap.undo()
        bitmap.undo()
        bitmap.undo()
        bitmap.undo()
        window.repaint()
    }
}