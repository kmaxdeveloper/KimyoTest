package uz.kmax.kimyotest.presentation.ui.fragment.main

import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.tools.filter.Filter
import uz.kmax.kimyotest.data.tools.firebase.FirebaseManager
import uz.kmax.kimyotest.data.tools.manager.ConnectionManager
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentTestListBinding
import uz.kmax.kimyotest.domain.models.main.MenuTestData
import uz.kmax.kimyotest.data.adapter.TestListAdapter
import uz.kmax.kimyotest.data.adapter.TestListElement
import uz.kmax.kimyotest.presentation.ui.dialog.DialogConnection
import uz.kmax.kimyotest.presentation.ui.fragment.main.test.HeartTestFragment
import uz.kmax.kimyotest.presentation.ui.fragment.main.test.TimerTestFragment
import javax.inject.Inject

@AndroidEntryPoint
class TestListFragment : BaseFragmentWC<FragmentTestListBinding>(FragmentTestListBinding::inflate) {
    private val adapter by lazy { TestListAdapter() }
    private lateinit var firebaseManager: FirebaseManager
    private var connectionDialog = DialogConnection()
    private var dataFilter = Filter()
    private var language = "uz"
    private var testTypeFilter = 0
    private var nativeAdView: View? = null
    private var originalTestData: List<MenuTestData>? = null

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var shared: SharedPref

    override fun onViewCreated() {
        firebaseManager = FirebaseManager()
        language = shared.getLanguage().toString()
        testTypeFilter = shared.getTestType()

        adsManager.init()
        loadNativeAd()

        getTestListData()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        adapter.setOnTaskListener { testCount, testLocation ->
            // Use current original data to find the item if needed, but here it's passed directly
        }
        
        adapter.setOnItemSendListener { data ->
            if (ConnectionManager().check(requireContext())) {
                checkLocation(data.testLocation, data.testCount, data.testType)
            } else {
                connectionDialog.show(requireContext())
                connectionDialog.setOnCloseListener { activity?.finish() }
                connectionDialog.setOnTryAgainListener {
                    if (ConnectionManager().check(requireContext())) {
                        checkLocation(data.testLocation, data.testCount, data.testType)
                    } else {
                        connectionDialog.show(requireContext())
                    }
                }
            }
        }
    }

    private fun getTestListData() {
        binding.shimmerView.startShimmer()
        binding.shimmerView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        
        val startTime = System.currentTimeMillis()
        
        firebaseManager.readList("AllTest/$language", MenuTestData::class.java){ data ->
            val timePassed = System.currentTimeMillis() - startTime
            val delay = if (timePassed < 2000) 2000 - timePassed else 0L
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    
                    if (data != null) {
                        originalTestData = dataFilter.filterTest(data)
                        updateListWithAds()
                    }
                }
            }, delay)
        }
    }

    private fun loadNativeAd() {
        adsManager.loadNativeAd(binding.recyclerView) { adView ->
            if (isAdded && !isStateSaved && adView != null) {
                nativeAdView = adView
                updateListWithAds()
            }
        }
    }

    private fun updateListWithAds() {
        val tests = originalTestData ?: return
        val items = mutableListOf<TestListElement>()
        
        tests.forEachIndexed { index, test ->
            items.add(TestListElement.TestItem(test))
            // Har 3-testdan keyin reklama qo'shish (faqat bitta bo'lsa ham)
            if (index == 2 && nativeAdView != null) {
                items.add(TestListElement.AdItem(nativeAdView!!))
            }
        }
        
        // Agar testlar kam bo'lsa va reklama yuklangan bo'lsa, oxiriga qo'shamiz
        if (tests.size <= 2 && nativeAdView != null && items.none { it is TestListElement.AdItem }) {
            items.add(TestListElement.AdItem(nativeAdView!!))
        }

        adapter.submitList(items)
    }

    private fun ads(testLocation: String, testCount: Int, testType: Int) {
        val currentActivity = activity ?: return
        adsManager.showAds(currentActivity){ showed ->
            if (!showed) {
                if (isAdded && !isStateSaved) {
                    startTestFragment(testType, testLocation, testCount)
                }
            }
        }

        adsManager.setOnAdClickListener {
            context?.let {
                Toast.makeText(it, "Thank You Bro !", Toast.LENGTH_SHORT).show()
            }
        }

        adsManager.setOnAdDismissListener {
            if (isAdded && !isStateSaved) {
                startTestFragment(testType, testLocation, testCount)
            }
        }
    }

    private fun startTestFragment(testType: Int, testLocation: String, testCount: Int){
        if (!isAdded || isStateSaved || activity == null) return
        when(testTypeFilter){
            1-> {
                startTest(testType, testLocation, testCount)
            }
            3-> {
                replaceFragment(HeartTestFragment.newInstance(testLocation, testCount))
            }
            else -> {
                context?.let {
                    Toast.makeText(it, "Dasturda Texnik nosozlik bo'ldi !", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkLocation(testLocation: String, testCount: Int, testType : Int){
        firebaseManager.observeListVisibly("Test/$language/$testLocation"){ status->
            if (!isAdded) return@observeListVisibly
            if (status){
                when(testType){
                    1, 3 -> {
                        ads(testLocation, testCount, testType)
                    }
                    else -> {
                        Snackbar.make(binding.recyclerView, "Bu test dastur versiyasiga mos emas ! \n Dasturni yangilang", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.WHITE)
                            .setTextColor(Color.BLACK)
                            .show()
                    }
                }
            }else{
                Snackbar.make(binding.recyclerView, "Texnik ishlar olib borilmoqda !", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.WHITE)
                    .setTextColor(Color.BLACK)
                    .show()
            }
        }
    }

    private fun startTest(type: Int,testLocation: String,testCount: Int){
        if (!isAdded || isStateSaved || activity == null) return
        when(type){
            1-> {
                startMainFragment(TimerTestFragment.newInstance(testLocation, testCount))
            }
            3-> {
                startMainFragment(HeartTestFragment.newInstance(testLocation, testCount))
            }
        }
    }

    override fun onDestroyView() {
        adsManager.setOnAdDismissListener {}
        adsManager.setOnAdClickListener {}
        super.onDestroyView()
    }
}
