package uz.kmax.kimyotest.data.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import uz.kmax.kimyotest.data.tools.firebase.FirebaseManager
import uz.kmax.kimyotest.databinding.ItemTestMenuBinding
import uz.kmax.kimyotest.domain.models.main.MenuTestData

sealed class TestListElement {
    data class TestItem(val data: MenuTestData) : TestListElement()
    data class AdItem(val adView: View) : TestListElement()
}

class TestListAdapter : ListAdapter<TestListElement, RecyclerView.ViewHolder>(DiffCallback()) {

    private val firebaseManager = FirebaseManager()
    private var onItemClickListener: ((MenuTestData) -> Unit)? = null
    private var onTaskListener: ((Int, String) -> Unit)? = null

    fun setOnItemSendListener(listener: (MenuTestData) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnTaskListener(listener: (Int, String) -> Unit) {
        onTaskListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TestListElement.TestItem -> 0
            is TestListElement.AdItem -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val binding = ItemTestMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TestViewHolder(binding)
        } else {
            val frame = android.widget.FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            AdViewHolder(frame)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is TestViewHolder && item is TestListElement.TestItem) {
            holder.bind(item.data)
        } else if (holder is AdViewHolder && item is TestListElement.AdItem) {
            holder.bind(item.adView)
        }
    }

    inner class TestViewHolder(private val binding: ItemTestMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MenuTestData) {
            binding.testName.text = item.testName
            binding.testCount.text = "Random"
            binding.testNewOld.visibility = if (item.testNewOld == 1) View.VISIBLE else View.INVISIBLE

            binding.root.setOnClickListener {
                firebaseManager.getChildCount("Test/uz/${item.testLocation}") {
                    onTaskListener?.invoke(it.toInt(), item.testLocation)
                    onItemClickListener?.invoke(item)
                }
            }

            binding.itemImage.tag = item.testLocation
            binding.itemImage.setImageBitmap(null)

            val storage = Firebase.storage.getReference("KimyoTest")
            val imageRef: StorageReference = storage.child("Test/${item.testLocation}").child("image.png")

            imageRef.getBytes(1024 * 1024).addOnSuccessListener { image ->
                if (binding.itemImage.tag == item.testLocation) {
                    binding.itemImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.size))
                }
            }
        }
    }

    inner class AdViewHolder(private val container: ViewGroup) : RecyclerView.ViewHolder(container) {
        fun bind(adView: View) {
            container.removeAllViews()
            (adView.parent as? ViewGroup)?.removeView(adView)
            container.addView(adView)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TestListElement>() {
        override fun areItemsTheSame(oldItem: TestListElement, newItem: TestListElement): Boolean {
            return if (oldItem is TestListElement.TestItem && newItem is TestListElement.TestItem) {
                oldItem.data.testLocation == newItem.data.testLocation
            } else oldItem is TestListElement.AdItem && newItem is TestListElement.AdItem
        }

        override fun areContentsTheSame(oldItem: TestListElement, newItem: TestListElement): Boolean {
            return oldItem == newItem
        }
    }
}
