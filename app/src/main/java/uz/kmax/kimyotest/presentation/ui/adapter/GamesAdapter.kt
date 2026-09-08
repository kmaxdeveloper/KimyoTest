package uz.kmax.kimyotest.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.kmax.kimyotest.databinding.ItemGameBinding

class GamesAdapter : RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

    private var onGameClickListener: (() -> Unit)? = null

    fun setOnGameClickListener(listener: () -> Unit) {
        onGameClickListener = listener
    }

    // Hozircha faqat 1 ta o'yin bor: Chemistry 2048
    override fun getItemCount() = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind()
    }

    inner class GameViewHolder(private val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.gameTitle.text = "Kimyo 2048"

            binding.playBtn.setOnClickListener {
                onGameClickListener?.invoke()
            }
            binding.root.setOnClickListener {
                onGameClickListener?.invoke()
            }
        }
    }
}
