package uz.kmax.kimyotest.data.adapter

import android.graphics.BitmapFactory
import android.view.View
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import uz.kmax.base.recycleview.BaseRecycleViewDU
import uz.kmax.kimyotest.databinding.ItemBookBinding
import uz.kmax.kimyotest.domain.models.main.BaseBookData

class BookListAdapter :
    BaseRecycleViewDU<ItemBookBinding, BaseBookData>(ItemBookBinding::inflate) {

    override fun bind(binding: ItemBookBinding, item: BaseBookData) {
        binding.bookName.text = item.bookName
        binding.bookTitle.text = item.bookTitle
        binding.bookSize.text = "Book Size : ${item.bookSize}"
        binding.bookRelease.text = "Book Release : ${item.bookRelease}"
        setDataToView(binding, item.bookLocation)
        if (item.bookVisibility == 1) {
            binding.contentNewOld.visibility = View.VISIBLE
        } else {
            binding.contentNewOld.visibility = View.INVISIBLE
        }
        binding.bookLayout.setOnClickListener {
            sendData(item)
        }
    }

    private fun setDataToView(binding: ItemBookBinding, path: String) {
        val storage = Firebase.storage.getReference("KimyoTest/Content/SchoolBooks/BookCover/")
        val imageRef: StorageReference =
            storage.child("$path.png")

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

    override fun areContentsTheSame(oldItem: BaseBookData, newItem: BaseBookData) =
        oldItem == newItem

    override fun areItemsTheSame(oldItem: BaseBookData, newItem: BaseBookData) =
        oldItem.bookLocation == newItem.bookLocation
}