package uz.kmax.kimyotest.presentation.ui.fragment.main.content.list

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.data.adapter.FormulaAdapter
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.data.tools.firebase.FirebaseManager
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentListBinding
import javax.inject.Inject

@AndroidEntryPoint
class FormulaListFragment : BaseFragmentWC<FragmentListBinding>(FragmentListBinding::inflate){
    private var adapter = FormulaAdapter()
    private lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var shared: SharedPref

    @Inject
    lateinit var adsManager: AdsManager

    override fun onViewCreated() {

        binding.bookRecycleView.layoutManager = LinearLayoutManager(requireContext())
//        binding.bookRecycleView.adapter = adapter
    }
}