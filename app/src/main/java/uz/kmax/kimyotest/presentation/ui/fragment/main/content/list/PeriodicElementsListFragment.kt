package uz.kmax.kimyotest.presentation.ui.fragment.main.content.list

import android.os.Bundle
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
class PeriodicElementsListFragment : BaseFragmentWC<FragmentListBinding>(FragmentListBinding::inflate) {

    private var location: String = ""

    companion object {
        private const val ARG_LOCATION = "location"

        fun newInstance(location: String): PeriodicElementsListFragment {
            val fragment = PeriodicElementsListFragment()
            val args = Bundle()
            args.putString(ARG_LOCATION, location)
            fragment.arguments = args
            return fragment
        }
    }

    private var adapter = PeriodicElementsAdapter()
    private var firebaseManager = FirebaseManager()
    private var language = "uz"

    @Inject
    lateinit var shared: SharedPref

    @Inject
    lateinit var adsManager: AdsManager

    override fun onViewCreated() {
        location = arguments?.getString(ARG_LOCATION) ?: ""
        language = shared.getLanguage().toString()

        binding.bookRecycleView.layoutManager = LinearLayoutManager(requireContext())
        // binding.bookRecycleView.adapter = adapter // Adapter is empty, skipping for now

        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {
        firebaseManager.readList("Content/$language/$location/", ElementsListData::class.java){ list ->
            if (isAdded && !isStateSaved && list != null) {
                // adapter.setItems(list)
            }
        }
    }
}