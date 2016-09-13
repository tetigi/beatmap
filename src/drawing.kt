import java.awt.Color
import java.util.*
object DrawingUtils {
    private val rand = Random()

    fun drip(bm: Bitmap, fallOff: Double, count: Int, update: () -> Unit) {
        val p = 0.1
        var drips = (0..bm.dimX-1)
                .flatMap { x ->
                    (0..bm.dimY - 1)
                            .flatMap { y ->
                                    val q = Math.abs(rand.nextInt().toDouble() / Int.MAX_VALUE)
                                    if (p >= q) listOf(Pair(x, y)) else emptyList()
                            }
                }.toTypedArray()

        for (i in 0..count) {
            val amount = (10 * Math.pow(fallOff, i.toDouble())).toInt()
            if (amount > 0) {
                drips = dripStage(bm, drips, amount)
                update()
            } else {
                break
            }
        }
    }

    private fun dripStage(bm: Bitmap, drips: Array<Pair<Int, Int>>, amount: Int): Array<Pair<Int, Int>> {
        val newDrips = drips
                .map { p -> Pair(p.first, p.second + amount) }
                .filter { p -> p.first < bm.dimX && p.second < bm.dimY }.toTypedArray()
        for (i in 0..drips.size-1) {
            val (x, y) = drips[i]
            val nx = x
            val ny = y + amount
            val c = bm.get(x, y)
            if (nx < bm.dimX && ny < bm.dimY) {
                for (dx in x..nx) {
                    for (dy in y..ny) {
                        bm.set(dx, dy, c)
                    }
                }
            }
        }
        return newDrips
    }

    fun drawHLine(bm: Bitmap, y: Int, c: Color) {
        for (x in 0.. bm.dimX-1) {
            bm.set(x, y, c)
        }
    }

    fun flicker(bm: Bitmap, intensity: Double) {
        val h1 = 40 + { h: Int -> if (h >= 0) h else -h } (rand.nextInt() % (bm.dimY - 40))
        val ratio: Double = 2 * ((h1.toDouble() / bm.dimY) - 0.5)
        val amount = 200 * ratio * intensity
        val decay = Math.max(1, (10 * Math.abs(ratio)).toInt())
        warpX(bm, h1, amount.toInt(), decay)
        warpX(bm, h1 - 20, - amount.toInt(), decay)
        warpX(bm, h1 - 40, amount.toInt(), decay)
        warpX(bm, h1 - 60, - amount.toInt(), decay)
        whiteNoise(bm, 0.2 * intensity)
    }

    fun warpX(bm: Bitmap, y: Int, amount: Int, decay: Int) {
        val absAmount = Math.max(1, Math.abs(amount))
        val sign = amount / absAmount
        for (i in 0..(absAmount / decay)) {
            translateX(bm, Math.min(Math.max(0, y + i), bm.dimY - 1), sign * (absAmount - (i*decay)))
            if (i != 0) {
                translateX(bm, Math.min(bm.dimY - 1, Math.max(y - i, 0)), sign * (absAmount - (i*decay)))
            }
        }
    }

    fun warpY(bm: Bitmap, x: Int, amount: Int, decay: Int) {
        val absAmount = Math.max(1, Math.abs(amount))
        val sign = amount / absAmount
        for (i in 0..(absAmount / decay)) {
            translateY(bm, Math.min(Math.max(0, x + i), bm.dimX - 1), sign * (absAmount - (i*decay)))
            if (i != 0) {
                translateY(bm, Math.min(bm.dimX - 1, Math.max(x - i, 0)), sign * (absAmount - (i*decay)))
            }
        }
    }

    fun whiteNoise(bm: Bitmap, p: Double) {
        val r = Random()
        for (x in 0..bm.dimX-1) {
            for (y in 0..bm.dimY-1) {
                if (p >= Math.abs(r.nextInt().toDouble() / Int.MAX_VALUE)) {
                    bm.set(x, y, Color.BLACK)
                }
            }
        }
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