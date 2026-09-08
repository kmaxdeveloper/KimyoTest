package uz.kmax.kimyotest.presentation.ui.fragment.main.arcade

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.game.Chemistry2048Engine
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentChemistry2048Binding
import uz.kmax.kimyotest.data.tools.tools.onFragmentBackPressed
import uz.kmax.kimyotest.domain.models.arcade.ChemistryElementTable
import uz.kmax.kimyotest.presentation.ui.fragment.main.MenuFragment
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class Chemistry2048Fragment : BaseFragmentWC<FragmentChemistry2048Binding>(FragmentChemistry2048Binding::inflate) {

    @Inject lateinit var adsManager: AdsManager
    @Inject lateinit var sharedPref: SharedPref

    private val engine = Chemistry2048Engine()
    private lateinit var cells: Array<Array<FrameLayout>>
    private lateinit var gestureDetector: GestureDetectorCompat
    private var madimiFace: Typeface? = null
    
    private var isHammerMode = false
    private var coins = 0
    private var pendingReward = 0

    companion object {
        private const val KEY_BEST_SCORE = "chem2048_best_score"
        private const val KEY_COINS = "chem2048_coins"
        private const val COST_HAMMER = 50
        private const val COST_CLEANUP = 100
        private const val REWARD_AD = 25
        
        private const val SWIPE_MIN_DISTANCE = 80f
        private const val SWIPE_MAX_OFF_PATH = 200f
    }

    override fun onViewCreated() {
        madimiFace = ResourcesCompat.getFont(requireContext(), R.font.madimi)
        coins = sharedPref.getInt(KEY_COINS, 100) // Default 100 coins for new players

        // Reklamalarni ishga tushirish (Tayyorlab qo'yish)
        adsManager.init()
        adsManager.initRewardedAds()

        // Cells grid'ni topish
        cells = Array(4) { r ->
            Array(4) { c ->
                val id = resources.getIdentifier("cell_${r}_${c}", "id", requireContext().packageName)
                if (id != 0) {
                    binding.root.findViewById<FrameLayout>(id)
                } else {
                    // This should theoretically not happen if layout is correct
                    FrameLayout(requireContext()) 
                }
            }
        }

        updateBestScoreDisplay()
        updateCoinDisplay()
        renderGrid()
        setupGestures()
        setupButtons()
        setupPowerUps()
        
        onFragmentBackPressed {
            handleExit()
        }
    }

    private fun updateCoinDisplay() {
        binding.coinText.text = coins.toString()
        sharedPref.saveInt(KEY_COINS, coins)
    }

    private fun addCoins(amount: Int) {
        coins += amount
        updateCoinDisplay()
        // Animation for coins could be added here
    }

    private fun handleCellClick(r: Int, c: Int) {
        if (isHammerMode) {
            if (engine.removeTile(r, c)) {
                isHammerMode = false
                binding.gameBoard.alpha = 1.0f
                renderGrid()
                binding.root.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                Toast.makeText(requireContext(), "Element o'chirildi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── UI Render ────────────────────────────────────────────────────────────

    private fun renderGrid() {
        val grid = engine.getGrid()
        val mergedCells = engine.getMergedCells()
        binding.scoreText.text = engine.score.toString()
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val cell = cells[r][c]
                val value = grid[r][c]
                cell.removeAllViews()
                if (value != Chemistry2048Engine.EMPTY) {
                    val tile = createTileView(value)
                    tile.translationX = 0f
                    tile.translationY = 0f
                    cell.addView(tile)
                    if (mergedCells.contains(Pair(r, c))) {
                        animateMerge(cell)
                    }
                }
            }
        }
    }

    private fun createTileView(atomicNumber: Int): View {
        val element = ChemistryElementTable.getElement(atomicNumber)
        val container = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            val bgColor = element?.color?.toInt() ?: 0xFFCDC1B4.toInt()
            
            val drawable = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(bgColor, darkenColor(bgColor))
            ).apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 8f * resources.displayMetrics.density
            }
            background = drawable
            elevation = 4f
        }

        val symbolView = TextView(requireContext()).apply {
            text = element?.symbol ?: atomicNumber.toString()
            typeface = madimiFace
            textSize = 28f
            val textColor = ChemistryElementTable.getTextColor(atomicNumber).toInt()
            setTextColor(textColor)
            gravity = android.view.Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val nameView = TextView(requireContext()).apply {
            text = element?.nameUz ?: ""
            textSize = 9f
            val textColor = ChemistryElementTable.getTextColor(atomicNumber).toInt()
            setTextColor(textColor)
            gravity = android.view.Gravity.CENTER or android.view.Gravity.BOTTOM
            setPadding(2, 0, 2, 4)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val zView = TextView(requireContext()).apply {
            text = atomicNumber.toString()
            textSize = 9f
            setTextColor(Color.argb(180, 255, 255, 255))
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            setPadding(8, 6, 0, 0)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        container.addView(zView)
        container.addView(nameView)
        container.addView(symbolView)
        return container
    }

    // ─── Animatsiya ───────────────────────────────────────────────────────────

    private fun animateTile(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.15f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.15f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 200
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun animateMerge(cell: FrameLayout) {
        val scaleX = ObjectAnimator.ofFloat(cell, "scaleX", 1f, 1.25f, 1f)
        val scaleY = ObjectAnimator.ofFloat(cell, "scaleY", 1f, 1.25f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 150
            start()
        }
    }

    private fun animateNewTile() {
        val newTile = engine.lastNewTile ?: return
        val cell = cells[newTile.row][newTile.col]
        if (cell.childCount > 0) animateTile(cell.getChildAt(0))
    }

    private var isAnimating = false

    private fun moveTilesWithAnimation() {
        if (isAnimating || isHammerMode) return
        val moves = engine.lastMoves
        if (moves.isEmpty()) {
            renderGrid()
            checkGameState()
            return
        }

        isAnimating = true
        var completedAnimations = 0
        val totalAnimations = moves.size
        
        binding.animationOverlay.removeAllViews()
        
        for (move in moves) {
            val fromCell = cells[move.fromRow][move.fromCol]
            val toCell = cells[move.toRow][move.toCol]
            
            if (fromCell.childCount > 0) {
                val tileView = fromCell.getChildAt(0)
                fromCell.removeView(tileView)
                
                val layoutParams = FrameLayout.LayoutParams(fromCell.width, fromCell.height)
                tileView.layoutParams = layoutParams
                tileView.scaleX = 1.0f
                tileView.scaleY = 1.0f
                binding.animationOverlay.addView(tileView)
                
                val overlayLoc = IntArray(2)
                binding.animationOverlay.getLocationInWindow(overlayLoc)
                val fromLoc = IntArray(2)
                fromCell.getLocationInWindow(fromLoc)
                val toLoc = IntArray(2)
                toCell.getLocationInWindow(toLoc)

                val startX = (fromLoc[0] - overlayLoc[0]).toFloat()
                val startY = (fromLoc[1] - overlayLoc[1]).toFloat()
                val targetX = (toLoc[0] - overlayLoc[0]).toFloat()
                val targetY = (toLoc[1] - overlayLoc[1]).toFloat()
                
                tileView.x = startX
                tileView.y = startY
                
                tileView.animate()
                    .x(targetX)
                    .y(targetY)
                    .setDuration(150)
                    .setUpdateListener { binding.animationOverlay.invalidate() }
                    .withEndAction {
                        binding.animationOverlay.removeView(tileView)
                        completedAnimations++
                        if (completedAnimations == totalAnimations) {
                            binding.animationOverlay.removeAllViews()
                            isAnimating = false
                            renderGrid()
                            animateNewTile()
                            checkGameState()
                            binding.gameBoard.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    }
                    .start()
            } else {
                completedAnimations++
                if (completedAnimations == totalAnimations) {
                    isAnimating = false
                    renderGrid()
                    checkGameState()
                }
            }
        }
    }

    // ─── Gesture (Swipe) ──────────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        gestureDetector = GestureDetectorCompat(requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (binding.gameOverCard.visibility == View.VISIBLE) return false
                    if (isHammerMode) {
                        val cellWidth = binding.gameBoard.width / 4
                        val cellHeight = binding.gameBoard.height / 4
                        val col = (e.x / cellWidth).toInt().coerceIn(0, 3)
                        val row = (e.y / cellHeight).toInt().coerceIn(0, 3)
                        handleCellClick(row, col)
                        return true
                    }
                    return false
                }

                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent,
                    velocityX: Float, velocityY: Float
                ): Boolean {
                    if (isHammerMode || binding.gameOverCard.visibility == View.VISIBLE) return false
                    e1 ?: return false
                    val dX = e2.x - e1.x
                    val dY = e2.y - e1.y
                    val moved: Boolean

                    if (abs(dX) > abs(dY)) {
                        if (abs(dY) > SWIPE_MAX_OFF_PATH) return false
                        moved = if (dX > SWIPE_MIN_DISTANCE) engine.swipeRight()
                        else if (-dX > SWIPE_MIN_DISTANCE) engine.swipeLeft()
                        else return false
                    } else {
                        if (abs(dX) > SWIPE_MAX_OFF_PATH) return false
                        moved = if (dY > SWIPE_MIN_DISTANCE) engine.swipeDown()
                        else if (-dY > SWIPE_MIN_DISTANCE) engine.swipeUp()
                        else return false
                    }

                    if (moved) {
                        moveTilesWithAnimation()
                    }
                    return true
                }
            })

        binding.gameBoard.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    // ─── O'yin holati ─────────────────────────────────────────────────────────

    private fun checkGameState() {
        updateBestScore()
        when {
            engine.hasWon -> showGameOver(won = true)
            !engine.canMove() -> showGameOver(won = false)
        }
    }

    private fun showGameOver(won: Boolean) {
        binding.gameOverCard.visibility = View.VISIBLE
        binding.gameOverTitle.text = if (won) getString(R.string.youWon) else getString(R.string.gameOver)
        
        // Mukofotni hisoblash: Boarddagi eng yuqori elementni topamiz
        val grid = engine.getGrid()
        var maxValue = 0
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (grid[r][c] > maxValue) maxValue = grid[r][c]
            }
        }

        // Element tartib raqamini hisoblaymiz (1 -> 1, 2 -> 2, 4 -> 3, 8 -> 4, 16 -> 5...)
        val elementIndex = if (maxValue > 0) (Math.log(maxValue.toDouble()) / Math.log(2.0)).toInt() + 1 else 0

        if (elementIndex >= 5) {
            // Balanslangan mukofot: 5-elementda 10 tanga, 12-elementda (2048) ~115 tanga
            pendingReward = (elementIndex - 4) * 15
            binding.gameOverScore.text = "Ball: ${engine.score}\nMukofot: +$pendingReward tanga!"
        } else {
            pendingReward = 0
            binding.gameOverScore.text = "Ball: ${engine.score}"
        }
        
        if (won) {
            binding.reviveBtn.text = "Davom etish"
            binding.reviveBtn.setIconResource(0)
            binding.reviveBtn.visibility = View.VISIBLE
        } else {
            binding.reviveBtn.text = getString(R.string.reviveGame)
            binding.reviveBtn.setIconResource(R.drawable.image_watch_ads)
            binding.reviveBtn.visibility = View.VISIBLE
        }
    }

    private fun hideGameOver() {
        binding.gameOverCard.visibility = View.GONE
    }

    // ─── Tugmalar ─────────────────────────────────────────────────────────────

    private fun setupButtons() {
        binding.backBtn.setOnClickListener {
            handleExit()
        }

        binding.newGameBtn.setOnClickListener {
            if (pendingReward > 0) {
                addCoins(pendingReward)
                pendingReward = 0
            }
            engine.reset()
            hideGameOver()
            renderGrid()
        }

        binding.reviveBtn.setOnClickListener {
            if (engine.hasWon) {
                // Agar yutgan bo'lsa, reklamasiz davom etishga ruxsat beramiz
                pendingReward = 0
                hideGameOver()
                return@setOnClickListener
            }
            
            if (adsManager.admobRewardedAdsReady()) {
                adsManager.showRewardedAds(requireActivity()) { reward, success ->
                    if (success && reward > 0) {
                        pendingReward = 0 // Revive bo'lganda pending reward bekor qilinadi
                        engine.revive()
                        hideGameOver()
                        renderGrid()
                    }
                }
            } else {
                adsManager.initRewardedAds()
                Toast.makeText(requireContext(), "Reklama yuklanmoqda...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleExit() {
        val currentActivity = activity ?: return
        if (pendingReward > 0) {
            addCoins(pendingReward)
            pendingReward = 0
        }
        adsManager.setOnAdDismissListener {
            if (isAdded && !isStateSaved) {
                startMainFragment(MenuFragment())
            }
        }
        adsManager.showAds(currentActivity, true) { showed ->
            if (!showed) {
                if (isAdded && !isStateSaved) {
                    startMainFragment(MenuFragment())
                }
            }
        }
    }

    private fun setupPowerUps() {
        binding.btnEarnCoins.setOnClickListener {
            if (binding.gameOverCard.visibility == View.VISIBLE) return@setOnClickListener
            if (adsManager.admobRewardedAdsReady()) {
                adsManager.showRewardedAds(requireActivity()) { reward, success ->
                    if (success) {
                        addCoins(REWARD_AD)
                        Toast.makeText(requireContext(), "+$REWARD_AD tanga qo'shildi!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Reklama tayyor emas", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnHammer.setOnClickListener {
            if (binding.gameOverCard.visibility == View.VISIBLE) return@setOnClickListener
            if (coins >= COST_HAMMER) {
                isHammerMode = !isHammerMode
                if (isHammerMode) {
                    binding.gameBoard.alpha = 0.7f
                    coins -= COST_HAMMER
                    updateCoinDisplay()
                    Toast.makeText(requireContext(), "O'chirmoqchi bo'lgan elementni tanlang", Toast.LENGTH_SHORT).show()
                } else {
                    binding.gameBoard.alpha = 1.0f
                    // Refund if cancelled? Or just let it be. For now, let's just refund if not used.
                    coins += COST_HAMMER
                    updateCoinDisplay()
                }
            } else {
                Toast.makeText(requireContext(), "Tangalar yetarli emas", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnCleanup.setOnClickListener {
            if (binding.gameOverCard.visibility == View.VISIBLE) return@setOnClickListener
            if (coins >= COST_CLEANUP) {
                if (engine.cleanupLowElements()) {
                    coins -= COST_CLEANUP
                    updateCoinDisplay()
                    renderGrid()
                    binding.root.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                    Toast.makeText(requireContext(), "Kichik elementlar tozalandi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Tozalash uchun element yo'q", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Tangalar yetarli emas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── Best Score ───────────────────────────────────────────────────────────

    private fun updateBestScore() {
        val current = engine.score
        val best = sharedPref.getInt(KEY_BEST_SCORE, 0)
        if (current > best) sharedPref.saveInt(KEY_BEST_SCORE, current)
        updateBestScoreDisplay()
    }

    private fun updateBestScoreDisplay() {
        val best = sharedPref.getInt(KEY_BEST_SCORE, 0)
        binding.bestScoreText.text = best.toString()
    }

    private fun darkenColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.85f 
        return Color.HSVToColor(hsv)
    }

    override fun onDestroyView() {
        adsManager.setOnAdDismissListener {}
        adsManager.setOnAdClickListener {}
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
    }
}
