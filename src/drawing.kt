import java.awt.Color
import java.util.*

object DrawingUtils {
    fun drawHLine(bm: Bitmap, y: Int, c: Color) {
        for (x in 0.. bm.dimX-1) {
            bm.set(x, y, c)
        }
    }

    fun warpX(bm: SaveableBitmap, y: Int, amount: Int, decay: Int) {
        val absAmount = Math.max(1, Math.abs(amount))
        val sign = amount / absAmount
        for (i in 0..(absAmount / decay)) {
            translateX(bm, Math.min(Math.max(0, y + i), bm.dimY - 1), sign * (absAmount - (i*decay)))
            if (i != 0) {
                translateX(bm, Math.min(bm.dimY - 1, Math.max(y - i, 0)), sign * (absAmount - (i*decay)))
            }
        }
        bm.save()
    }

    fun warpY(bm: SaveableBitmap, x: Int, amount: Int, decay: Int) {
        val absAmount = Math.max(1, Math.abs(amount))
        val sign = amount / absAmount
        for (i in 0..(absAmount / decay)) {
            translateY(bm, Math.min(Math.max(0, x + i), bm.dimX - 1), sign * (absAmount - (i*decay)))
            if (i != 0) {
                translateY(bm, Math.min(bm.dimX - 1, Math.max(x - i, 0)), sign * (absAmount - (i*decay)))
            }
        }
        bm.save()
    }

    fun whiteNoise(bm: SaveableBitmap, p: Double) {
        val r = Random()
        for (x in 0..bm.dimX-1) {
            for (y in 0..bm.dimY-1) {
                if (p >= Math.abs(r.nextInt().toDouble() / Int.MAX_VALUE)) {
                    bm.set(x, y, Color.BLACK)
                }
            }
        }
        bm.save()
    }

    fun translateX(bm: Bitmap, y: Int, amount: Int, fill: Color = Color.WHITE) {
        val store: Array<Color> = Array(bm.dimX, { x -> bm.get(x, y) })
        for (x in 0..bm.dimX-1) {
            val newX = x - amount
            val color = if (newX < 0 || newX >= bm.dimX) fill else store[newX]
            bm.set(x, y, color)
        }
    }

    fun translateY(bm: Bitmap, x: Int, amount: Int, fill: Color = Color.WHITE) {
        val store: Array<Color> = Array(bm.dimY, { y -> bm.get(x, y) })
        for (y in 0..bm.dimY-1) {
            val newY = y - amount
            val color = if (newY < 0 || newY >= bm.dimY) fill else store[newY]
            bm.set(x, y, color)
        }
    }
}