package uz.kmax.kimyotest.data.game

/**
 * Chemistry 2048 o'yinining asosiy logika mexanizmi.
 * 4x4 grid'dagi elementlarni boshqaradi.
 * Har bir katak atomicNumber (Z) saqaydi (0 = bo'sh).
 */
class Chemistry2048Engine {

    companion object {
        const val GRID_SIZE = 4
        const val EMPTY = 0
    }

    // 4x4 grid: qator x ustun
    private val grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) { EMPTY } }

    var score: Int = 0
        private set

    var hasWon: Boolean = false
        private set

    var isMerged: Boolean = false
        private set

    // Yangi tile animatsiyasi uchun
    data class TileMove(val fromRow: Int, val fromCol: Int, val toRow: Int, val toCol: Int)
    data class MergedTile(val row: Int, val col: Int, val value: Int)
    data class NewTile(val row: Int, val col: Int, val value: Int)

    private val _lastMoves = mutableListOf<TileMove>()
    private val _lastMerges = mutableListOf<MergedTile>()
    private var _lastNewTile: NewTile? = null

    val lastMoves: List<TileMove> get() = _lastMoves.toList()
    val lastMerges: List<MergedTile> get() = _lastMerges.toList()
    val lastNewTile: NewTile? get() = _lastNewTile

    private val _mergedCells = mutableListOf<Pair<Int, Int>>()

    init {
        addFixedTile(1)
        addFixedTile(1)
    }

    fun getMergedCells(): List<Pair<Int, Int>> = _mergedCells.toList()

    /** Joriy grid'ning ko'chirmasini qaytaradi */
    fun getGrid(): Array<IntArray> = Array(GRID_SIZE) { r -> grid[r].copyOf() }

    /** Bo'sh kataklar bormi? */
    fun hasEmptyCell(): Boolean = grid.any { row -> row.any { it == EMPTY } }

    /** O'yin davom eta oladimi? */
    fun canMove(): Boolean {
        if (hasEmptyCell()) return true
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                val v = grid[r][c]
                if (c + 1 < GRID_SIZE && grid[r][c + 1] == v) return true
                if (r + 1 < GRID_SIZE && grid[r + 1][c] == v) return true
            }
        }
        return false
    }

    // ─── Harakat funksiyalari ────────────────────────────────────────────────

    fun swipeLeft(): Boolean = move(0, 1, 0, 0)
    fun swipeRight(): Boolean = move(0, -1, 0, GRID_SIZE - 1)
    fun swipeUp(): Boolean = moveVertical(1, 0)
    fun swipeDown(): Boolean = moveVertical(-1, GRID_SIZE - 1)

    /**
     * Gorizontal harakatni amalga oshiradi.
     * @param colStep ustun tartibining yo'nalishi (+1=chapdan, -1=o'ngdan)
     * @param startCol iteratsiya boshlash ustuni
     */
    private fun move(rowStep: Int, colStep: Int, startRow: Int, startCol: Int): Boolean {
        _lastMoves.clear(); _lastMerges.clear(); _lastNewTile = null; _mergedCells.clear()
        var changed = false
        for (r in 0 until GRID_SIZE) {
            val cols = if (colStep > 0) 0 until GRID_SIZE else GRID_SIZE - 1 downTo 0
            val oldLinePositions = cols.filter { grid[r][it] != EMPTY }
            val line = oldLinePositions.map { grid[r][it] }.toMutableList()
            
            // Merge logic
            val result = mutableListOf<Int>()
            val sourcePositions = oldLinePositions.toMutableList()
            var i = 0
            while (i < line.size) {
                if (i + 1 < line.size && line[i] == line[i + 1]) {
                    val mergedValue = line[i] * 2
                    result.add(mergedValue)
                    score += mergedValue
                    if (mergedValue >= 2048) hasWon = true
                    
                    val targetCol = if (colStep > 0) result.size - 1 else GRID_SIZE - result.size
                    _mergedCells.add(Pair(r, targetCol))
                    
                    // Track moves for both merged tiles
                    _lastMoves.add(TileMove(r, sourcePositions[i], r, targetCol))
                    _lastMoves.add(TileMove(r, sourcePositions[i+1], r, targetCol))
                    
                    i += 2
                } else {
                    result.add(line[i])
                    val targetCol = if (colStep > 0) result.size - 1 else GRID_SIZE - result.size
                    _lastMoves.add(TileMove(r, sourcePositions[i], r, targetCol))
                    i++
                }
            }
            
            val newLine = result.toMutableList()
            while (newLine.size < GRID_SIZE) newLine.add(EMPTY)
            
            for (j in 0 until GRID_SIZE) {
                val targetCol = if (colStep > 0) j else GRID_SIZE - 1 - j
                val newValue = newLine[j]
                if (grid[r][targetCol] != newValue) {
                    changed = true
                    grid[r][targetCol] = newValue
                }
            }
        }
        if (changed) { addRandomTile() }
        return changed
    }

    /** Vertikal harakatni amalga oshiradi. */
    private fun moveVertical(rowStep: Int, startRow: Int): Boolean {
        _lastMoves.clear(); _lastMerges.clear(); _lastNewTile = null; _mergedCells.clear()
        var changed = false
        for (c in 0 until GRID_SIZE) {
            val rows = if (rowStep > 0) 0 until GRID_SIZE else GRID_SIZE - 1 downTo 0
            val oldLinePositions = rows.filter { grid[it][c] != EMPTY }
            val line = oldLinePositions.map { grid[it][c] }.toMutableList()
            
            val result = mutableListOf<Int>()
            val sourcePositions = oldLinePositions.toMutableList()
            var i = 0
            while (i < line.size) {
                if (i + 1 < line.size && line[i] == line[i + 1]) {
                    val mergedValue = line[i] * 2
                    result.add(mergedValue)
                    score += mergedValue
                    if (mergedValue >= 2048) hasWon = true
                    
                    val targetRow = if (rowStep > 0) result.size - 1 else GRID_SIZE - result.size
                    _mergedCells.add(Pair(targetRow, c))
                    
                    _lastMoves.add(TileMove(sourcePositions[i], c, targetRow, c))
                    _lastMoves.add(TileMove(sourcePositions[i+1], c, targetRow, c))
                    
                    i += 2
                } else {
                    result.add(line[i])
                    val targetRow = if (rowStep > 0) result.size - 1 else GRID_SIZE - result.size
                    _lastMoves.add(TileMove(sourcePositions[i], c, targetRow, c))
                    i++
                }
            }
            
            val newLine = result.toMutableList()
            while (newLine.size < GRID_SIZE) newLine.add(EMPTY)

            for (j in 0 until GRID_SIZE) {
                val targetRow = if (rowStep > 0) j else GRID_SIZE - 1 - j
                val newValue = newLine[j]
                if (grid[targetRow][c] != newValue) {
                    changed = true
                    grid[targetRow][c] = newValue
                }
            }
        }
        if (changed) { addRandomTile() }
        return changed
    }

    /**
     * Qatorni birlashtiradi. Bir xil qo'shni elementlar qo'shiladi (Z ikkilanadi).
     * @return Pair(yangi qator, o'zgarish bo'ldimi)
     */
    private fun merge(line: MutableList<Int>): Pair<List<Int>, Boolean> {
        val result = mutableListOf<Int>()
        var changed = false
        var i = 0
        while (i < line.size) {
            if (i + 1 < line.size && line[i] == line[i + 1]) {
                val merged = line[i] * 2
                result.add(merged)
                score += merged
                if (merged >= 2048) hasWon = true
                changed = true
                i += 2
            } else {
                result.add(line[i])
                i++
            }
        }
        val originalNonEmpty = line.size
        if (result.size != originalNonEmpty) changed = true
        return Pair(result, changed)
    }

    /** Tasodifiy bo'sh katakka qiymat beradi. O'yinchining progressiga qarab kattaroq elementlar chiqadi. */
    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        var maxZ = 0
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (grid[r][c] == EMPTY) emptyCells.add(Pair(r, c))
                if (grid[r][c] > maxZ) maxZ = grid[r][c]
            }
        }
        
        if (emptyCells.isEmpty()) return
        val (r, c) = emptyCells.random()
        
        // Tizimli Shift (Bosqichma-bosqich o'sish):
        val rand = Math.random()
        val value = when {
            maxZ >= 1024 -> if (rand < 0.9) 32 else 64
            maxZ >= 512 -> if (rand < 0.9) 16 else 32
            maxZ >= 256 -> if (rand < 0.9) 8 else 16
            maxZ >= 128 -> if (rand < 0.9) 4 else 8
            else -> if (rand < 0.9) 1 else 2
        }
        
        grid[r][c] = value
        _lastNewTile = NewTile(r, c, value)
    }

    private fun getMinGeneratedVal(): Int {
        var maxZ = 0
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (grid[r][c] > maxZ) maxZ = grid[r][c]
            }
        }
        return when {
            maxZ >= 512 -> 16
            maxZ >= 256 -> 8
            maxZ >= 128 -> 4
            maxZ >= 64 -> 2
            else -> 1
        }
    }

    /** Eng kichik (keraksiz) elementlarni o'chiradi (Revive uchun) */
    fun revive() {
        val threshold = getMinGeneratedVal()
        for (r in 0 until GRID_SIZE)
            for (c in 0 until GRID_SIZE)
                if (grid[r][c] != EMPTY && grid[r][c] <= threshold) grid[r][c] = EMPTY
        addRandomTile()
    }

    /** Ma'lum bir katakni o'chiradi (Bolg'a uchun) */
    fun removeTile(r: Int, c: Int): Boolean {
        if (r !in 0 until GRID_SIZE || c !in 0 until GRID_SIZE) return false
        if (grid[r][c] == EMPTY) return false
        grid[r][c] = EMPTY
        return true
    }

    /** Eng kichik (keraksiz) elementlarni tozalaydi */
    fun cleanupLowElements(): Boolean {
        var changed = false
        val threshold = getMinGeneratedVal()
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (grid[r][c] != EMPTY && grid[r][c] <= threshold) {
                    grid[r][c] = EMPTY
                    changed = true
                }
            }
        }
        return changed
    }

    /** O'yinni qayta boshlaydi */
    fun reset() {
        score = 0; hasWon = false
        for (r in 0 until GRID_SIZE) grid[r].fill(EMPTY)
        addFixedTile(1)
        addFixedTile(1)
    }

    private fun addFixedTile(value: Int) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until GRID_SIZE)
            for (c in 0 until GRID_SIZE)
                if (grid[r][c] == EMPTY) emptyCells.add(Pair(r, c))
        if (emptyCells.isEmpty()) return
        val (r, c) = emptyCells.random()
        grid[r][c] = value
        _lastNewTile = NewTile(r, c, value)
    }
}
