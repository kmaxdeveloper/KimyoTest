package uz.kmax.kimyotest.presentation.ui.fragment.main.content.list

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.adapter.BookListAdapter
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.tools.file.SaveFiles
import uz.kmax.kimyotest.data.tools.filter.Filter
import uz.kmax.kimyotest.data.tools.firebase.FirebaseManager
import uz.kmax.kimyotest.data.tools.tools.FindFileFromDevice
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.data.tools.tools.onFragmentBackPressed
import uz.kmax.kimyotest.databinding.FragmentBookListBinding
import uz.kmax.kimyotest.domain.models.main.BaseBookData
import uz.kmax.kimyotest.presentation.ui.dialog.DialogBookNotExist
import uz.kmax.kimyotest.presentation.ui.fragment.main.MenuFragment
import uz.kmax.kimyotest.presentation.ui.fragment.main.content.BookFragment
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class BookListFragment() : BaseFragmentWC<FragmentBookListBinding>(FragmentBookListBinding::inflate){
    private var adapter = BookListAdapter()
    private lateinit var firebaseManager: FirebaseManager
    private var dataFilter = Filter()
    private var language :String = ""
    private var dialog = DialogBookNotExist()
    private var bookPath : String = ""
    private var bookName : String = ""

    @Inject
    lateinit var shared: SharedPref

    @Inject
    lateinit var adsManager: AdsManager

    override fun onViewCreated() {
        val window = activity?.window
        window?.statusBarColor = this.resources.getColor(R.color.appTheme)
        firebaseManager = FirebaseManager()
        language = shared.getLanguage().toString()
        getBookListData()

        binding.bookRecycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.bookRecycleView.adapter = adapter

        adsManager.init()

        binding.back.setOnClickListener {
            val currentActivity = activity ?: return@setOnClickListener
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

        onFragmentBackPressed {
            val currentActivity = activity ?: return@onFragmentBackPressed
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

        adsManager.setOnAdDismissListener {
            if (isAdded && !isStateSaved && bookPath.isNotEmpty()) {
                startMainFragment(BookFragment.newInstance(bookPath, bookName))
            }
        }

        adapter.setOnItemSendListener {
            val fileName = "${it.bookLocation}.pdf"
            val internalFile = File(requireContext().getExternalFilesDir(null), "Kitoblar/$fileName")
            
            if (internalFile.exists()) {
                bookPath = internalFile.absolutePath
                bookName = it.bookName
                adsManager.showAds(requireActivity()) { showed ->
                    if (!showed) {
                        if (isAdded && !isStateSaved) {
                            startMainFragment(BookFragment.newInstance(bookPath, it.bookName))
                        }
                    }
                }
            } else {
                dialog.show(requireContext(), it.bookSize)
                dialog.setOnDownloadNowListener { type ->
                    when(type) {
                        1 -> {
                            downloadAndSavePDF(requireContext(), fileName, "KimyoTest/Content/SchoolBooks/Book/$fileName") { fileUri ->
                                if (fileUri.isNotEmpty()) {
                                    bookPath = fileUri
                                    dialog.setDownloadInfo("✅ Yuklab olindi !")
                                    dialog.setType(2, "✅ Kitobni ochish")
                                } else {
                                    dialog.setType(1, "Qayta yuklash")
                                    dialog.setDownloadInfo("Xatolik yuz berdi . Kitobni qayta yuklang !")
                                }
                            }
                        }
                        2 -> {
                            dialog.dismissDialog()
                            if (isAdded && !isStateSaved) {
                                startMainFragment(BookFragment.newInstance(bookPath, it.bookName))
                            }
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Xatolik yuz berdi !", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun downloadAndSavePDF(context: Context, fileName: String, path : String, onComplete: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child(path)

        // 📌 Faylni vaqtincha yuklash
        val tempFile = File.createTempFile("temp_", ".pdf", context.cacheDir)

        // 📌 Firebase'dan faylni yuklab olish
        storageRef.getFile(tempFile)
            .addOnProgressListener { taskSnapshot ->
                dialog.setDownloadInfo("Yuklanmoqda ....")
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                dialog.downloadProgress(progress)
            }
            .addOnSuccessListener {
                // 📌 Ilova ichki xotirasiga saqlash (Permission denied xatosini oldini oladi)
                val savedPath = SaveFiles.saveFileInternally(context, fileName, tempFile)
                onComplete(savedPath)
            }
            .addOnFailureListener { exception ->
                dialog.setType(1,"Xatolik yuz berdi !")
                Log.e("FirebaseDownload", "Xatolik: ${exception.message}")
                onComplete("")
            }
    }

    private fun getBookListData() {
        binding.shimmerView.startShimmer()
        binding.shimmerView.visibility = View.VISIBLE
        binding.bookRecycleView.visibility = View.GONE
        
        val startTime = System.currentTimeMillis()
        
        firebaseManager.readList("Content/$language/SchoolBooks", BaseBookData::class.java) {
            val timePassed = System.currentTimeMillis() - startTime
            val delay = if (timePassed < 3000) 3000 - timePassed else 0L

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.visibility = View.GONE
                    binding.bookRecycleView.visibility = View.VISIBLE
                    
                    if (it != null) {
                        adapter.setItems(dataFilter.filterBook(it))
                    }
                }
            }, delay)
        }
    }

    override fun onDestroyView() {
        adsManager.setOnAdDismissListener {}
        adsManager.setOnAdClickListener {}
        super.onDestroyView()
    }
}