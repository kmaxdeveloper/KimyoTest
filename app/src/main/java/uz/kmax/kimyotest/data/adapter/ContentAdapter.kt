package uz.kmax.kimyotest.data.adapter

import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.view.View
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import uz.kmax.base.recycleview.BaseRecycleViewDU
import uz.kmax.kimyotest.databinding.ItemContentMenuBinding
import uz.kmax.kimyotest.domain.models.main.MenuContentData
import java.util.Date

class ContentAdapter : BaseRecycleViewDU<ItemContentMenuBinding, MenuContentData>(ItemContentMenuBinding::inflate) {
    override fun bind(binding: ItemContentMenuBinding, item: MenuContentData) {
        when (item.contentType) {
            0-> {
                val day : String = SimpleDateFormat("dd").format(Date())
                val month : String = SimpleDateFormat("MM").format(Date())
                val year : String= SimpleDateFormat("yyyy").format(Date())
                binding.contentName.text = item.contentName
                binding.contentTitle.text = "${day}.${month}.${year}"
                binding.contentNewOld.visibility = View.INVISIBLE
                setDataToView(binding,"DayHistory")
                binding.test.setOnClickListener {
                    sendMessage(item.contentType,item.contentLocation)
                }
            }

            else -> {
                binding.contentName.text = item.contentName
                binding.contentTitle.text = "Content"
                setDataToView(binding,item.contentLocation)
                if (item.contentNewOld == 1) {
                    binding.contentNewOld.visibility = View.VISIBLE
                } else {
                    binding.contentNewOld.visibility = View.INVISIBLE
                }
                binding.test.setOnClickListener {
                    sendMessage(item.contentType,item.contentLocation)
                }
            }
        }
    }

    private fun setDataToView(binding: ItemContentMenuBinding, path : String){
        val storage = Firebase.storage.getReference("KimyoTest/Content")
        val imageRef: StorageReference =
            storage.child(path).child("image.png")

        imageRef.getBytes(1024 * 1024)
            .addOnSuccessListener { image ->
                binding.itemImage.setImageBitmap(
                    BitmapFactory.decodeByteArray(
                        image,
                        0,
                        image.size
                    )
                )
            }
    }

    override fun areContentsTheSame(oldItem: MenuContentData, newItem: MenuContentData) = oldItem == newItem

    override fun areItemsTheSame(oldItem: MenuContentData, newItem: MenuContentData) = oldItem.contentName == newItem.contentName
}