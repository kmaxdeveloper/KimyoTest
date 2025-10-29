package uz.kmax.kimyotest.presentation.ui.fragment.main.content.list

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.data.adapter.PeriodicElementsAdapter
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.tools.firebase.FirebaseManager
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentListBinding
import uz.kmax.kimyotest.domain.models.content.list.ElementsListData
import uz.kmax.kimyotest.domain.models.main.MenuContentData
import javax.inject.Inject

@AndroidEntryPoint
class PeriodicElementsListFragment(var location : String) : BaseFragmentWC<FragmentListBinding>(FragmentListBinding::inflate) {

    private var adapter = PeriodicElementsAdapter()
    private var firebaseManager = FirebaseManager()
    private var language = "uz"

    @Inject
    lateinit var shared: SharedPref

    @Inject
    lateinit var adsManager: AdsManager

    override fun onViewCreated() {

        language = shared.getLanguage().toString()

        binding.bookRecycleView.layoutManager = LinearLayoutManager(requireContext())
//        binding.bookRecycleView.adapter = adapter

        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {
        firebaseManager.observeList("Content/$language/$location/", ElementsListData::class.java){

        }
    }
}