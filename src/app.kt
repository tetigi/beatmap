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
    //val reader = BitmapReader(File("resources/karab.bmp").toPath())
    val bitmap = MemoryBitmap(reader.doThing())
    val window = Window(bitmap)
    window.isVisible = true
    val rand = Random()
    while (true) {
        window.waitForSpace()
        //DrawingUtils.flicker(bitmap, 1.0)
        DrawingUtils.drip(bitmap, 0.9, 500, {
            Thread.sleep(100)
            bitmap.save()
            window.repaint()
            val q = Math.abs(rand.nextInt().toDouble() / Int.MAX_VALUE)
            if (0.1 > q) {
                DrawingUtils.flicker(bitmap, 1.0)
                window.repaint()
                Thread.sleep(100)
                bitmap.undo()
                window.repaint()
            }
        })
        window.waitForSpace()
        bitmap.hardReset()
        window.repaint()
        Thread.sleep(100)
        //Thread.sleep(100)
        //bitmap.undo()
    }
}