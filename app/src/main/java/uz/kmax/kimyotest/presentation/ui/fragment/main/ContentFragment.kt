package uz.kmax.kimyotest.presentation.ui.fragment.main

import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.adapter.ContentAdapter
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.tools.filter.Filter
import uz.kmax.kimyotest.data.tools.firebase.FirebaseManager
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentContentBinding
import uz.kmax.kimyotest.domain.models.main.MenuContentData
import uz.kmax.kimyotest.presentation.ui.fragment.main.content.list.BookListFragment
import uz.kmax.kimyotest.presentation.ui.fragment.main.content.list.FormulaListFragment
import uz.kmax.kimyotest.presentation.ui.fragment.main.content.list.PeriodicElementsListFragment
import uz.kmax.kimyotest.presentation.ui.fragment.main.content.PeriodicTableFragment
import javax.inject.Inject

@AndroidEntryPoint
class ContentFragment : BaseFragmentWC<FragmentContentBinding>(FragmentContentBinding::inflate) {
    private val adapter by lazy { ContentAdapter() }
    private lateinit var firebaseManager: FirebaseManager
    private var filter = Filter()
    private lateinit var shared : SharedPref
    private var language = "uz"

    @Inject
    lateinit var adsManager: AdsManager

    override fun onViewCreated() {
        /** Ads init */
        adsManager.init()
        /** Ads init */

        firebaseManager = FirebaseManager()

        shared = SharedPref(requireContext())
        language = shared.getLanguage().toString()

        getContentData()
        binding.contentRecycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.contentRecycleView.adapter = adapter

        adapter.setOnTaskListener { contentType, contentLocation ->
            ads(contentType,contentLocation)
        }
    }

    private fun getContentData() {
        binding.shimmerView.startShimmer()
        binding.shimmerView.visibility = View.VISIBLE
        binding.contentRecycleView.visibility = View.GONE
        
        val startTime = System.currentTimeMillis()
        
        firebaseManager.readList("AllContent/$language", MenuContentData::class.java){
            val timePassed = System.currentTimeMillis() - startTime
            val delay = if (timePassed < 2000) 2000 - timePassed else 0L

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.visibility = View.GONE
                    binding.contentRecycleView.visibility = View.VISIBLE
                    
                    if (it != null){
                        adapter.setItems(filter.filterContent(it))
                    }
                }
            }, delay)
        }
    }

    private fun ads(type: Int, contentLocation: String) {
        val currentActivity = activity ?: return
        if (type == 2) {
            adsManager.showAds(currentActivity) { showed ->
                if (!showed) {
                    if (isAdded && !isStateSaved) {
                        replace(type, contentLocation)
                    }
                }
            }
        } else {
            replace(type, contentLocation)
        }

        adsManager.setOnAdClickListener {
            context?.let {
                Toast.makeText(it, "Thank You !", Toast.LENGTH_SHORT).show()
            }
        }

        adsManager.setOnAdDismissListener {
            if (isAdded && !isStateSaved) {
                replace(type, contentLocation)
            }
        }
    }

    private fun replace(type: Int, location : String) {
        when (type) {
            1 -> {
                replaceFragment(BookListFragment())
            }
            2->{
                replaceFragment(PeriodicTableFragment())
            }
            3->{
                replaceFragment(PeriodicElementsListFragment.newInstance(location))
            }
            4->{
                replaceFragment(FormulaListFragment())
            }
            else->{
                Snackbar.make(binding.contentRecycleView, getString(R.string.contentWarningInfo), Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.CYAN)
                    .setTextColor(Color.BLACK)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        adsManager.setOnAdDismissListener {}
        adsManager.setOnAdClickListener {}
        super.onDestroyView()
    }
}