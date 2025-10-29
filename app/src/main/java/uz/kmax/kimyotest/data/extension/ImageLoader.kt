package uz.kmax.kimyotest.data.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage

object ImageLoader {
    fun loadImage(path: String, onImageLoaded: (Bitmap?) -> Unit) {
        val storage = Firebase.storage.getReference("KimyoTest")
        val imageRef: StorageReference =
            storage.child(path).child("image.png")

        imageRef.getBytes(1024 * 1024)
            .addOnSuccessListener { image ->

                onImageLoaded(BitmapFactory.decodeByteArray(
                    image,
                    0,
                    image.size
                ))
            }
    }
}